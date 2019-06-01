package jnc.foreign;

import java.util.Objects;
import javax.annotation.Nonnull;
import jnc.foreign.enums.CallingConvention;

@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
public final class LoadOptionsBuilder {

    private CallingConvention callingConvention = CallingConvention.DEFAULT;
    private boolean failImmediately;

    LoadOptionsBuilder() {
    }

    public LoadOptionsBuilder stdcall() {
        return convention(CallingConvention.STDCALL);
    }

    public LoadOptionsBuilder convention(CallingConvention callingConvention) {
        this.callingConvention = Objects.requireNonNull(callingConvention);
        return this;
    }

    public LoadOptionsBuilder failImmediately() {
        failImmediately = true;
        return this;
    }

    public LoadOptionsBuilder failDeferred() {
        failImmediately = false;
        return this;
    }

    @Nonnull
    public LoadOptions build() {
        return new LoadOptions(callingConvention, failImmediately);
    }

}
