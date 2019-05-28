package jnc.foreign.internal;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SizedDirectMemoryTest {

    private static final Logger log = LoggerFactory.getLogger(SizedDirectMemoryTest.class);

    /**
     * Test of putStringUTF method, of class SizedDirectMemory.
     */
    @Test
    public void testPutStringUTF() {
        log.info("putStringUTF");
        byte space = 32;
        SizedDirectMemory instance = AllocatedMemory.allocate(8);
        instance.putLong(0, 0x2020202020202020L);

        assertThatThrownBy(() -> instance.putStringUTF(0, "abcdefgh"))
                .isInstanceOf(IndexOutOfBoundsException.class);

        assertThatThrownBy(() -> instance.putStringUTF(-1, "a"))
                .isInstanceOf(IndexOutOfBoundsException.class);

        assertThatThrownBy(() -> instance.putStringUTF(2, "abcdef"))
                .isInstanceOf(IndexOutOfBoundsException.class);

        instance.putStringUTF(1, "a\u0000cd");
        assertEquals(space, instance.getByte(0));
        assertEquals('a', instance.getByte(1));
        assertEquals(0, instance.getByte(2));
        assertEquals('c', instance.getByte(3));
        assertEquals('d', instance.getByte(4));
        assertEquals(0, instance.getByte(5));
        assertEquals(space, instance.getByte(6));
        assertEquals(space, instance.getByte(7));

        assertThatThrownBy(() -> instance.getByte(8))
                .isInstanceOf(IndexOutOfBoundsException.class);
    }

    /**
     * Test of getStringUTF method, of class SizedDirectMemory.
     */
    @Test
    public void testGetStringUTF() {
        log.info("getStringUTF");
        int offset = 0;
        SizedDirectMemory instance = AllocatedMemory.allocate(8);
        assertEquals("", instance.getStringUTF(offset));
        instance.putLong(offset, 0x2020202020202020L);
        assertEquals("        ", instance.getStringUTF(offset));
    }

}
