package jnc.foreign;

import java.text.MessageFormat;
import java.util.Objects;
import javax.annotation.Nonnull;
import jnc.foreign.enums.CallingConvention;

@SuppressWarnings("WeakerAccess")
public class LoadOptionsBuilder {

    private static void checkState(boolean condition, String msg, Object... args) {
        if (!condition) {
            throw new IllegalStateException(MessageFormat.format(msg, args));
        }
    }

    private CallingConvention callingConvention;

    public LoadOptionsBuilder() {
    }

    public LoadOptionsBuilder stdcall() {
        return callingConvention(CallingConvention.STDCALL);
    }

    public LoadOptionsBuilder cdecl() {
        return callingConvention(CallingConvention.DEFAULT);
    }

    public LoadOptionsBuilder callingConvention(CallingConvention callingConvention) {
        checkState(this.callingConvention == null, "Calling convention was already set to ''{}''", this.callingConvention);
        this.callingConvention = Objects.requireNonNull(callingConvention, "calling mode");
        return this;
    }

    @Nonnull
    public LoadOptions create() {
        CallingConvention cc = callingConvention;
        return new LoadOptions(cc != null ? cc : CallingConvention.DEFAULT);
    }

}
