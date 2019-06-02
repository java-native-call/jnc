package jnc.foreign;

import jnc.foreign.annotation.Pack;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

@SuppressWarnings("unused")
public class AlignTest {

    private void assertSizeAndAlign(Struct struct, int size, int alignment) {
        String msg = struct.getClass().getSimpleName();
        assertEquals("sizeof(" + msg + ")", size, struct.size());
        assertEquals("alignof(" + msg + ")", alignment, struct.alignment());
    }

    @Test
    public void testAlign() {
        assertSizeAndAlign(new Aligned2(), 6, 2);
        assertSizeAndAlign(new Aligned4(), 8, 4);
    }

    @Test
    public void testAlign2() {
        assertSizeAndAlign(new Base(), 8, 4);
        assertSizeAndAlign(new my_packed_struct(), 13, 1);
    }

    @Test
    public void testAlign0() {
        assertSizeAndAlign(new PackZero(), 8, 4);
    }

    private static class Base extends Struct {

        private final uint8_t c = new uint8_t();
        private final int32_t i = new int32_t();

    }

    private static class Base2 extends Struct {

        private final Struct.int32_t i = new int32_t();
        private final Struct.int8_t c = new int8_t();

    }

    @Pack(2)
    private static class Aligned2 extends Base2 {
    }

    @Pack(4)
    private static class Aligned4 extends Base2 {
    }

    @Pack(1)
    private static class my_packed_struct extends Base {

        private final Base s = inner(new Base());
    }

    @Pack(0)
    // extends Base2 to make sure parent's @Pack is cleared.
    private static class PackZero extends Aligned2 {
    }

}
