package jnc.foreign;

public abstract class Platform {

    public static Platform getNativePlatform() {
        return ForeignProviders.getDefault().getPlatform();
    }

    public abstract String getLibcName();

    public abstract OS getOS();

    public abstract Arch getArch();

}
