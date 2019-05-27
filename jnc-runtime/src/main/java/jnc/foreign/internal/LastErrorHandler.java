package jnc.foreign.internal;

import java.lang.reflect.Method;

@FunctionalInterface
interface LastErrorHandler {

    long METHOD_ID = Companion.METHOD_ID;

    void handle(int error);

    @SuppressWarnings({"PackageVisibleInnerClass", "UtilityClassWithoutPrivateConstructor"})
    class Companion {

        static final long METHOD_ID;

        static {
            try {
                Method method = LastErrorHandler.class.getMethod("handle", int.class);
                METHOD_ID = NativeMethods.getInstance().getMethodId(method);
            } catch (NoSuchMethodException ex) {
                throw new ExceptionInInitializerError(ex);
            }
        }
    }

}
