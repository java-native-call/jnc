package jnc.foreign;

import java.text.MessageFormat;
import java.util.Objects;
import javax.annotation.Nonnull;
import jnc.foreign.abi.CallingMode;

public class LoadOptionsBuilder {

    private static void checkState(boolean condition, String msg, Object... args) {
        if (!condition) {
            throw new IllegalStateException(MessageFormat.format(msg, args));
        }
    }

    private static <T> T checkNotNull(T obj) {
        // Objects.requireNonNull will be retrolambda as Object.getClass
        return Objects.requireNonNull(obj);
    }

    private CallingMode callingMode;

    public LoadOptionsBuilder() {
    }

    public LoadOptionsBuilder stdcall() {
        return callingMode(CallingMode.STDCALL);
    }

    public LoadOptionsBuilder cdecl() {
        return callingMode(CallingMode.DEFAULT);
    }

    public LoadOptionsBuilder callingMode(CallingMode callingMode) {
        checkState(this.callingMode == null, "Calling mode was already set to ''{}''", this.callingMode);
        this.callingMode = checkNotNull(callingMode);
        return this;
    }

    @Nonnull
    public LoadOptions create() {
        CallingMode cm = callingMode;
        return new LoadOptions(cm != null ? cm : CallingMode.DEFAULT);
    }

}
