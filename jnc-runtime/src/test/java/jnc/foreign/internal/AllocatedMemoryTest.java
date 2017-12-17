package jnc.foreign.internal;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Test;

@SuppressWarnings("ResultOfObjectAllocationIgnored")
public class AllocatedMemoryTest {

    @Test
    public void testIllegalArgument() {
        try {
            AllocatedMemory.allocate(-1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
        }
        try {
            AllocatedMemory.allocate(-1, 1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
        }
        try {
            AllocatedMemory.allocate(1, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
        }
        try {
            AllocatedMemory.allocate(-1, -1);
            fail("should throw IllegalArgumentException");
        } catch (IllegalArgumentException ex) {
        }
        AllocatedMemory.allocate(0, 0);
    }

    @Test
    public void testOutOfMemory() {
        try {
            AllocatedMemory.allocate(Long.MAX_VALUE);
            fail("should throw OutOfMemoryError");
        } catch (OutOfMemoryError ex) {
        }
        try {
            AllocatedMemory.allocate(Integer.MAX_VALUE, Integer.MAX_VALUE);
            fail("should throw OutOfMemoryError");
        } catch (OutOfMemoryError ex) {
        }
    }

    @Test
    public void testIndexOfRange() {
        try {
            AllocatedMemory.allocate(3).putInt(2, 2);
            fail("should throw IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException ex) {
            // ok
        }
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
            for (int i = 0; i < 9 && !set.isEmpty(); ++i) {
                try {
                    Thread.sleep(sleep);
                } catch (InterruptedException ex) {
                    interrupted = true;
                }
                sleep <<= 1;
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
        assertEquals(0, set.size());
    }

}
