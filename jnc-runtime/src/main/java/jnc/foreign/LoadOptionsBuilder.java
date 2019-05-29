package jnc.foreign;

import java.util.Objects;
import javax.annotation.Nonnull;
import jnc.foreign.enums.CallingConvention;

@SuppressWarnings("WeakerAccess")
public class LoadOptionsBuilder {

    private CallingConvention callingConvention = CallingConvention.DEFAULT;

    LoadOptionsBuilder() {
    }

    public LoadOptionsBuilder stdcall() {
        return callingConvention(CallingConvention.STDCALL);
    }

    public LoadOptionsBuilder defaultCallingConvention() {
        return callingConvention(CallingConvention.DEFAULT);
    }

    public LoadOptionsBuilder callingConvention(CallingConvention callingConvention) {
        this.callingConvention = Objects.requireNonNull(callingConvention);
        return this;
    }

    @Nonnull
    public LoadOptions build() {
        return new LoadOptions(callingConvention);
    }

}
