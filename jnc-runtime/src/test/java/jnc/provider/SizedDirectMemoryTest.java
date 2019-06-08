package jnc.provider;

import jnc.foreign.Pointer;
import static org.assertj.core.api.Assertions.assertThat;
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
        Pointer instance = AllocatedMemory.allocate(8);

        assertThatThrownBy(() -> instance.putStringUTF(0, "abcdefgh"))
                .isInstanceOf(IndexOutOfBoundsException.class);

        assertThatThrownBy(() -> instance.putStringUTF(-1, "a"))
                .isInstanceOf(IndexOutOfBoundsException.class);

        assertThatThrownBy(() -> instance.putStringUTF(2, "abcdef"))
                .isInstanceOf(IndexOutOfBoundsException.class);
        instance.putStringUTF(2, "abcde");

        instance.putLong(0, 0x2020202020202020L);

        byte space = 32;
        instance.putStringUTF(1, "a\u0000cd");
        assertEquals(space, instance.getByte(0));
        assertEquals('a', instance.getByte(1));
        // NOTE putStringUTF is modified UTF-8
        assertEquals(-64, instance.getByte(2));
        assertEquals(-128, instance.getByte(3));
        assertEquals('c', instance.getByte(4));
        assertEquals('d', instance.getByte(5));
        assertEquals(0, instance.getByte(6));
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
        Pointer instance = AllocatedMemory.allocate(8);
        Pointer slice = instance.slice(0, 6);
        assertThat(instance.getStringUTF(offset)).isEmpty();
        instance.putLong(offset, 0x2020202020202020L);
        assertThat(instance.getStringUTF(offset)).isEqualTo("        ");
        assertThat(slice.address()).isEqualTo(instance.address());
        assertThat(slice.getStringUTF(offset)).isEqualTo("      ");
        assertThat(slice.slice(1, 6).getStringUTF(offset)).isEqualTo("     ");
    }

}
