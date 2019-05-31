package jnc.foreign;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.atomic.AtomicBoolean;
import jnc.foreign.byref.IntByReference;
import jnc.foreign.typedef.size_t;
import jnc.foreign.typedef.uintptr_t;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

@SuppressWarnings("PublicInnerClass")
public class InvokeTest {

    @Test
    public void testInvoke() {
        Libc.INSTANCE.memcpy(0, 0, 0);
        Libc.INSTANCE.memcpy(0, 0, 0);
        assertEquals(Math.sqrt(5), Libm.INSTANCE.sqrt(5), -1);
        assertEquals(Math.PI, Libm.INSTANCE.atan2(0, -1), -1);
        assertEquals(Math.PI / 2, Libm.INSTANCE.atan2(1, 0), 1e-14);
    }

    @Test
    public void testByReference() {
        Struct1 struct1 = new Struct1();
        struct1.setValue(123456);
        IntByReference reference = new IntByReference();
        assertEquals(0, reference.getValue());
        Libc.INSTANCE.memcpy(reference, struct1, 4);
        Libc.INSTANCE.memcpy(reference, (Struct) struct1, 4);
    }

    @Test
    public void testPrimitiveArray() {
        byte[] bytes = "abcde".getBytes(StandardCharsets.UTF_8);
        Pointer memory = Foreign.getDefault().getMemoryManager().allocate(20);
        assertEquals(memory.address(), Libc.INSTANCE.memcpy(memory, bytes, 3).address());
        assertEquals("abc", memory.getStringUTF(0));

        Libc.INSTANCE.memcpy(bytes, memory, 4);
        byte[] expect = "abc\000e".getBytes(StandardCharsets.UTF_8);
        assertArrayEquals(expect, bytes);

        boolean[] bools = new boolean[4];
        boolean[] expectBooleans = {true, true, true, false};
        Libc.INSTANCE.memcpy(bools, bytes, 4);
        assertArrayEquals(expectBooleans, bools);
    }

    @Test
    public void testDefaultMethod() {
        AtomicBoolean bool = new AtomicBoolean();
        assertEquals(0x123456, Libc.INSTANCE.memcpy(bool));
        assertTrue(bool.get());
    }

    @Test
    public void testVoid() {
        Libc.INSTANCE.memcpy(null, 0, 0);
    }

    private static class Struct1 extends Struct {

        private final int32_t value = new int32_t();

        public void setValue(int i) {
            value.set(i);
        }

        public int getValue() {
            return value.get();
        }
    }

    public interface Libc {

        Libc INSTANCE = LibraryLoader.create(Libc.class).load(Platform.getNativePlatform().getLibcName());

        @uintptr_t
        long memcpy(@uintptr_t long dst, @uintptr_t long src, @size_t long n);

        @uintptr_t
        long memcpy(IntByReference dst, Struct1 src, @size_t long n);

        @uintptr_t
        long memcpy(IntByReference dst, Struct src, @size_t long n);

        @uintptr_t
        void memcpy(Void dst, @uintptr_t long src, @size_t long n);

        @uintptr_t
        long memcpy(byte[] dst, Pointer src, @size_t long n);

        @uintptr_t
        long memcpy(boolean[] dst, byte[] src, @size_t long n);

        Pointer memcpy(Pointer dst, byte[] src, @size_t long n);

        default int memcpy(AtomicBoolean atomic) {
            atomic.set(true);
            return 0x123456;
        }

    }

    public interface Libm {

        Libm INSTANCE = LibraryLoader.create(Libm.class).load(TestLibs.getStandardMath());

        double sqrt(double i);

        double atan2(double x, double y);

    }

}
