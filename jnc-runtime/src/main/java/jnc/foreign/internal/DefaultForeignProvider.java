package jnc.foreign.internal;

import jnc.foreign.Foreign;
import jnc.foreign.ForeignProvider;

public class DefaultForeignProvider implements ForeignProvider {

    public static ForeignProvider getInstance() {
        return Singleton.INSTANCE;
    }

    private final Foreign foreign;

    DefaultForeignProvider() {
        foreign = new ForeignImpl(this);
    }

    @Override
    public Foreign getForeign() {
        return foreign;
    }

    private interface Singleton {

        DefaultForeignProvider INSTANCE = new DefaultForeignProvider();

    }

}
