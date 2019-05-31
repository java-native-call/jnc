package jnc.foreign.internal;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
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
        AllocatedMemory memory = AllocatedMemory.allocate(3);
        assertThatThrownBy(() -> memory.putInt(2, 2)).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    @SuppressWarnings("SleepWhileInLoop")
    public void testDealloc() {
        Set<AllocatedMemory> set = Collections.newSetFromMap(new WeakHashMap<>());
        for (int i = 0; i < 100; ++i) {
            set.add(AllocatedMemory.allocate(10));
        }
        System.gc();
        boolean interrupted = false;
        try {
            int sleep = 1;
            for (int i = 0; i < 12 && !set.isEmpty(); ++i) {
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException ex) {
                    interrupted = true;
                }
                sleep <<= 1;
                if (i > 8) {
                    System.gc();
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
        assertEquals(0, set.size());
    }

}
