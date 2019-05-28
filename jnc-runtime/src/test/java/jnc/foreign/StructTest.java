package jnc.foreign;

import java.io.PrintStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import jnc.foreign.annotation.Continuously;
import jnc.foreign.enums.TypeAlias;
import jnc.foreign.typedef.size_t;
import jnc.foreign.typedef.uint32_t;
import jnc.foreign.typedef.uintptr_t;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"PublicInnerClass", "UnusedReturnValue", "WeakerAccess", "unused", "SameParameterValue"})
public class StructTest {

    private static final Logger log = LoggerFactory.getLogger(StructTest.class);

    public static void main(String[] args) {
        Class<?>[] declaredClasses = Struct.class.getDeclaredClasses();
        List<Class<?>> list = new ArrayList<>(declaredClasses.length);
        for (Class<?> inner : declaredClasses) {
            if (!inner.isAnonymousClass() && (inner.getModifiers() & (Modifier.PRIVATE | Modifier.STATIC)) == 0) {
                list.add(inner);
            }
        }
        PrintStream out = System.out;
        for (int i = list.size() - 1; i >= 0; --i) {
            String s = list.get(i).getSimpleName();
            out.println("@Nonnull");
            out.println("protected final " + s + "[] array(@Nonnull " + s + "[] array) {");
            out.println("    return memberArray(array, struct -> struct.new " + s + "());");
            out.println("}");
        }
    }

    /**
     * Test of size method, of class Struct.
     */
    @Test
    public void testSize() {
        log.info("size");
        SizeTStruct instance = new SizeTStruct();
        SizeTStruct tmp = new SizeTStruct();
        Type expResult = ForeignProviders.getDefault().findType(TypeAlias.size_t);
        final long mask;
        switch (expResult.size()) {
            case 1:
                mask = 0xFFL;
                break;
            case 2:
                mask = 0xFFFFL;
                break;
            case 4:
                mask = 0xFFFFFFFFL;
                break;
            case 8:
                mask = -1;
                break;
            default:
                throw new AssertionError();
        }
        assertEquals(expResult.size(), instance.size());
        assertEquals(expResult.alignment(), instance.alignment());
        long MAGIC = 0x0807060504030201L;
        instance.setValue(MAGIC);
        assertEquals(0, tmp.getValue());
        Libc.INSTANCE.memcpy(tmp, instance, instance.size());
        assertEquals(MAGIC & mask, tmp.getValue());
        Libc.INSTANCE.memset(tmp, (byte) 0, tmp.size());
        assertEquals(0, tmp.getValue());
        Libc.INSTANCE.memcpy(tmp, instance.getMemory(), instance.size());
        assertEquals(MAGIC & mask, tmp.getValue());
        assertNull(Libc.INSTANCE.memcpy((Pointer) null, (Pointer) null, 0));
    }

    /**
     * Test of inner method, of class Struct.
     */
    @Test
    public void testInner() {
        log.info("inner");
        Wrapper wrapper = new Wrapper();
        wrapper.inner.getA().setValue(1);
        wrapper.inner.getA().setSuffix((byte) 2);
        wrapper.inner.getB().setValue(3);
        wrapper.inner.getB().setSuffix((byte) 4);
        assertEquals(1, wrapper.getMemory().getInt(4));
        assertEquals(2, wrapper.getMemory().getByte(8));
        assertEquals(3, wrapper.getMemory().getInt(12));
        assertEquals(4, wrapper.getMemory().getByte(16));
    }

    @Test
    public void testUint64() {
        Uint64Struct uint64Struct = new Uint64Struct();
        uint64Struct.setValue(-1);
        // double has only have 52 bit fraction, so the result is 2^64
        assertEquals(0x1.0p64, uint64Struct.doubleValue(), -1);
    }

    @Test
    public void testAdvance() {
        Struct struct = new Struct();
        struct.size();
        assertThatThrownBy(() -> struct.new size_t())
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    public void testEnum() {
        Struct1 struct1 = new Struct1();
        assertEquals(1, struct1.size());
        struct1.setSeason(Season1.Winter);
        assertEquals(Season1.Winter, struct1.getSeason());

        Struct2 struct2 = new Struct2();
        assertEquals(4, struct2.size());
        struct2.setSeason(Season2.Winter);
        assertEquals(Season2.Winter, struct2.getSeason());
    }

    @Test
    public void testConvertFromDouble() {
        DoubleStruct struct = new DoubleStruct();
        assertEquals(8, struct.size());
        struct.setValue(0.0);
        assertFalse(struct.getAsBoolean());
        struct.setValue(Double.NaN);
        assertTrue("Double NaN converted to boolean, expect true, but was false", struct.getAsBoolean());
        struct.setValue(Long.MAX_VALUE);
        assertEquals(Long.MIN_VALUE, struct.getAsLong()); // value truncated
    }

    @Test
    public void testStructSizeTooLarge() {
        assertThatThrownBy(() -> new Struct() {
            {
                padding(Integer.MAX_VALUE);
            }
        }).isInstanceOf(OutOfMemoryError.class);
    }

    @Test
    public void testIllegalSize() {
        assertThatThrownBy(() -> new Struct() {
            {
                padding(Integer.MIN_VALUE);
            }
        }).isInstanceOf(IllegalArgumentException.class);
    }

    private static class Wrapper extends Struct {

        final int8_t x = new int8_t();
        final StructWIthInner inner = inner(new StructWIthInner());

    }

    private static class StructWIthInner extends Struct {

        private final Inner a = inner(new Inner());

        private final Inner b = inner(new Inner());

        public Inner getA() {
            return a;
        }

        public Inner getB() {
            return b;
        }

    }

    private static class Inner extends Struct {

        private final int32_t value = new int32_t();
        private final int8_t suffix = new int8_t();

        public int getValue() {
            return value.get();
        }

        public void setValue(int l) {
            value.set(l);
        }

        public byte getSuffix() {
            return suffix.get();
        }

        public void setSuffix(byte l) {
            suffix.set(l);
        }

    }

    private static class SizeTStruct extends Struct {

        private final size_t value = new size_t();

        public long getValue() {
            return value.get();
        }

        private void setValue(long l) {
            value.set(l);
        }

    }

    private static class Uint64Struct extends Struct {

        private final uint64_t value = new uint64_t();

        public long getValue() {
            return value.get();
        }

        private void setValue(long l) {
            value.set(l);
        }

        private double doubleValue() {
            return value.doubleValue();
        }

    }

    private static class Struct1 extends Struct {

        private final EnumField<Season1> season = enumField(Season1.class);

        public Season1 getSeason() {
            return season.get();
        }

        public void setSeason(Season1 field) {
            this.season.set(field);
        }

    }

    private static class Struct2 extends Struct {

        private final EnumField<Season2> season = enumField(Season2.class);

        public Season2 getSeason() {
            return season.get();
        }

        public void setSeason(Season2 field) {
            this.season.set(field);
        }

    }

    private static class DoubleStruct extends Struct {

        Float64 d = new Float64();

        public boolean getAsBoolean() {
            return d.booleanValue();
        }

        public long getAsLong() {
            return d.longValue();
        }

        public void setValue(double v) {
            d.set(v);
        }

    }

    @SuppressWarnings("PublicInnerClass")
    public interface Libc {

        Libc INSTANCE = LibraryLoader.create(Libc.class).load(Platform.getNativePlatform().getLibcName());

        @uintptr_t
        long memcpy(Struct dst, Struct src, @size_t long n);

        @uintptr_t
        void memset(Struct dst, @uint32_t byte value, @size_t long n);

        Pointer memcpy(Struct dst, Pointer src, @size_t long n);

        Pointer memcpy(Pointer dst, Pointer src, @size_t long n);

        @uintptr_t
        long memcpy(Pointer dst, Struct src, @size_t long n);

    }

    @Continuously(type = NativeType.SINT8)
    private enum Season1 {
        Spring, Summer, Autumn, Winter
    }

    private enum Season2 {
        Spring, Summer, Autumn, Winter
    }

}
