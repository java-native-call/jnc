package jnc.foreign.internal;

import java.lang.reflect.Field;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.stream.Collectors;
import jnc.foreign.Pointer;
import jnc.foreign.TestLibs;
import jnc.foreign.enums.CallingConvention;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.AssumptionViolatedException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NativeMethodsTest {

    private static final Logger log = LoggerFactory.getLogger(NativeMethodsTest.class);
    private static final String LIBC = DefaultPlatform.INSTANCE.getLibcName();
    private static final String LIBM = TestLibs.getStandardMath();
    private static final NativeAccessor NA = NativeLoader.getAccessor();

    /**
     * Test of getJniVersion method, of class NativeMethods.
     */
    @Test
    public void testGetJniVersion() {
        log.info("getJniVersion");
        int expResult = 0x10006;
        int result = NA.getJniVersion();
        assertTrue(result >= expResult);
    }

    @Test
    public void testNotFound() {
        log.info("test not found");
        String path = System.mapLibraryName("not_exists_lib");
        assertThatThrownBy(() -> NativeLibrary.open(path, 0))
                .isInstanceOf(UnsatisfiedLinkError.class)
                .matches(ex -> ex.getMessage().length() > 0, "message won't be empty");

        Library libm = NativeLibrary.open(LIBM, 0);
        assertThatThrownBy(() -> libm.dlsym("not_exists_function"))
                .isInstanceOf(UnsatisfiedLinkError.class)
                .matches(ex -> ex.getMessage().length() > 0, "message won't be empty");
    }

    @Test
    public void testNullPointer() {
        log.info("test null pointer");
        assertThatThrownBy(() -> NA.dlsym(0, "not_exists_function"))
                .isInstanceOf(NullPointerException.class);
        Library libm = NativeLibrary.open(LIBM, 0);
        assertThatThrownBy(() -> libm.dlsym(null))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> NA.dlclose(0))
                .isInstanceOf(NullPointerException.class);
    }

    @Test
    public void testDlopen() {
        for (int i = 0; i < 10; ++i) {
            NativeLibrary.open(null, 0);
        }
    }

    @Test
    public void testInitAlias() {
        log.info("initAlias");
        assertThatThrownBy(() -> NA.initAlias(null))
                .isInstanceOf(NullPointerException.class);
        HashMap<String, Integer> map = new HashMap<>(50);
        NA.initAlias(map);
        log.info("map={}", map);
        assertEquals(Integer.valueOf(BuiltinType.UINT8.type()), map.get("uint8_t"));
        assertEquals(Integer.valueOf(BuiltinType.SINT32.type()), map.get("int32_t"));
    }

    @Test
    public void testStringUTF() {
        Pointer hello = AllocatedMemory.allocate(10);
        String str = "hello!123";
        hello.putStringUTF(0, str);
        assertEquals(str, hello.getStringUTF(0));
    }

    @Test
    public void testFfi_call() {
        Library lib = NativeLibrary.open(LIBC, 0);
        long toupper = lib.dlsym("toupper");
        CifContainer container = CifContainer.create(CallingConvention.DEFAULT, BuiltinType.SINT32, BuiltinType.SINT32);
        CallContext context = container.newCallContext();
        int param = 'a';
        context.putInt(0, param);
        long result = context.invoke(Invokers::invokeInt, toupper);
        log.info("result = " + result);
        assertEquals('A', result);
    }

    /**
     * Test of allocateMemory method, of class NativeMethods.
     */
    @Test
    public void testAllocateMemory() {
        log.info("allocateMemory");
        long size = 1000;
        for (int i = 0; i < 10000000; ++i) {
            long addr = NA.allocateMemory(size);
            NA.freeMemory(addr);
        }
    }

    /**
     * Test of freeMemory method, of class NativeMethods.
     */
    @Test
    public void testFreeMemory() {
        log.info("freeMemory");
        NA.freeMemory(0);
    }

    /**
     * Test of pageSize method, of class NativeMethods.
     */
    @Test
    @SuppressWarnings({"UseSpecificCatch", "BroadCatchBlock", "TooBroadCatch"})
    public void testPageSize() {
        int expResult;
        try {
            Field field = Class.forName("sun.misc.Unsafe").getDeclaredField("theUnsafe");
            field.setAccessible(true);
            Object unsafe = field.get(null);
            expResult = ((Number) unsafe.getClass().getMethod("pageSize").invoke(unsafe)).intValue();
        } catch (Throwable t) {
            throw new AssumptionViolatedException("unsafe not present", t);
        }
        log.info("pageSize");
        int result = NA.pageSize();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetStringUTFEmpty() {
        long address = NA.allocateMemory(0);
        try {
            String string = NA.getStringUTF(address, Long.MAX_VALUE);
            assertEquals("", string);
        } finally {
            NA.freeMemory(address);
        }
    }

    private String str2Hex(String str) {
        return str.chars().mapToObj(x -> String.format("%04x", x)).collect(Collectors.joining(""));
    }

    private void assertHexEquals(String message, String expect, String result) {
        String expectHex = str2Hex(expect);
        String resultHex = str2Hex(result);
        assertEquals(message, expectHex, resultHex);
    }

    /**
     * Test of putStringChar16/getStringChar16 methods, of class NativeMethods.
     */
    @Test
    public void testStringChar16() {
        log.info("stringChar16");
        Pointer memory = AllocatedMemory.allocate(40);
        long address = memory.address();
        String value = "\u0102\u0304\u0506\u0708\u0000\u0807\u0000";

        assertThatThrownBy(() -> NA.putStringChar16(address, null))
                .isInstanceOf(NullPointerException.class);

        assertThatThrownBy(() -> NA.putStringChar16(0, value))
                .isInstanceOf(NullPointerException.class);

        NA.putStringChar16(address, value);

        char[] arr1 = new char[4], arr2 = "\u0102\u0304\u0506\u0708".toCharArray();
        memory.getCharArray(0, arr1, 0, arr1.length);
        assertArrayEquals(arr2, arr1);

        assertHexEquals("aligned access", "\u0102\u0304\u0506\u0708", NA.getStringChar16(address, Long.MAX_VALUE));
        String expect;
        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            expect = "\u0203\u0405\u0607\u0800\u0008\u0700";
        } else {
            expect = "\u0401\u0603\u0805\u0007\u0700\u0008";
        }
        assertHexEquals("unaligned access", expect, NA.getStringChar16(address + 1, Long.MAX_VALUE));

        assertHexEquals("aligned access with limit", "\u0102\u0304\u0506\u0708", NA.getStringChar16(address, 8));
        assertHexEquals("aligned access with limit", "\u0102\u0304\u0506", NA.getStringChar16(address, 7));
        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            expect = "\u0203\u0405\u0607\u0800\u0008\u0700";
        } else {
            expect = "\u0401\u0603\u0805\u0007\u0700\u0008";
        }
        assertHexEquals("unaligned access with limit", expect, NA.getStringChar16(address + 1, 12));
        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            expect = "\u0203\u0405\u0607\u0800\u0008";
        } else {
            expect = "\u0401\u0603\u0805\u0007\u0700";
        }
        assertHexEquals("unaligned access with limit", expect, NA.getStringChar16(address + 1, 11));
    }

    /**
     * Test of getStringUTFLength method, of class NativeMethods.
     */
    @Test
    public void testGetStringUTFLength() {
        log.info("getStringUTFLength");
        assertThatThrownBy(() -> NA.getStringUTFLength(null))
                .isInstanceOf(NullPointerException.class);
        assertThat(NA.getStringUTFLength("")).isEqualTo(0);
        assertThat(NA.getStringUTFLength("abcdef")).isEqualTo(6);
        assertThat(NA.getStringUTFLength("\u0000")).isEqualTo(2);
    }

    /**
     * Test of getStringLength method, of class NativeMethods.
     */
    @Test
    public void testGetStringLength() {
        log.info("getStringLength");
        // address, limit, terminatorLength
        // terminatorLength must be 1,2,4
        assertThatThrownBy(() -> NA.getStringLength(0, 0, 1))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> NA.getStringLength(1, -2, 1))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> NA.getStringLength(1, 0, 3))
                .isInstanceOf(IllegalArgumentException.class);

        Pointer pointer = AllocatedMemory.allocate(20);
        pointer.putLong(0, 0x2020202020202020L);
        for (int terminatorLength : new int[]{1, 2, 4}) {
            assertThat(NA.getStringLength(pointer.address(), 0, terminatorLength))
                    .describedAs("terminatorLength=%s", terminatorLength)
                    .isEqualTo(0);
            assertThat(NA.getStringLength(pointer.address(), Long.MAX_VALUE, terminatorLength))
                    .describedAs("terminatorLength=%s", terminatorLength)
                    .isEqualTo(8 / terminatorLength);
            assertThat(NA.getStringLength(pointer.address(), 4, terminatorLength))
                    .describedAs("terminatorLength=%s", terminatorLength)
                    .isEqualTo(4 / terminatorLength);
        }
    }

}
