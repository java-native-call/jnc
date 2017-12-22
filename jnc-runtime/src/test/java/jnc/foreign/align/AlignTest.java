package jnc.foreign.align;

import jnc.foreign.Struct;
import jnc.foreign.annotation.Pack;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

public class AlignTest {

    void assertSizeAndAlign(Struct struct, int size, int alignment) {
        String msg = struct.getClass().getSimpleName();
        assertEquals("sizeof(" + msg + ")", size, struct.size());
        assertEquals("alignof(" + msg + ")", alignment, struct.alignment());
    }

    @Test
    public void testAlign() {
        assertSizeAndAlign(new jnc.foreign.align.align2.Sample(), 6, 2);
        assertSizeAndAlign(new jnc.foreign.align.align4.Sample(), 8, 4);
    }

    @Test
    public void testAlign2() {
        assertSizeAndAlign(new my_unpacked_struct(), 8, 4);
        assertSizeAndAlign(new my_packed_struct(), 13, 1);
    }

    @Test
    public void testAlign0() {
        assertSizeAndAlign(new pack_zero(), 8, 4);
    }

    private static class my_unpacked_struct extends Struct {

        private final uint8_t c = new uint8_t();
        private final int32_t i = new int32_t();

    }

    @Pack(1)
    private static class my_packed_struct extends Struct {

        private final uint8_t c = new uint8_t();
        private final int32_t i = new int32_t();
        private final my_unpacked_struct s = inner(new my_unpacked_struct());

    }

    @Pack(0)
    private static class pack_zero extends my_unpacked_struct {
    }

}
