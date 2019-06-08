package jnc.provider;

import java.nio.ByteOrder;
import jnc.foreign.Pointer;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class MemoryTest {

    private static final boolean LITTLE_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;

    /**
     * Test of address method, of class Memory.
     */
    @Test
    public void testUnalignedGet() {
        int size = 64;
        Pointer memory = AllocatedMemory.allocate(size);
        for (int i = 0; i < size; ++i) {
            memory.putByte(i, (byte) ((i & 7) + 1));
        }
        long MAGIC0 = LITTLE_ENDIAN ? 0x0807060504030201L : 0x0102030405060708L;
        for (int off = 0; off < 8; ++off) {
            long MAGIC = LITTLE_ENDIAN ? Long.rotateRight(MAGIC0, off << 3) : Long.rotateLeft(MAGIC0, off << 3);
            assertEquals(MAGIC, memory.getLong(off));
            for (int i = 0; i < 6; ++i) {
                long[] array = new long[i];
                memory.getLongArray(off, array, 0, i);
                for (int j = 0; j < i; ++j) {
                    assertEquals(MAGIC, array[j]);
                }
            }
            assertEquals(MAGIC, Double.doubleToLongBits(memory.getDouble(off)), -1);
            if (LITTLE_ENDIAN) {
                assertEquals((int) MAGIC, Float.floatToIntBits(memory.getFloat(off)));
            } else {
                assertEquals((int) (MAGIC >> 8), Float.floatToIntBits(memory.getFloat(off)));
            }
        }
    }

}
