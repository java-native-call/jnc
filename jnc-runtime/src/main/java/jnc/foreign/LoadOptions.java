package jnc.foreign;

import javax.annotation.Nonnull;
import jnc.foreign.enums.CallingConvention;

public final class LoadOptions {

    public static LoadOptionsBuilder builder() {
        return empty();
    }

    public static LoadOptionsBuilder empty() {
        return new LoadOptionsBuilder();
    }

    private final CallingConvention callingConvention;

    LoadOptions(CallingConvention callingConvention) {
        this.callingConvention = callingConvention;
    }

    @Nonnull
    public CallingConvention getCallingConvention() {
        return callingConvention;
    }

}
