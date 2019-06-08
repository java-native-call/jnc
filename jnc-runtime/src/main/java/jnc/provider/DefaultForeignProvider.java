package jnc.provider;

import javax.annotation.Nonnull;
import jnc.foreign.Foreign;
import jnc.foreign.Platform;
import jnc.foreign.spi.ForeignProvider;

public enum DefaultForeignProvider implements ForeignProvider {

    INSTANCE;

    public static ForeignProvider getInstance() {
        return INSTANCE;
    }

    @Nonnull
    @Override
    public Foreign getForeign() {
        return DefaultForeign.INSTANCE;
    }

    @Nonnull
    @Override
    public Platform getPlatform() {
        return DefaultPlatform.INSTANCE;
    }

}
