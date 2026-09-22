package ai.timefold.solver.core.impl.bavet.common.index;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import ai.timefold.solver.core.impl.util.Pair;
import ai.timefold.solver.core.impl.util.Quadruple;
import ai.timefold.solver.core.impl.util.Triple;

import org.junit.jupiter.api.Test;

class IndexKeyTest {

    @Test
    void biEqualKey() {
        var key = new BiEqualKey("a", 1);
        assertThat(key)
                .isEqualTo(new BiEqualKey("a", 1))
                .hasSameHashCodeAs(new BiEqualKey("a", 1))
                .hasSameHashCodeAs(new Pair<>("a", 1))
                .isNotEqualTo(new BiEqualKey("a", 2))
                .isNotEqualTo(new BiEqualKey(1, "a"))
                .isNotEqualTo(new Pair<>("a", 1));
        assertThat(key.size()).isEqualTo(2);
        assertThat(key.get(0)).isEqualTo("a");
        assertThat(key.get(1)).isEqualTo(1);
        assertThatThrownBy(() -> key.get(2)).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    void triEqualKey() {
        var key = new TriEqualKey("a", 1, 2L);
        assertThat(key)
                .isEqualTo(new TriEqualKey("a", 1, 2L))
                .hasSameHashCodeAs(new Triple<>("a", 1, 2L))
                .isNotEqualTo(new TriEqualKey("a", 1, 3L));
        assertThat(key.size()).isEqualTo(3);
        assertThat(key.get(2)).isEqualTo(2L);
    }

    @Test
    void quadEqualKey() {
        var key = new QuadEqualKey("a", 1, 2L, 'c');
        assertThat(key)
                .isEqualTo(new QuadEqualKey("a", 1, 2L, 'c'))
                .hasSameHashCodeAs(new Quadruple<>("a", 1, 2L, 'c'))
                .isNotEqualTo(new QuadEqualKey("b", 1, 2L, 'c'));
        assertThat(key.size()).isEqualTo(4);
        assertThat(key.get(3)).isEqualTo('c');
    }

    @Test
    void nulls() {
        assertThat(new BiEqualKey(null, null))
                .isEqualTo(new BiEqualKey(null, null))
                .hasSameHashCodeAs(new Pair<>(null, null))
                .isNotEqualTo(new BiEqualKey(null, "a"));
        assertThat(new TriEqualKey(null, "a", null))
                .isEqualTo(new TriEqualKey(null, "a", null))
                .hasSameHashCodeAs(new Triple<>(null, "a", null));
        assertThat(new QuadEqualKey(null, null, null, "a"))
                .isEqualTo(new QuadEqualKey(null, null, null, "a"))
                .hasSameHashCodeAs(new Quadruple<>(null, null, null, "a"));
        assertThat(new IndexerKey("a", null, "b", null, "c"))
                .isEqualTo(new IndexerKey("a", null, "b", null, "c"));
    }

    @Test
    void indexerKey() {
        var key = new IndexerKey("a", 1, 2L, 'c', "e");
        assertThat(key)
                .isEqualTo(new IndexerKey("a", 1, 2L, 'c', "e"))
                .hasSameHashCodeAs(new IndexerKey("a", 1, 2L, 'c', "e"))
                .isNotEqualTo(new IndexerKey("a", 1, 2L, 'c', "f"));
        assertThat(key.size()).isEqualTo(5);
        assertThat(key.get(4)).isEqualTo("e");
    }

    @Test
    void arrayComponents() {
        // Only IndexerKey compares arrays by content, as before.
        assertThat(new IndexerKey(new int[] { 1 }, 2, 3, 4, 5))
                .isEqualTo(new IndexerKey(new int[] { 1 }, 2, 3, 4, 5));
        assertThat(new BiEqualKey(new int[] { 1 }, 2))
                .isNotEqualTo(new BiEqualKey(new int[] { 1 }, 2));
    }

}
