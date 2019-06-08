package jnc.provider;

import java.lang.reflect.Method;
import java.util.function.IntConsumer;

enum DefaultLastErrorHandler implements IntConsumer {

    INSTANCE;

    public static final long METHOD_ID = DefaultLastErrorHandler.getMethodId();

    /**
     * Use Integer to avoid memory leak.
     */
    private static final ThreadLocal<Integer> THREAD_LOCAL = new ThreadLocal<>();

    static int get() {
        Integer i = THREAD_LOCAL.get();
        return i != null ? i : 0;
    }

    static long getMethodId() {
        try {
            Method method = IntConsumer.class.getMethod("accept", int.class);
            return NativeLoader.getAccessor().getMethodId(method);
        } catch (NoSuchMethodException ex) {
            throw new AssertionError(ex);
        }
    }

    @Override
    public void accept(int error) {
        if (error == 0) {
            THREAD_LOCAL.remove();
        } else {
            THREAD_LOCAL.set(error);
        }
    }

}
