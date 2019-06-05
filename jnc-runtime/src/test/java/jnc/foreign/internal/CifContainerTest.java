package jnc.foreign.internal;

import jnc.foreign.Platform;
import jnc.foreign.Pointer;
import jnc.foreign.TestLibs;
import jnc.foreign.enums.CallingConvention;
import jnc.foreign.enums.TypeAlias;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CifContainerTest {

    private static final Logger log = LoggerFactory.getLogger(CifContainerTest.class);
    private static final String LIBC = DefaultPlatform.INSTANCE.getLibcName();
    private static final String LIBM = TestLibs.getStandardMath();

    @Test
    public void testAcos() {
        log.info("test acos");
        Library libm = NativeLibrary.open(LIBM, 0);

        long function = libm.dlsym("acos");
        CifContainer container = CifContainer.create(CallingConvention.DEFAULT, BuiltinType.DOUBLE, BuiltinType.DOUBLE);
        CallContext ctx = container.newCallContext();
        ctx.putDouble(0, -1);
        double result = ctx.invoke(Invokers::invokeDouble, function);
        assertEquals(Math.PI, result, 1e-10);
    }

    @Test
    public void testMemcpy() {
        log.info("test memcpy");
        Library libc = NativeLibrary.open(LIBC, 0);
        long function = libc.dlsym("memcpy");
        Alias sizeT = DefaultForeign.INSTANCE.getTypeFactory().findByAlias(TypeAlias.size_t);
        Alias uIntPtr = DefaultForeign.INSTANCE.getTypeFactory().findByAlias(TypeAlias.uintptr_t);
        CifContainer container = CifContainer.create(CallingConvention.DEFAULT, uIntPtr, uIntPtr, uIntPtr, sizeT);
        CallContext p = container.newCallContext();
        Pointer a = AllocatedMemory.allocate(20);
        SizedDirectMemory b = AllocatedMemory.allocate(20);
        String str = "memory copy test";
        b.putStringUTF(0, str);
        p.putLong(0, a.address());
        p.putLong(1, b.address());
        p.putLong(2, b.capacity());
        assertEquals("", a.getStringUTF(0));
        long addr = p.invoke(Invokers::invokeLong, function);
        assertEquals(a.address(), addr);
        assertEquals(str, a.getStringUTF(0));
    }

    @Test
    public void testGetpid() {
        log.info("test get pid");
        Library libc;
        long function;
        InternalType returnType;
        if (Platform.getNativePlatform().getOS().isWindows()) {
            libc = NativeLibrary.open("kernel32", 0);
            function = libc.dlsym("GetCurrentProcessId");
            returnType = DefaultForeign.INSTANCE.getTypeFactory().findByAlias(TypeAlias.uint32_t);
        } else {
            libc = NativeLibrary.open(LIBC, 0);
            function = libc.dlsym("getpid");
            returnType = DefaultForeign.INSTANCE.getTypeFactory().findByAlias(TypeAlias.pid_t);
        }
        long pid = CifContainer.create(CallingConvention.DEFAULT, returnType)
                .newCallContext()
                .invoke(Invokers::invokeLong, function);
        log.info("pid={}", pid);
    }

}
