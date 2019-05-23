package jnc.foreign;

import javax.annotation.Nonnull;

public final class ForeignProviders {

    @Nonnull
    public static Foreign getDefault() {
        return ForeignProvider.getDefault().getForeign();
    }

    private ForeignProviders() {
        throw new AssertionError();
    }

}
