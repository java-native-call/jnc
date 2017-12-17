package jnc.foreign;

import jnc.foreign.abi.CallingMode;

@SuppressWarnings("FinalClass")
public final class LoadOptions {

    public static LoadOptions defaultOptions() {
        return Singleton.DEFAULT;
    }

    private final CallingMode callingMode;

    LoadOptions(CallingMode callingMode) {
        this.callingMode = callingMode;
    }

    public CallingMode getCallingMode() {
        return callingMode;
    }

    private interface Singleton {

        LoadOptions DEFAULT = new LoadOptionsBuilder().create();

    }

}
