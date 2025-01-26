package ai.timefold.solver.core.impl.score.stream.collector.connected_ranges;

import java.util.Comparator;
import java.util.Iterator;
import java.util.Objects;

import it.unimi.dsi.fastutil.Hash;
import it.unimi.dsi.fastutil.objects.Object2IntOpenCustomHashMap;

public class RangeSplitPoint<Range_, Point_ extends Comparable<Point_>>
        implements Comparable<RangeSplitPoint<Range_, Point_>> {
    final Point_ splitPoint;
    Object2IntOpenCustomHashMap<Range_> startpointRangeToCountMap;
    Object2IntOpenCustomHashMap<Range_> endpointRangeToCountMap;
    TreeMultiSet<Range<Range_, Point_>> rangesStartingAtSplitPointSet;
    TreeMultiSet<Range<Range_, Point_>> rangesEndingAtSplitPointSet;

    public RangeSplitPoint(Point_ splitPoint) {
        this.splitPoint = splitPoint;
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected void createCollections() {
        startpointRangeToCountMap =
                new Object2IntOpenCustomHashMap<Range_>((IdentityHashingStrategy) IdentityHashingStrategy.INSTANCE);
        endpointRangeToCountMap =
                new Object2IntOpenCustomHashMap<Range_>((IdentityHashingStrategy) IdentityHashingStrategy.INSTANCE);
        rangesStartingAtSplitPointSet = new TreeMultiSet<>(
                Comparator.<Range<Range_, Point_>, Point_> comparing(Range::getEnd)
                        .thenComparingInt(range -> System.identityHashCode(range.getValue())));
        rangesEndingAtSplitPointSet = new TreeMultiSet<>(
                Comparator.<Range<Range_, Point_>, Point_> comparing(Range::getStart)
                        .thenComparingInt(range -> System.identityHashCode(range.getValue())));
    }

    public boolean addRangeStartingAtSplitPoint(Range<Range_, Point_> range) {
        return addRange(range, startpointRangeToCountMap, rangesStartingAtSplitPointSet);
    }

    private static <Range_, Point_ extends Comparable<Point_>> boolean addRange(Range<Range_, Point_> range,
            Object2IntOpenCustomHashMap<Range_> rangeToCountMap,
            TreeMultiSet<Range<Range_, Point_>> rangesAtSplitPointSet) {
        rangeToCountMap.addTo(range.getValue(), 1);
        return rangesAtSplitPointSet.add(range);
    }

    public void removeRangeStartingAtSplitPoint(Range<Range_, Point_> range) {
        removeRange(range, startpointRangeToCountMap, rangesStartingAtSplitPointSet);
    }

    private static <Range_, Point_ extends Comparable<Point_>> void removeRange(Range<Range_, Point_> range,
            Object2IntOpenCustomHashMap<Range_> rangeToCountMap,
            TreeMultiSet<Range<Range_, Point_>> rangesAtSplitPointSet) {
        var value = range.getValue();
        var newCount = rangeToCountMap.addTo(value, -1) - 1;
        if (newCount == 0) {
            rangeToCountMap.removeInt(value);
            rangesAtSplitPointSet.remove(range);
        }
    }

    public boolean addRangeEndingAtSplitPoint(Range<Range_, Point_> range) {
        return addRange(range, endpointRangeToCountMap, rangesEndingAtSplitPointSet);
    }

    public void removeRangeEndingAtSplitPoint(Range<Range_, Point_> range) {
        removeRange(range, endpointRangeToCountMap, rangesEndingAtSplitPointSet);
    }

    public boolean containsRangeStarting(Range<Range_, Point_> range) {
        return rangesStartingAtSplitPointSet.contains(range);
    }

    public boolean containsRangeEnding(Range<Range_, Point_> range) {
        return rangesEndingAtSplitPointSet.contains(range);
    }

    public Iterator<Range_> getValuesStartingFromSplitPointIterator() {
        return rangesStartingAtSplitPointSet.stream()
                .map(Range::getValue)
                .iterator();
    }

    public boolean isEmpty() {
        return rangesStartingAtSplitPointSet.isEmpty() && rangesEndingAtSplitPointSet.isEmpty();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        RangeSplitPoint<?, ?> that = (RangeSplitPoint<?, ?>) o;
        return splitPoint.equals(that.splitPoint);
    }

    public boolean isBefore(RangeSplitPoint<Range_, Point_> other) {
        return compareTo(other) < 0;
    }

    public boolean isAfter(RangeSplitPoint<Range_, Point_> other) {
        return compareTo(other) > 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(splitPoint);
    }

    @Override
    public int compareTo(RangeSplitPoint<Range_, Point_> other) {
        return splitPoint.compareTo(other.splitPoint);
    }

    @Override
    public String toString() {
        return splitPoint.toString();
    }

    private static final class IdentityHashingStrategy<K> implements Hash.Strategy<K> {

        public static final IdentityHashingStrategy<?> INSTANCE = new IdentityHashingStrategy<>();

        private IdentityHashingStrategy() {
        }

        @Override
        public int hashCode(K k) {
            return System.identityHashCode(k);
        }

        @Override
        public boolean equals(K a, K b) {
            return a == b;
        }
    }

}
