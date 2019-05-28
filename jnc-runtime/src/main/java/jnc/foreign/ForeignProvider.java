package jnc.foreign;

import javax.annotation.Nonnull;
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
