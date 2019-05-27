package jnc.foreign;

import java.text.MessageFormat;
import java.util.Objects;
import javax.annotation.Nonnull;
import jnc.foreign.enums.CallingConvention;

@SuppressWarnings("WeakerAccess")
public class LoadOptionsBuilder {

    private CallingConvention callingConvention;

    public LoadOptionsBuilder() {
    }

    public LoadOptionsBuilder stdcall() {
        return callingConvention(CallingConvention.STDCALL);
    }

    public LoadOptionsBuilder cdecl() {
        return callingConvention(CallingConvention.DEFAULT);
    }

    public LoadOptionsBuilder unsetCallingConvention() {
        this.callingConvention = null;
        return this;
    }

    public LoadOptionsBuilder callingConvention(CallingConvention callingConvention) {
        if (this.callingConvention != null) {
            throw new IllegalStateException(MessageFormat.format("Calling convention was already set to ''{0}''", this.callingConvention));
        }
        this.callingConvention = Objects.requireNonNull(callingConvention, "calling convention");
        return this;
    }

    @Nonnull
    public LoadOptions create() {
        return new LoadOptions(callingConvention);
    }

}
