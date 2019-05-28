package jnc.foreign;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import jnc.foreign.annotation.Stdcall;
import jnc.foreign.byref.IntByReference;
import jnc.foreign.typedef.int32_t;
import jnc.foreign.typedef.uintptr_t;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("PublicInnerClass")
public class WindowsTest {

    private static final Logger log = LoggerFactory.getLogger(WindowsTest.class);

    @BeforeClass
    public static void setupClass() {
        assumeTrue("os.name", Platform.getNativePlatform().getOS().isWindows());
    }

    @Test
    public void testGetCurrentProcess() {
        log.info("GetCurrentProcess");
        long current = Kernel32.INSTANCE.GetCurrentProcess();
        if (Platform.getNativePlatform().getArch().sizeOfPointer() == 4) {
            assertEquals(0xFFFFFFFFL, current);
        } else {
            assertEquals(-1, current);
        }
    }

    @Test
    public void testGetExitCodeProcess() {
        log.info("GetExitCodeProcess");
        long current = Kernel32.INSTANCE.GetCurrentProcess();
        IntByReference byref = new IntByReference();
        for (int i = 0; i < 100000; ++i) {
            assertTrue(Kernel32.INSTANCE.GetExitCodeProcess(current, byref));
            assertEquals(0x103, byref.getValue());
        }
        int[] tmp = new int[1];
        assertTrue(Kernel32.INSTANCE.GetExitCodeProcess(current, tmp));
        assertEquals(0x103, tmp[0]);
    }

    @Test
    public void testGetProcessTimes() throws InterruptedException {
        log.info("GetProcessTimes");
        long current = Kernel32.INSTANCE.GetCurrentProcess();
        FILETIME a = new FILETIME();
        FILETIME b = new FILETIME();
        FILETIME c = new FILETIME();
        FILETIME d = new FILETIME();
        long now = System.currentTimeMillis();
        long least = now - 24L * 60 * 60 * 1000;
        for (int i = 0; i < 20; ++i) {
            assertTrue(Kernel32.INSTANCE.GetProcessTimes(current, a, b, c, d));
            assertTrue(least < a.toMillis() && a.toMillis() <= now);
        }
    }

    @Test
    public void testLastError() throws Throwable {
        log.info("lastError");
        ExecutorService es = Executors.newFixedThreadPool(8);
        final AtomicReference<Throwable> atomic = new AtomicReference<>();
        for (int i = 0; i < 2000; ++i) {
            es.submit(() -> {
                try {
                    IntByReference dwExitCode = new IntByReference();
                    assertFalse(Kernel32.INSTANCE.GetExitCodeProcess(0, dwExitCode));
                    assertEquals(6, Foreign.getDefault().getLastError());
                } catch (Throwable t) {
                    atomic.compareAndSet(null, t);
                }
            });
        }
        es.shutdown();
        es.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
        if (atomic.get() != null) {
            throw atomic.get();
        }
    }

    public static class FILETIME extends Struct {

        private static long toMillis(final int high, final int low) {
            final long filetime = (long) high << 32 | low & 0xffffffffL;
            final long ms_since_16010101 = filetime / (1000 * 10);
            final long ms_since_19700101 = ms_since_16010101 - 11644473600000L;
            return ms_since_19700101;
        }

        private final DWORD dwLowDateTime = new DWORD();
        private final DWORD dwHighDateTime = new DWORD();

        public int getLowDateTime() {
            return dwLowDateTime.intValue();
        }

        public int getHighDateTime() {
            return dwHighDateTime.intValue();
        }

        public long longValue() {
            return (long) getHighDateTime() << 32 | (getLowDateTime() & 0xFFFFFFFFL);
        }

        public long toMillis() {
            return toMillis(getHighDateTime(), getLowDateTime());
        }

        @Override
        public String toString() {
            return Long.toString(longValue());
        }

    }

    @Stdcall
    public interface Kernel32 {

        Kernel32 INSTANCE = LibraryLoader.create(Kernel32.class).load("kernel32");

        @uintptr_t
        long /*HANDLE*/ GetCurrentProcess();

        @int32_t
        boolean GetExitCodeProcess(@uintptr_t long process, IntByReference byref);

        @int32_t
        boolean GetExitCodeProcess(@uintptr_t long process, int[] byref);

        @int32_t
        boolean GetProcessTimes(
                @uintptr_t long /*HANDLE*/ hProcess,
                FILETIME lpCreationTime,
                FILETIME lpExitTime,
                FILETIME lpKernelTime,
                FILETIME lpUserTime);
    }

}
