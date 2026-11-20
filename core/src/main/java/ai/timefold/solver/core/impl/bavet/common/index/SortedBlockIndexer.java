package ai.timefold.solver.core.impl.bavet.common.index;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.random.RandomGenerator;

import ai.timefold.solver.core.impl.bavet.common.joiner.JoinerType;
import ai.timefold.solver.core.impl.util.ListEntry;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * An {@link Indexer} for a suffix of comparison levels (LT/LTE/GT/GTE) that stores the tuples itself,
 * replacing a chain of nested {@link ComparisonIndexer}s.
 * <p>
 * Entries are kept sorted by the primary key, in blocks of at most {@link #MAX_BLOCK_SIZE} entries.
 * A query walks the blocks from the matching end and stops at the first block that fails the primary test.
 * An optional secondary level skips a whole block with one comparison against the block's best secondary key;
 * this is the {@code overlapping} pair (a LT-type level followed by a GT-type level).
 * All other levels are residual tests on each candidate.
 * <p>
 * Level roles are chosen from the joiner types as the user wrote them (left orientation),
 * so that the left and the right index of one join choose the same levels:
 * <ol>
 * <li>The first LT-type level directly followed by a GT-type level: primary and secondary.</li>
 * <li>Else the first GT-type level directly followed by a LT-type level: primary and secondary.</li>
 * <li>Else the first level is primary, and there is no secondary.</li>
 * </ol>
 * <p>
 * Used only by callers that do not need random access;
 * those keep the {@link ComparisonIndexer} chain (see {@link IndexerFactory}).
 * Does not support changes to the index while a {@link #forEach} runs.
 *
 * @param <T> the element type, see {@link Indexer}
 */
@NullMarked
@SuppressWarnings({ "rawtypes", "unchecked" })
final class SortedBlockIndexer<T> implements Indexer<T> {

    private static final int INITIAL_BLOCK_CAPACITY = 4;
    private static final int MAX_BLOCK_SIZE = 64;
    private static final int MERGE_THRESHOLD = 8;
    private static final int MAX_MERGED_SIZE = 48;

    private static final Block[] NO_BLOCKS = new Block[0];
    private static final Comparable[] NO_KEYS = new Comparable[0];

    private final KeyUnpacker<Comparable> primaryKeyUnpacker;
    private final boolean primaryGreater; // GT-type: matches are a suffix of the ascending order.
    private final boolean primaryOrEquals;
    private final @Nullable KeyUnpacker<Comparable> secondaryKeyUnpacker;
    private final boolean secondaryGreater;
    private final boolean secondaryOrEquals;
    private final KeyUnpacker<?>[] residualKeyUnpackers; // Shared with the layout; never written.
    private final JoinerType[] residualJoinerTypes; // For this side: storedKey OP queryKey.

    private Block<T>[] blocks = NO_BLOCKS;
    private Comparable[] blockMin = NO_KEYS;
    private Comparable @Nullable [] blockBest; // Only with a secondary level.
    private int blockCount = 0;
    private int size = 0;

    SortedBlockIndexer(boolean isLeftBridge, List<JoinerType> joinerTypes, List<KeyUnpacker<?>> keyUnpackers) {
        this(new Layout(isLeftBridge, joinerTypes, keyUnpackers));
    }

    /**
     * Allocates nothing but this object; arrays are created by the first {@link #put}.
     */
    SortedBlockIndexer(Layout layout) {
        this.primaryKeyUnpacker = layout.primaryKeyUnpacker;
        this.primaryGreater = layout.primaryGreater;
        this.primaryOrEquals = layout.primaryOrEquals;
        this.secondaryKeyUnpacker = layout.secondaryKeyUnpacker;
        this.secondaryGreater = layout.secondaryGreater;
        this.secondaryOrEquals = layout.secondaryOrEquals;
        this.residualKeyUnpackers = layout.residualKeyUnpackers;
        this.residualJoinerTypes = layout.residualJoinerTypes;
        this.blockBest = secondaryKeyUnpacker == null ? null : NO_KEYS;
    }

    /**
     * The level roles of one side of one join, computed once and shared by all its indexers
     * (one per equal-prefix bucket under {@link FusedEqualIndex}).
     */
    static final class Layout {

        private final KeyUnpacker<Comparable> primaryKeyUnpacker;
        private final boolean primaryGreater;
        private final boolean primaryOrEquals;
        private final @Nullable KeyUnpacker<Comparable> secondaryKeyUnpacker;
        private final boolean secondaryGreater;
        private final boolean secondaryOrEquals;
        private final KeyUnpacker<?>[] residualKeyUnpackers;
        private final JoinerType[] residualJoinerTypes;

        /**
         * @param isLeftBridge true for the index of the left tuples, false for the right one
         * @param joinerTypes the joiner type of each level, as the user wrote it (not flipped)
         * @param keyUnpackers the key unpacker of each level, in the same order
         */
        Layout(boolean isLeftBridge, List<JoinerType> joinerTypes, List<KeyUnpacker<?>> keyUnpackers) {
            var levelCount = joinerTypes.size();
            if (levelCount == 0 || levelCount != keyUnpackers.size()) {
                throw new IllegalArgumentException(
                        "Impossible state: the joinerTypes (%s) and keyUnpackers (%s) do not match."
                                .formatted(joinerTypes, keyUnpackers));
            }
            var sideTypes = new JoinerType[levelCount];
            for (var i = 0; i < levelCount; i++) {
                var joinerType = joinerTypes.get(i);
                if (!isComparison(joinerType)) {
                    throw new IllegalArgumentException("Impossible state: the joinerType (%s) is not a comparison."
                            .formatted(joinerType));
                }
                sideTypes[i] = isLeftBridge ? joinerType : joinerType.flip();
            }
            var primaryLevel = 0;
            var secondaryLevel = selectPairStart(joinerTypes);
            if (secondaryLevel >= 0) {
                primaryLevel = secondaryLevel;
                secondaryLevel++;
            }
            this.primaryKeyUnpacker = (KeyUnpacker<Comparable>) keyUnpackers.get(primaryLevel);
            this.primaryGreater = isGreater(sideTypes[primaryLevel]);
            this.primaryOrEquals = isOrEquals(sideTypes[primaryLevel]);
            if (secondaryLevel >= 0) {
                this.secondaryKeyUnpacker = (KeyUnpacker<Comparable>) keyUnpackers.get(secondaryLevel);
                this.secondaryGreater = isGreater(sideTypes[secondaryLevel]);
                this.secondaryOrEquals = isOrEquals(sideTypes[secondaryLevel]);
            } else {
                this.secondaryKeyUnpacker = null;
                this.secondaryGreater = false;
                this.secondaryOrEquals = false;
            }
            var residualCount = levelCount - (secondaryLevel >= 0 ? 2 : 1);
            this.residualKeyUnpackers = new KeyUnpacker<?>[residualCount];
            this.residualJoinerTypes = new JoinerType[residualCount];
            var residualIndex = 0;
            for (var i = 0; i < levelCount; i++) {
                if (i != primaryLevel && i != secondaryLevel) {
                    residualKeyUnpackers[residualIndex] = keyUnpackers.get(i);
                    residualJoinerTypes[residualIndex] = sideTypes[i];
                    residualIndex++;
                }
            }
        }

    }

    /**
     * @return the level of the primary of the selected pair (the secondary is the next level), or -1 if there is no pair
     */
    static int selectPairStart(List<JoinerType> joinerTypes) {
        for (var i = 0; i + 1 < joinerTypes.size(); i++) {
            if (!isGreater(joinerTypes.get(i)) && isGreater(joinerTypes.get(i + 1))) {
                return i; // The order that Joiners.overlapping() creates.
            }
        }
        for (var i = 0; i + 1 < joinerTypes.size(); i++) {
            if (isGreater(joinerTypes.get(i)) && !isGreater(joinerTypes.get(i + 1))) {
                return i;
            }
        }
        return -1;
    }

    static boolean isComparison(JoinerType joinerType) {
        return switch (joinerType) {
            case LESS_THAN, LESS_THAN_OR_EQUAL, GREATER_THAN, GREATER_THAN_OR_EQUAL -> true;
            default -> false;
        };
    }

    private static boolean isGreater(JoinerType joinerType) {
        return joinerType == JoinerType.GREATER_THAN || joinerType == JoinerType.GREATER_THAN_OR_EQUAL;
    }

    private static boolean isOrEquals(JoinerType joinerType) {
        return joinerType == JoinerType.LESS_THAN_OR_EQUAL || joinerType == JoinerType.GREATER_THAN_OR_EQUAL;
    }

    private static boolean passes(boolean greater, boolean orEquals, Comparable storedKey, Comparable queryKey) {
        var comparison = storedKey.compareTo(queryKey);
        if (comparison == 0) {
            return orEquals;
        }
        return greater == (comparison > 0);
    }

    private boolean passesPrimary(Comparable storedKey, Comparable queryKey) {
        return passes(primaryGreater, primaryOrEquals, storedKey, queryKey);
    }

    private boolean passesSecondary(Comparable storedKey, Comparable queryKey) {
        return passes(secondaryGreater, secondaryOrEquals, storedKey, queryKey);
    }

    private boolean passesResiduals(Object storedCompositeKey, Object queryCompositeKey) {
        for (var i = 0; i < residualKeyUnpackers.length; i++) {
            var keyUnpacker = residualKeyUnpackers[i];
            if (!residualJoinerTypes[i].matches(keyUnpacker.apply(storedCompositeKey), keyUnpacker.apply(queryCompositeKey))) {
                return false;
            }
        }
        return true;
    }

    /**
     * The secondary key that passes the secondary test most easily:
     * if this one fails for a query, every key that is not better fails too.
     */
    private boolean isBetterSecondary(Comparable candidate, Comparable current) {
        var comparison = candidate.compareTo(current);
        return secondaryGreater ? comparison > 0 : comparison < 0;
    }

    private static Comparable requireKey(@Nullable Comparable key, Object compositeKey) {
        if (key == null) {
            throw new IllegalStateException("""
                    The comparison key (null) of the composite key (%s) is null.
                    Maybe make the mapping function of the comparison joiner (lessThan, greaterThan, ...) \
                    never return null."""
                    .formatted(compositeKey));
        }
        return key;
    }

    @Override
    public ListEntry<T> put(Object compositeKey, T tuple) {
        var primaryKey = requireKey(primaryKeyUnpacker.apply(compositeKey), compositeKey);
        var secondaryKey = secondaryKeyUnpacker == null ? null
                : requireKey(secondaryKeyUnpacker.apply(compositeKey), compositeKey);
        if (blockCount == 0) {
            insertBlock(0, new Block<>(INITIAL_BLOCK_CAPACITY, secondaryKey != null));
        }
        var blockIndex = findBlockForInsert(primaryKey);
        var block = blocks[blockIndex];
        var position = block.upperBound(primaryKey);
        var entry = new Entry<>(tuple, compositeKey, block);
        block.insert(position, primaryKey, secondaryKey, entry);
        if (position == 0) {
            blockMin[blockIndex] = primaryKey;
        }
        if (secondaryKey != null && (block.size == 1 || isBetterSecondary(secondaryKey, blockBest[blockIndex]))) {
            blockBest[blockIndex] = secondaryKey;
        }
        if (block.size == MAX_BLOCK_SIZE) {
            split(blockIndex);
        }
        size++;
        return entry;
    }

    /**
     * @return the last block whose first key is not greater than primaryKey, or the first block if there is none;
     *         equal keys therefore stay in insertion order
     */
    private int findBlockForInsert(Comparable primaryKey) {
        var low = 1;
        var high = blockCount - 1;
        var result = 0;
        while (low <= high) {
            var middle = (low + high) >>> 1;
            if (blockMin[middle].compareTo(primaryKey) <= 0) {
                result = middle;
                low = middle + 1;
            } else {
                high = middle - 1;
            }
        }
        return result;
    }

    private void split(int blockIndex) {
        var block = blocks[blockIndex];
        var newBlock = block.splitUpperHalf();
        insertBlock(blockIndex + 1, newBlock);
        blockMin[blockIndex + 1] = newBlock.primaryKeys[0];
        if (blockBest != null) {
            blockBest[blockIndex] = computeBest(block);
            blockBest[blockIndex + 1] = computeBest(newBlock);
        }
    }

    private Comparable computeBest(Block<T> block) {
        var best = block.secondaryKeys[0];
        for (var i = 1; i < block.size; i++) {
            var candidate = block.secondaryKeys[i];
            if (isBetterSecondary(candidate, best)) {
                best = candidate;
            }
        }
        return best;
    }

    private void insertBlock(int blockIndex, Block<T> block) {
        if (blockCount == blocks.length) {
            var newCapacity = Math.max(4, blockCount * 2);
            blocks = Arrays.copyOf(blocks, newCapacity);
            blockMin = Arrays.copyOf(blockMin, newCapacity);
            if (blockBest != null) {
                blockBest = Arrays.copyOf(blockBest, newCapacity);
            }
        }
        var tailLength = blockCount - blockIndex;
        if (tailLength > 0) {
            System.arraycopy(blocks, blockIndex, blocks, blockIndex + 1, tailLength);
            System.arraycopy(blockMin, blockIndex, blockMin, blockIndex + 1, tailLength);
            if (blockBest != null) {
                System.arraycopy(blockBest, blockIndex, blockBest, blockIndex + 1, tailLength);
            }
        }
        blocks[blockIndex] = block;
        blockCount++;
        renumberBlocks(blockIndex);
    }

    private void removeBlock(int blockIndex) {
        var tailLength = blockCount - blockIndex - 1;
        if (tailLength > 0) {
            System.arraycopy(blocks, blockIndex + 1, blocks, blockIndex, tailLength);
            System.arraycopy(blockMin, blockIndex + 1, blockMin, blockIndex, tailLength);
            if (blockBest != null) {
                System.arraycopy(blockBest, blockIndex + 1, blockBest, blockIndex, tailLength);
            }
        }
        blockCount--;
        blocks[blockCount] = null;
        blockMin[blockCount] = null;
        if (blockBest != null) {
            blockBest[blockCount] = null;
        }
        renumberBlocks(blockIndex);
    }

    private void renumberBlocks(int fromBlockIndex) {
        for (var i = fromBlockIndex; i < blockCount; i++) {
            blocks[i].index = i;
        }
    }

    @Override
    public void remove(Object compositeKey, ListEntry<T> entry) {
        var castEntry = (Entry<T>) entry;
        var block = castEntry.block;
        var position = block == null ? -1 : block.positionOf(castEntry);
        if (position < 0) {
            throw new IllegalStateException(
                    "Impossible state: the tuple (%s) with composite key (%s) doesn't exist in the indexer %s."
                            .formatted(entry, compositeKey, this));
        }
        var blockIndex = block.index;
        var removedSecondaryKey = block.secondaryKeys == null ? null : block.secondaryKeys[position];
        block.removeAt(position);
        castEntry.block = null;
        size--;
        if (block.size == 0) {
            removeBlock(blockIndex);
            return;
        }
        if (position == 0) {
            blockMin[blockIndex] = block.primaryKeys[0];
        }
        if (removedSecondaryKey != null && removedSecondaryKey.compareTo(blockBest[blockIndex]) == 0) {
            blockBest[blockIndex] = computeBest(block);
        }
        if (block.size < MERGE_THRESHOLD) {
            mergeWithNeighbor(blockIndex);
        }
    }

    private void mergeWithNeighbor(int blockIndex) {
        var block = blocks[blockIndex];
        var hasPrevious = blockIndex > 0 && blocks[blockIndex - 1].size + block.size <= MAX_MERGED_SIZE;
        var hasNext = blockIndex + 1 < blockCount && blocks[blockIndex + 1].size + block.size <= MAX_MERGED_SIZE;
        int leftIndex;
        if (hasPrevious && (!hasNext || blocks[blockIndex - 1].size <= blocks[blockIndex + 1].size)) {
            leftIndex = blockIndex - 1;
        } else if (hasNext) {
            leftIndex = blockIndex;
        } else {
            return;
        }
        var left = blocks[leftIndex];
        var right = blocks[leftIndex + 1];
        left.append(right);
        if (blockBest != null && isBetterSecondary(blockBest[leftIndex + 1], blockBest[leftIndex])) {
            blockBest[leftIndex] = blockBest[leftIndex + 1];
        }
        removeBlock(leftIndex + 1);
    }

    @Override
    public int size(Object queryCompositeKey) {
        if (size == 0) {
            return 0;
        }
        if (secondaryKeyUnpacker == null && residualKeyUnpackers.length == 0) {
            return countPrimaryOnly(requireKey(primaryKeyUnpacker.apply(queryCompositeKey), queryCompositeKey));
        }
        return walk(queryCompositeKey, null);
    }

    private int countPrimaryOnly(Comparable queryKey) {
        var count = 0;
        if (primaryGreater) {
            for (var b = blockCount - 1; b >= 0; b--) {
                var block = blocks[b];
                if (!passesPrimary(block.primaryKeys[block.size - 1], queryKey)) {
                    return count;
                }
                if (passesPrimary(blockMin[b], queryKey)) {
                    count += block.size;
                } else {
                    return count + block.size - block.partitionPoint(this, queryKey, true);
                }
            }
        } else {
            for (var b = 0; b < blockCount; b++) {
                var block = blocks[b];
                if (!passesPrimary(blockMin[b], queryKey)) {
                    return count;
                }
                if (passesPrimary(block.primaryKeys[block.size - 1], queryKey)) {
                    count += block.size;
                } else {
                    return count + block.partitionPoint(this, queryKey, false);
                }
            }
        }
        return count;
    }

    @Override
    public void forEach(Object queryCompositeKey, Consumer<T> tupleConsumer) {
        if (size != 0) {
            walk(queryCompositeKey, tupleConsumer);
        }
    }

    /**
     * Visits every match in the order of the primary key (descending for a GT-type primary).
     *
     * @param tupleConsumer null to only count
     * @return the number of matches
     */
    private int walk(Object queryCompositeKey, @Nullable Consumer<T> tupleConsumer) {
        var primaryQueryKey = requireKey(primaryKeyUnpacker.apply(queryCompositeKey), queryCompositeKey);
        var secondaryQueryKey = secondaryKeyUnpacker == null ? null
                : requireKey(secondaryKeyUnpacker.apply(queryCompositeKey), queryCompositeKey);
        var hasResiduals = residualKeyUnpackers.length != 0;
        var count = 0;
        if (primaryGreater) {
            for (var b = blockCount - 1; b >= 0; b--) {
                var block = blocks[b];
                var last = block.size - 1;
                if (!passesPrimary(block.primaryKeys[last], primaryQueryKey)) {
                    return count;
                }
                if (secondaryQueryKey != null && !passesSecondary(blockBest[b], secondaryQueryKey)) {
                    continue;
                }
                var inside = passesPrimary(blockMin[b], primaryQueryKey);
                for (var i = last; i >= 0; i--) {
                    if (!inside && !passesPrimary(block.primaryKeys[i], primaryQueryKey)) {
                        return count;
                    }
                    if (secondaryQueryKey != null && !passesSecondary(block.secondaryKeys[i], secondaryQueryKey)) {
                        continue;
                    }
                    var entry = block.entries[i];
                    if (hasResiduals && !passesResiduals(entry.compositeKey, queryCompositeKey)) {
                        continue;
                    }
                    count++;
                    if (tupleConsumer != null) {
                        tupleConsumer.accept(entry.element);
                    }
                }
            }
        } else {
            for (var b = 0; b < blockCount; b++) {
                var block = blocks[b];
                if (!passesPrimary(blockMin[b], primaryQueryKey)) {
                    return count;
                }
                if (secondaryQueryKey != null && !passesSecondary(blockBest[b], secondaryQueryKey)) {
                    continue;
                }
                var blockSize = block.size;
                var inside = passesPrimary(block.primaryKeys[blockSize - 1], primaryQueryKey);
                for (var i = 0; i < blockSize; i++) {
                    if (!inside && !passesPrimary(block.primaryKeys[i], primaryQueryKey)) {
                        return count;
                    }
                    if (secondaryQueryKey != null && !passesSecondary(block.secondaryKeys[i], secondaryQueryKey)) {
                        continue;
                    }
                    var entry = block.entries[i];
                    if (hasResiduals && !passesResiduals(entry.compositeKey, queryCompositeKey)) {
                        continue;
                    }
                    count++;
                    if (tupleConsumer != null) {
                        tupleConsumer.accept(entry.element);
                    }
                }
            }
        }
        return count;
    }

    @Override
    public Iterator<T> iterator(Object queryCompositeKey) {
        // Only ContainedInIndexer calls iterator() in production, and it is never above this index.
        var matches = new ArrayList<T>();
        forEach(queryCompositeKey, matches::add);
        return matches.iterator();
    }

    @Override
    public RepeatingRandomIterator<T> randomIterator(Object queryCompositeKey, RandomGenerator workingRandom) {
        throw new UnsupportedOperationException("Impossible state: This indexer does not support random access.");
    }

    @Override
    public UniqueRandomIterator<T> uniqueRandomIterator(Object queryCompositeKey, RandomGenerator workingRandom) {
        throw new UnsupportedOperationException("Impossible state: This indexer does not support random access.");
    }

    @Override
    public boolean isRemovable() {
        return size == 0;
    }

    @Override
    public String toString() {
        return "size = " + size;
    }

    private static final class Entry<T> implements ListEntry<T> {

        private final T element;
        private final Object compositeKey;
        private @Nullable Block<T> block;

        private Entry(T element, Object compositeKey, Block<T> block) {
            this.element = element;
            this.compositeKey = compositeKey;
            this.block = block;
        }

        @Override
        public T element() {
            return element;
        }

        @Override
        public String toString() {
            return String.valueOf(element);
        }

    }

    private static final class Block<T> {

        private Comparable[] primaryKeys;
        private Comparable @Nullable [] secondaryKeys;
        private Entry<T>[] entries;
        private int size = 0;
        private int index = 0;

        private Block(int capacity, boolean hasSecondary) {
            this.primaryKeys = new Comparable[capacity];
            this.secondaryKeys = hasSecondary ? new Comparable[capacity] : null;
            this.entries = new Entry[capacity];
        }

        /**
         * @return the first position whose key is greater than primaryKey
         */
        private int upperBound(Comparable primaryKey) {
            var low = 0;
            var high = size;
            while (low < high) {
                var middle = (low + high) >>> 1;
                if (primaryKeys[middle].compareTo(primaryKey) <= 0) {
                    low = middle + 1;
                } else {
                    high = middle;
                }
            }
            return low;
        }

        /**
         * The primary test is monotone over the sorted keys:
         * LT-type passes on a prefix, GT-type passes on a suffix.
         *
         * @return the first position where the primary test result equals {@code greater}
         */
        private int partitionPoint(SortedBlockIndexer<T> indexer, Comparable queryKey, boolean greater) {
            var low = 0;
            var high = size;
            while (low < high) {
                var middle = (low + high) >>> 1;
                if (indexer.passesPrimary(primaryKeys[middle], queryKey) != greater) {
                    low = middle + 1;
                } else {
                    high = middle;
                }
            }
            return low;
        }

        private int positionOf(Entry<T> entry) {
            for (var i = 0; i < size; i++) {
                if (entries[i] == entry) {
                    return i;
                }
            }
            return -1;
        }

        private void insert(int position, Comparable primaryKey, @Nullable Comparable secondaryKey, Entry<T> entry) {
            if (size == entries.length) {
                ensureCapacity(Math.min(MAX_BLOCK_SIZE, size * 2));
            }
            var tailLength = size - position;
            if (tailLength > 0) {
                System.arraycopy(primaryKeys, position, primaryKeys, position + 1, tailLength);
                System.arraycopy(entries, position, entries, position + 1, tailLength);
                if (secondaryKeys != null) {
                    System.arraycopy(secondaryKeys, position, secondaryKeys, position + 1, tailLength);
                }
            }
            primaryKeys[position] = primaryKey;
            entries[position] = entry;
            if (secondaryKeys != null) {
                secondaryKeys[position] = secondaryKey;
            }
            size++;
        }

        private void removeAt(int position) {
            var tailLength = size - position - 1;
            if (tailLength > 0) {
                System.arraycopy(primaryKeys, position + 1, primaryKeys, position, tailLength);
                System.arraycopy(entries, position + 1, entries, position, tailLength);
                if (secondaryKeys != null) {
                    System.arraycopy(secondaryKeys, position + 1, secondaryKeys, position, tailLength);
                }
            }
            size--;
            primaryKeys[size] = null;
            entries[size] = null;
            if (secondaryKeys != null) {
                secondaryKeys[size] = null;
            }
        }

        private void ensureCapacity(int capacity) {
            if (capacity > entries.length) {
                primaryKeys = Arrays.copyOf(primaryKeys, capacity);
                entries = Arrays.copyOf(entries, capacity);
                if (secondaryKeys != null) {
                    secondaryKeys = Arrays.copyOf(secondaryKeys, capacity);
                }
            }
        }

        private Block<T> splitUpperHalf() {
            var half = size / 2;
            var movedCount = size - half;
            var newBlock = new Block<T>(MAX_BLOCK_SIZE, secondaryKeys != null);
            System.arraycopy(primaryKeys, half, newBlock.primaryKeys, 0, movedCount);
            System.arraycopy(entries, half, newBlock.entries, 0, movedCount);
            if (secondaryKeys != null) {
                System.arraycopy(secondaryKeys, half, newBlock.secondaryKeys, 0, movedCount);
            }
            newBlock.size = movedCount;
            for (var i = 0; i < movedCount; i++) {
                newBlock.entries[i].block = newBlock;
            }
            Arrays.fill(primaryKeys, half, size, null);
            Arrays.fill(entries, half, size, null);
            if (secondaryKeys != null) {
                Arrays.fill(secondaryKeys, half, size, null);
            }
            size = half;
            return newBlock;
        }

        private void append(Block<T> other) {
            ensureCapacity(MAX_BLOCK_SIZE);
            System.arraycopy(other.primaryKeys, 0, primaryKeys, size, other.size);
            System.arraycopy(other.entries, 0, entries, size, other.size);
            if (secondaryKeys != null) {
                System.arraycopy(other.secondaryKeys, 0, secondaryKeys, size, other.size);
            }
            for (var i = 0; i < other.size; i++) {
                other.entries[i].block = this;
            }
            size += other.size;
        }

    }

}
