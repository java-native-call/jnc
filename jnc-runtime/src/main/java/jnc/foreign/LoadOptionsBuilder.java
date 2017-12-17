package jnc.foreign;

import java.text.MessageFormat;
import jnc.foreign.abi.CallingMode;

public class LoadOptionsBuilder {

    private static void checkState(boolean condition, String msg, Object... args) {
        if (!condition) {
            throw new IllegalStateException(MessageFormat.format(msg, args));
        }
    }

    private static <T> T checkNotNull(T obj) {
        if (obj == null) {
            throw new NullPointerException();
        }
        return obj;
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

    public LoadOptions create() {
        CallingMode cm = callingMode;
        return new LoadOptions(cm != null ? cm : CallingMode.DEFAULT);
    }

}
