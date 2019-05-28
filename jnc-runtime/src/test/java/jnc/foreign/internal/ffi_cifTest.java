package jnc.foreign.internal;

import jnc.foreign.TestLibs;
import jnc.foreign.enums.CallingConvention;
import jnc.foreign.enums.TypeAlias;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ffi_cifTest {

    private static final Logger log = LoggerFactory.getLogger(ffi_cifTest.class);
    private static final String LIBC = DefaultPlatform.INSTANCE.getLibcName();
    private static final String LIBM = TestLibs.getStandardMath();
    private static final NativeMethods nm = NativeMethods.getInstance();

    @Test
    public void testAcos() {
        log.info("test acos");
        Library libm = NativeLibrary.open(LIBM, 0);

        long function = libm.dlsym("acos");
        ffi_cif cif = new ffi_cif(CallingConvention.DEFAULT, BuiltinType.DOUBLE, BuiltinType.DOUBLE);
        CallContext ctx = cif.newCallContext();
        ctx.putDouble(0, -1);
        double result = nm.invokeDouble(cif.getCifAddress(), function, ctx.parameterBaseAddress(), ctx.offsets(), null, 0);
        assertEquals(Math.PI, result, 1e-10);
    }

    @Test
    public void testMemcpy() {
        log.info("test memcpy");
        Library libc = NativeLibrary.open(LIBC, 0);
        long function = libc.dlsym("memcpy");
        Alias sizeT = TypeHelper.findByAlias(TypeAlias.size_t);
        Alias uIntPtr = TypeHelper.findByAlias(TypeAlias.uintptr_t);
        ffi_cif cif = new ffi_cif(CallingConvention.DEFAULT, uIntPtr, uIntPtr, uIntPtr, sizeT);
        CallContext p = cif.newCallContext();
        Memory a = AllocatedMemory.allocate(20);
        AllocatedMemory b = AllocatedMemory.allocate(20);
        String str = "memory copy test";
        b.putStringUTF(0, str);
        p.putLong(0, a.address());
        p.putLong(1, b.address());
        p.putLong(2, b.size());
        assertEquals("", a.getStringUTF(0));
        long addr = nm.invokeLong(cif.getCifAddress(), function, p.parameterBaseAddress(), p.offsets(), null, 0);
        assertEquals(a.address(), addr);
        assertEquals(str, a.getStringUTF(0));
    }

}
