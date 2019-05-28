package jnc.foreign.internal;

import java.util.EnumSet;
import javax.annotation.Nonnull;
import jnc.foreign.Platform;

enum DefaultPlatform implements Platform {

    INSTANCE;

    private static boolean startsWith(String string, String other) {
        return string.regionMatches(true, 0, other, 0, other.length());
    }

    private static OS getOsByName(String osName) {
        for (OS maybe : EnumSet.allOf(OS.class)) {
            if (startsWith(osName, maybe.name())) {
                return maybe;
            }
        }
        return startsWith(osName, "mac") ? OS.DARWIN : OS.UNKNOWN;
    }

    private static Arch getArchByName(String archName) {
        if (archName.matches("^(?i)(?:x(86[_-])?64|amd64|em64t)$")) {
            return Arch.X86_64;
        } else if (archName.matches("^(?i)(?:i[3-6]86|x86|pentium)$")) {
            return Arch.I386;
        } else {
            return Arch.UNKNOWN;
        }
    }

    private final OS os;
    private final Arch arch;

    DefaultPlatform() {
        this.os = getOsByName(System.getProperty("os.name", "unknown"));
        this.arch = getArchByName(System.getProperty("os.arch", "unknown"));
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
        switch (os) {
            case WINDOWS:
                return "msvcrt.dll";
            case LINUX:
                return "libc.so.6";
            case DARWIN:
                return "libc.dylib";
            default:
                return System.mapLibraryName("c");
        }
    }

}
