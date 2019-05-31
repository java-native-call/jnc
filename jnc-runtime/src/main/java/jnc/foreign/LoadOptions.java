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
    private final boolean failImmediately;

    LoadOptions(CallingConvention callingConvention, boolean failImmediately) {
        this.callingConvention = callingConvention;
        this.failImmediately = failImmediately;
    }

    @Nonnull
    public CallingConvention getCallingConvention() {
        return callingConvention;
    }

    public boolean isFailImmediately() {
        return failImmediately;
    }

}
