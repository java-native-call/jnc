package jnc.foreign;

import jnc.foreign.enums.CallingConvention;

public final class LoadOptions {

    public static LoadOptions defaultOptions() {
        return Singleton.DEFAULT;
    }

    private final CallingConvention callingConvention;

    LoadOptions(CallingConvention callingConvention) {
        this.callingConvention = callingConvention;
    }

    public CallingConvention getCallingConvention() {
        return callingConvention;
    }

    private interface Singleton {

        LoadOptions DEFAULT = new LoadOptionsBuilder().create();

    }

}
