package jnc.foreign.internal;

import javax.annotation.Nonnull;
import jnc.foreign.Foreign;
import jnc.foreign.ForeignProvider;

public class DefaultForeignProvider implements ForeignProvider {

    public static ForeignProvider getInstance() {
        return Singleton.INSTANCE;
    }

    private final Foreign foreign;

    private DefaultForeignProvider() {
        foreign = new DefaultForeign(this);
    }

    @Nonnull
    @Override
    public Foreign getForeign() {
        return foreign;
    }

    private interface Singleton {

        DefaultForeignProvider INSTANCE = new DefaultForeignProvider();

    }

}
