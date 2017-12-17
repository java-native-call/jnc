package jnc.foreign;

import jnc.foreign.typedef.pid_t;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assume.assumeThat;
import org.junit.Before;
import org.junit.Test;

public class GetPidTest {

    @Before
    public void setUp() {
        assumeThat(Platform.getNativePlatform().getOS(), not(OS.WINDOWS));
    }

    @Test
    public void test() {
        for (int i = 0; i < 2000000; ++i) {
            LibC.INSTANCE.getpid();
        }
    }

    @SuppressWarnings("PublicInnerClass")
    public interface LibC {

        LibC INSTANCE = LibraryLoader.create(LibC.class).load(Platform.getNativePlatform().getLibcName());

        @pid_t
        long getpid();

    }

}
