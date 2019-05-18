package jnc.foreign;

import javax.annotation.Nonnull;

public abstract class Platform {

    @Nonnull
    public static Platform getNativePlatform() {
        return ForeignProviders.getDefault().getPlatform();
    }

    @Nonnull
    public abstract String getLibcName();

    @Nonnull
    public abstract OS getOS();

    @Nonnull
    public abstract Arch getArch();

}
