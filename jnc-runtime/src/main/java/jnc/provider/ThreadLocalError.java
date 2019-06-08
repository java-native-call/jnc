package jnc.provider;

import java.lang.reflect.Method;

enum ThreadLocalError implements LastErrorHandler {

    INSTANCE;

    /**
     * Use Integer to avoid memory leak.
     */
    private static final ThreadLocal<Integer> THREAD_LOCAL = new ThreadLocal<>();

    static int get() {
        Integer i = THREAD_LOCAL.get();
        return i != null ? i : 0;
    }

    static Object getInstance() {
        return INSTANCE;
    }

    static long getMethodId() {
        try {
            Method method = LastErrorHandler.class.getMethod("handle", int.class);
            return NativeLoader.getAccessor().getMethodId(method);
        } catch (NoSuchMethodException ex) {
            throw new AssertionError(ex);
        }
    }

    @Override
    public void handle(int error) {
        if (error == 0) {
            THREAD_LOCAL.remove();
        } else {
            THREAD_LOCAL.set(error);
        }
    }

}
