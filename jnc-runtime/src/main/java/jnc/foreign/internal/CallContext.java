package jnc.foreign.internal;

interface CallContext {

    default void putBoolean(int i, boolean value) {
        putInt(i, value ? 1 : 0);
    }

    default void putByte(int i, byte value) {
        putInt(i, value);
    }

    default void putChar(int i, char value) {
        putInt(i, value);
    }

    default void putShort(int i, short value) {
        putInt(i, value);
    }

    void putInt(int i, int value);

    void putLong(int i, long value);

    void putFloat(int i, float value);

    void putDouble(int i, double value);

    CallContext onFinish(Runnable r);

    <T> T invoke(Invoker<T> invoker, long function);

}
