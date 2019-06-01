package jnc.foreign.spi;

import javax.annotation.Nonnull;
import jnc.foreign.Foreign;
import jnc.foreign.Platform;
import jnc.foreign.internal.DefaultForeignProvider;

public interface ForeignProvider {

    @Nonnull
    static ForeignProvider getDefault() {
        return DefaultForeignProvider.getInstance();
    }

    @Nonnull
    Foreign getForeign();

    @Nonnull
    Platform getPlatform();

}
