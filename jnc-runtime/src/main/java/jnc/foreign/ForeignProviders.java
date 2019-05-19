package jnc.foreign;

import javax.annotation.Nonnull;
import jnc.foreign.internal.DefaultForeignProvider;

public final class ForeignProviders {

    @Nonnull
    public static Foreign getDefault() {
        return DefaultForeignProvider.getInstance().getForeign();
    }

    private ForeignProviders() {
        throw new AssertionError();
    }

}
