package jnc.foreign.internal;

import java.lang.reflect.Method;

@SuppressWarnings("UtilityClassWithoutPrivateConstructor")
class LastErrorHandlers {

    private static final long METHOD_ID;

    static {
        try {
            Method method = LastErrorHandler.class.getMethod("handle", int.class);
            METHOD_ID = NativeMethods.getInstance().getMethodId(method);
        } catch (NoSuchMethodException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    static long methodId() {
        return METHOD_ID;
    }

    static Object object() {
        return ThreadLocalError.getInstance();
    }

}
