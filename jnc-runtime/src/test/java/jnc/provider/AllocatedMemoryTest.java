package jnc.provider;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import jnc.foreign.Pointer;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class AllocatedMemoryTest {

    @Test
    public void testIllegalArgument() {
        assertThatThrownBy(() -> AllocatedMemory.allocate(-1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AllocatedMemory.allocate(-1, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AllocatedMemory.allocate(1, -1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AllocatedMemory.allocate(-1, -1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AllocatedMemory.allocate(0, -1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> AllocatedMemory.allocate(-1, 0))
                .isInstanceOf(IllegalArgumentException.class);
        AllocatedMemory.allocate(0, 0);
    }

    @Test
    public void testOutOfMemory() {
        assertThatThrownBy(() -> AllocatedMemory.allocate(Long.MAX_VALUE))
                .isInstanceOf(OutOfMemoryError.class);
        assertThatThrownBy(() -> AllocatedMemory.allocate(Integer.MAX_VALUE, Integer.MAX_VALUE))
                .isInstanceOf(OutOfMemoryError.class);
    }

    @Test
    public void testIndexOfRange() {
        Pointer memory = AllocatedMemory.allocate(3);
        assertThatThrownBy(() -> memory.putInt(2, 2)).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    public void testDealloc() {
        Set<AllocatedMemory> set = Collections.newSetFromMap(new WeakHashMap<>());
        for (int i = 0; i < 100; ++i) {
            set.add(AllocatedMemory.allocate(10));
        }
        assertTrue(SleepUtil.sleepUntil(set::isEmpty));
    }

}
