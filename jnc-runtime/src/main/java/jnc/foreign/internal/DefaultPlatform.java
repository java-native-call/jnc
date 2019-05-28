package jnc.foreign.internal;

import javax.annotation.Nonnull;
import jnc.foreign.Platform;

class DefaultPlatform extends Platform {

    public static Platform getInstance() {
        return Singleton.INSTANCE;
    }

    private static boolean startsWith(String string, String other) {
        return string.regionMatches(true, 0, other, 0, other.length());
    }

    private final OS os;
    private final Arch arch;
    private final String libc;

    private DefaultPlatform() {
        String osName = System.getProperty("os.name", "unknown");
        if (startsWith(osName, "mac") || startsWith(osName, "darwin")) {
            os = OS.DARWIN;
        } else if (startsWith(osName, "windows")) {
            os = OS.WINDOWS;
        } else if (startsWith(osName, "freebsd")) {
            os = OS.FREEBSD;
        } else if (startsWith(osName, "openbsd")) {
            os = OS.OPENBSD;
        } else if (startsWith(osName, "linux")) {
            os = OS.LINUX;
        } else {
            os = OS.UNKNOWN;
        }
        String osArch = System.getProperty("os.arch", "unknown");
        if (osArch.matches("^(x(86[_-])?64|amd64|em64t)$")) {
            arch = Arch.X86_64;
        } else if (osArch.matches("^(i[3-6]86|x86|pentium)$")) {
            arch = Arch.I386;
        } else {
            arch = Arch.UNKNOWN;
        }
        String c;
        if (os == OS.WINDOWS) {
            c = "msvcrt.dll";
        } else {
            c = System.mapLibraryName("c");
        }
        this.libc = c;
    }

    @Nonnull
    @Override
    public OS getOS() {
        return os;
    }

    @Nonnull
    @Override
    public Arch getArch() {
        return arch;
    }

    @Nonnull
    @Override
    public String getLibcName() {
        return libc;
    }

    private interface Singleton {

        Platform INSTANCE = new DefaultPlatform();

    }

}
