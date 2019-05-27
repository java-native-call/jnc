package jnc.foreign;

import javax.annotation.Nullable;
import jnc.foreign.enums.CallingConvention;

public final class LoadOptions {

    private final CallingConvention callingConvention;

    LoadOptions(CallingConvention callingConvention) {
        this.callingConvention = callingConvention;
    }

    /**
     * @return null if not specified in {@code this} load options.
     */
    @Nullable
    public CallingConvention getCallingConvention() {
        return callingConvention;
    }

}
