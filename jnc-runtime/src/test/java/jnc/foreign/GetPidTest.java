package jnc.foreign;

import jnc.foreign.typedef.pid_t;
import static org.junit.Assume.assumeFalse;
import org.junit.Before;
import org.junit.Test;

public class GetPidTest {

    @Before
    public void setUp() {
        assumeFalse("os.name", Platform.getNativePlatform().getOS().isWindows());
    }

    @Test
    public void test() {
        for (int i = 0; i < 2000000; ++i) {
            LibC.INSTANCE.getpid();
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    private interface LibC {

        LibC INSTANCE = LibraryLoader.create(LibC.class).load(Platform.getNativePlatform().getLibcName());

        @pid_t
        long getpid();

    }

}
