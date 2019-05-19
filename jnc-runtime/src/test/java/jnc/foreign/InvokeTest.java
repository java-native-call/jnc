package jnc.foreign;

import jnc.foreign.annotation.Continuously;
import jnc.foreign.typedef.int32_t;
import jnc.foreign.typedef.size_t;
import jnc.foreign.typedef.uintptr_t;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

@SuppressWarnings("PublicInnerClass")
public class InvokeTest {

    @Test
    public void testInvoke() {
        Libc.INSTANCE.memcpy(0, 0, 0);
        Libc.INSTANCE.memcpy(0, 0, 0);
        assertTrue(Libc.INSTANCE.equals(Libc.INSTANCE));
        Libc.INSTANCE.hashCode();
        Libc.INSTANCE.toString();
        assertEquals(Math.sqrt(5), Libm.INSTANCE.sqrt(5), -1);
        assertEquals(Math.PI, Libm.INSTANCE.atan2(0, -1), -1);
        assertEquals(Math.PI / 2, Libm.INSTANCE.atan2(1, 0), 1e-14);
    }

    @Test
    public void testDefaultMethod() {
        assertEquals(0x123456, Libc.INSTANCE.memcpy());
    }

    @Test
    public void testVoid() {
        Libc.INSTANCE.memcpy(null, 0, 0);
    }

    @Test
    public void testEnumParameter() {
        assertTrue(Libc.INSTANCE.isalpha(Char.A));
    }

    @Test
    public void testEnumReturnValue() {
        assertEquals(Char.A, Libc.INSTANCE.toupper('a'));
    }

    @Continuously(start = 'A')
    public enum Char {
        A, B
    }

    public interface Libc {

        Libc INSTANCE = LibraryLoader.create(Libc.class).load(Platform.getNativePlatform().getLibcName());

        @uintptr_t
        long memcpy(@uintptr_t long dst, @uintptr_t long src, @size_t long n);

        @uintptr_t
        void memcpy(Void dst, @uintptr_t long src, @size_t long n);

        @int32_t
        boolean isalpha(Char ch);

        Char toupper(int ch);

        default int memcpy() {
            return 0x123456;
        }

        @Override
        int hashCode();

        @Override
        boolean equals(Object obj);

    }

    public interface Libm {

        Libm INSTANCE = LibraryLoader.create(Libm.class).load(TestLibs.getStandardMath());

        double sqrt(double i);

        double atan2(double x, double y);

    }

}
