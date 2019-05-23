package jnc.foreign.internal;

import java.lang.reflect.Field;
import java.nio.ByteOrder;
import java.util.HashMap;
import java.util.stream.Collectors;
import jnc.foreign.TestLibs;
import jnc.foreign.abi.CallingMode;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.AssumptionViolatedException;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NativeMethodsTest {

    private static final Logger log = LoggerFactory.getLogger(NativeMethodsTest.class);
    private static final String LIBC = DefaultPlatform.getInstance().getLibcName();
    private static final String LIBM = TestLibs.getStandardMath();
    private static final NativeMethods nm = NativeMethods.getInstance();

    /**
     * Test of getTypeInfo method, of class NativeMethods.
     */
    @Test(expected = NullPointerException.class)
    public void testGetTypeInfo() {
        log.info("getTypeInfo");
        long result = nm.getTypeInfo(0);
    }

    /**
     * Test of getJniVersion method, of class NativeMethods.
     */
    @Test
    public void testGetJniVersion() {
        log.info("getJniVersion");
        int expResult = 0x10006;
        int result = nm.getJniVersion();
        assertTrue(result >= expResult);
    }

    @Test
    public void testNotFound() {
        log.info("test not found");
        String path = System.mapLibraryName("not_exists_lib");
        try {
            NativeLibrary.open(path, 0);
            fail("should throw a UnsatisfiedLinkError");
        } catch (UnsatisfiedLinkError ex) {
            log.info(ex.getMessage());
            assertNotEquals(0, ex.getMessage().length());
        }
        NativeLibrary libm = NativeLibrary.open(LIBM, 0);
        try {
            libm.dlsym("not_exists_function");
            fail("should throw a UnsatisfiedLinkError");
        } catch (UnsatisfiedLinkError ex) {
            log.info(ex.getMessage());
            assertNotEquals(0, ex.getMessage().length());
        }
    }

    @Test
    public void testNullPointer() {
        log.info("test null pointer");
        try {
            nm.dlsym(0, "not_exists_function");
            fail("should throw a NullPointerException");
        } catch (NullPointerException ex) {
            // ok
        }
        NativeLibrary libm = NativeLibrary.open(LIBM, 0);
        try {
            libm.dlsym(null);
            fail("should throw a NullPointerException");
        } catch (NullPointerException ex) {
            // ok
        }
        try {
            nm.dlclose(0);
            fail("should throw a NullPointerException");
        } catch (NullPointerException ex) {
            // ok
        }
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
        try {
            nm.initAlias(null);
            fail("should throw NullPointerException");
        } catch (NullPointerException ex) {
            // ok
        }
        HashMap<String, Integer> map = new HashMap<>(50);
        nm.initAlias(map);
        log.info("map={}", map);
        assertEquals(Integer.valueOf(BuiltinType.UINT8.type()), map.get("uint8_t"));
        assertEquals(Integer.valueOf(BuiltinType.SINT32.type()), map.get("int32_t"));
    }

    @Test
    public void testStringUTF() {
        DirectMemory hello = AllocatedMemory.allocate(10);
        String str = "hello!123";
        hello.putStringUTF(0, str);
        assertEquals(str, hello.getStringUTF(0));
    }

    @Test
    public void testFfi_call() throws Exception {
        NativeLibrary lib = NativeLibrary.open(LIBC, 0);
        long toupper = lib.dlsym("toupper");
        ffi_cif cif = new ffi_cif(CallingMode.DEFAULT, BuiltinType.SINT32, BuiltinType.SINT32);
        CallContext p = cif.newCallContext();
        int param = 'a';
        p.putInt(0, param);
        long addr = p.address();
        long result = nm.invokeInt(cif.address(), toupper, addr, null, 0);
        log.info("result = " + result);
        assertEquals('A', result);
    }

    /**
     * Test of allocateMemory method, of class NativeMethods.
     */
    @Test
    public void testAllocateMemory() throws Exception {
        log.info("allocateMemory");
        long size = 1000;
        for (int i = 0; i < 10000000; ++i) {
            long addr = nm.allocateMemory(size);
            nm.freeMemory(addr);
        }
    }

    /**
     * Test of freeMemory method, of class NativeMethods.
     */
    @Test
    public void testFreeMemory() {
        log.info("freeMemory");
        nm.freeMemory(0);
    }

    /**
     * Test of pageSize method, of class NativeMethods.
     */
    @Test
    @SuppressWarnings({"UseSpecificCatch", "BroadCatchBlock", "TooBroadCatch"})
    public void testPageSize() throws Exception {
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
        int result = nm.pageSize();
        assertEquals(expResult, result);
    }

    @Test
    public void testGetStringUTFEmpty() {
        long address = nm.allocateMemory(0);
        try {
            String string = nm.getStringUTF(address);
            assertEquals("", string);
        } finally {
            if (address != 0) {
                nm.freeMemory(address);
            }
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
     * Test of putStringChar16/getStringChar16/getStringChar16N methods, of class NativeMethods.
     */
    @Test
    public void testStringChar16() {
        log.info("StringChar16");
        AllocatedMemory memory = AllocatedMemory.allocate(40);
        long address = memory.address();
        String value = "\u0102\u0304\u0506\u0708\u0000\u0807\u0000";
        try {
            nm.putStringChar16(address, null);
            fail("should throw NullPointerException");
        } catch (NullPointerException ex) {
            // ok
        }
        try {
            nm.putStringChar16(0, value);
            fail("should throw NullPointerException");
        } catch (NullPointerException ex) {
            // ok
        }
        nm.putStringChar16(address, value);

        char[] arr1 = new char[4], arr2 = "\u0102\u0304\u0506\u0708".toCharArray();
        memory.getCharArray(0, arr1, 0, arr1.length);
        assertArrayEquals(arr2, arr1);

        assertHexEquals("aligned access", "\u0102\u0304\u0506\u0708", nm.getStringChar16(address));
        String expect;
        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            expect = "\u0203\u0405\u0607\u0800\u0008\u0700";
        } else {
            expect = "\u0401\u0603\u0805\u0007\u0700\u0008";
        }
        assertHexEquals("unaligned access", expect, nm.getStringChar16(address + 1));

        assertHexEquals("aligned access with limit", "\u0102\u0304\u0506\u0708", nm.getStringChar16N(address, 8));
        assertHexEquals("aligned access with limit", "\u0102\u0304\u0506", nm.getStringChar16N(address, 7));
        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            expect = "\u0203\u0405\u0607\u0800\u0008\u0700";
        } else {
            expect = "\u0401\u0603\u0805\u0007\u0700\u0008";
        }
        assertHexEquals("unaligned access with limit", expect, nm.getStringChar16N(address + 1, 12));
        if (ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN) {
            expect = "\u0203\u0405\u0607\u0800\u0008";
        } else {
            expect = "\u0401\u0603\u0805\u0007\u0700";
        }
        assertHexEquals("unaligned access with limit", expect, nm.getStringChar16N(address + 1, 11));
    }

}
