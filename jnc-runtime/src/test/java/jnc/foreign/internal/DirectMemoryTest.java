package jnc.foreign.internal;

import java.nio.ByteOrder;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class DirectMemoryTest {

    private static final boolean LITTLE_ENDIAN = ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN;

    /**
     * Test of address method, of class DirectMemory.
     */
    @Test
    public void testUnalignedGet() {
        DirectMemory memory = AllocatedMemory.allocate(16);
        int off = 3;
        long MAGIC = 0x0102030405060708L;
        memory.putLong(off, MAGIC);
        assertEquals(MAGIC, memory.getLong(off));
        long[] array = new long[2];
        memory.getLongArray(off, array, 1, 1);
        assertEquals(0, array[0]);
        assertEquals(MAGIC, array[1]);
        assertEquals(Double.longBitsToDouble(MAGIC), memory.getDouble(off), -1);
        if (LITTLE_ENDIAN) {
            assertEquals(Float.intBitsToFloat((int) MAGIC), memory.getFloat(off), -1);
        } else {
            assertEquals(Float.intBitsToFloat((int) (MAGIC >> 8)), memory.getFloat(off), -1);
        }
        for (int i = 0; i < off; ++i) {
            assertEquals(0, memory.getByte(i));
        }
        if (LITTLE_ENDIAN) {
            for (int i = off; i < off + 8; ++i) {
                assertEquals(8 - (i - off), memory.getByte(i));
            }
        } else {
            for (int i = off; i < off + 8; ++i) {
                assertEquals(i - off + 1, memory.getByte(i));
            }
        }
        for (int i = off + 8; i < 16; ++i) {
            assertEquals(0, memory.getByte(i));
        }
    }

}
