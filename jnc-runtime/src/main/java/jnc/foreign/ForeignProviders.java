package jnc.foreign;

import jnc.foreign.internal.DefaultForeignProvider;

@SuppressWarnings("FinalClass")
public final class ForeignProviders {

    public static Foreign getDefault() {
        return DefaultForeignProvider.getInstance().getForeign();
    }

    private ForeignProviders() {
        throw new AssertionError();
    }

}
