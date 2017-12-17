package jnc.foreign;

@SuppressWarnings("UtilityClassWithoutPrivateConstructor")
public class TestLibs {

    public static String getStandardMath() {
        switch (Platform.getNativePlatform().getOS()) {
            case WINDOWS:
                return "msvcrt.dll";
            case LINUX:
                return "libm.so.6";
            case DARWIN:
            default:
                return System.mapLibraryName("m");
        }
    }

}
