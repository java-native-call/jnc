package jnc.foreign;

import jnc.foreign.annotation.Pack;
import static org.junit.Assert.*;
import org.junit.Test;

public class UnionTest {

    @Test
    public void testSize() {
        int int64Align = Foreign.getDefault().findType(NativeType.SINT64).alignment();
        // https://en.wikipedia.org/wiki/Data_structure_alignment#Typical_alignment_of_C_structs_on_x86
        // https://msdn.microsoft.com/en-us/library/45t0s5f4.aspx
        switch (int64Align) {
            case 4:
                expectSizeAndAlign(new Test1(), 8, 4);
                expectSizeAndAlign(new Test2(), 20, 4);
                expectSizeAndAlign(new Test3(), 20, 4);
                expectSizeAndAlign(new Test4(), 20, 4);
                break;
            case 8:
                expectSizeAndAlign(new Test1(), 8, 8);
                expectSizeAndAlign(new Test2(), 24, 8);
                expectSizeAndAlign(new Test3(), 24, 8);
                expectSizeAndAlign(new Test4(), 20, 4);
                break;
            default:
                throw new AssertionError();
        }
        Test1 test1 = new Test1();
        long magic = 0x0102030405060708L;
        test1.setJ(magic);
        assertEquals(Double.longBitsToDouble(magic), test1.getD(), -1);
    }

    private void expectSizeAndAlign(Struct test1, int size, int align) {
        String name = test1.getClass().getSimpleName();
        assertEquals("sizeof(" + name + ")", size, test1.size());
        assertEquals("alignof(" + name + ")", align, test1.alignment());
    }

    @SuppressWarnings("unused")
    private static class Test1 extends Union {

        private final uint8_t[] b = array(new uint8_t[8]);
        private final uint16_t[] s = array(new uint16_t[4]);
        private final uint32_t[] u = array(new uint32_t[2]);
        private final int64_t j = new int64_t();
        private final uint64_t ju = new uint64_t();
        private final Float64 d = new Float64();

        private double getD() {
            return d.get();
        }

        private void setJ(long value) {
            j.set(value);
        }

    }

    @SuppressWarnings("unused")
    private static class Test2 extends Union {

        private final uint8_t[] b = array(new uint8_t[18]);
        private final uint64_t ju = new uint64_t();

    }

    @SuppressWarnings("unused")
    private static class Test3 extends Union {

        private final uint64_t ju = new uint64_t();
        private final uint8_t[] b = array(new uint8_t[17]);

    }

    @Pack(4)
    private static class Test4 extends Test2 {
    }

}
