package jnc.foreign.internal;

import jnc.foreign.Pointer;

class DirectMemory extends AbstractMemory implements NativeObject, Pointer {

    private static final NativeMethods nm = NativeMethods.getInstance();

    static Pointer of(long address) {
        return address == 0 ? null : new DirectMemory(address);
    }

    private final long address;

    DirectMemory(long address) {
        if (address == 0) {
            throw new NullPointerException();
        }
        this.address = address;
    }

    @Override
    @SuppressWarnings("FinalMethod")
    public final long address() {
        return address;
    }

    /**
     * Do not rely on the String presentation, maybe changed in the future.
     *
     * @return
     */
    @Override
    public String toString() {
        return "[" + getClass().getSimpleName() + "@" + Long.toHexString(address) + ",]";
    }

    @Override
    void putBoolean(int offset, BuiltinType builtinType, boolean value) {
        nm.putInt(address + offset, builtinType.address(), value ? 0 : 1);
    }

    @Override
    void putInt(int offset, BuiltinType builtinType, int value) {
        nm.putInt(address + offset, builtinType.address(), value);
    }

    @Override
    void putLong(int offset, BuiltinType builtinType, long value) {
        nm.putLong(address + offset, builtinType.address(), value);
    }

    @Override
    void putFloat(int offset, BuiltinType builtinType, float value) {
        nm.putFloat(address + offset, builtinType.address(), value);
    }

    @Override
    void putDouble(int offset, BuiltinType builtinType, double value) {
        nm.putDouble(address + offset, builtinType.address(), value);
    }

    @Override
    boolean getBoolean(int offset, BuiltinType builtinType) {
        return nm.getBoolean(address + offset, builtinType.address());
    }

    @Override
    int getInt(int offset, BuiltinType builtinType) {
        return nm.getInt(address + offset, builtinType.address());
    }

    @Override
    long getLong(int offset, BuiltinType builtinType) {
        return nm.getLong(address + offset, builtinType.address());
    }

    @Override
    float getFloat(int offset, BuiltinType builtinType) {
        return nm.getFloat(address + offset, builtinType.address());
    }

    @Override
    double getDouble(int offset, BuiltinType builtinType) {
        return nm.getDouble(address + offset, builtinType.address());
    }

    @Override
    public void putBytes(int offset, byte[] bytes, int off, int len) {
        nm.putBytes(address + offset, bytes, off, len);
    }

    @Override
    public void getBytes(int offset, byte[] bytes, int off, int len) {
        nm.getBytes(address + offset, bytes, off, len);
    }

    @Override
    public void getShortArray(int offset, short[] array, int off, int len) {
        nm.getShortArray(address + offset, array, off, len);
    }

    @Override
    public void putShortArray(int offset, short[] array, int off, int len) {
        nm.putShortArray(address + offset, array, off, len);
    }

    @Override
    public void getCharArray(int offset, char[] array, int off, int len) {
        nm.getCharArray(address + offset, array, off, len);
    }

    @Override
    public void putCharArray(int offset, char[] array, int off, int len) {
        nm.putCharArray(address + offset, array, off, len);
    }

    @Override
    public void getIntArray(int offset, int[] array, int off, int len) {
        nm.getIntArray(address + offset, array, off, len);
    }

    @Override
    public void putIntArray(int offset, int[] array, int off, int len) {
        nm.putIntArray(address + offset, array, off, len);
    }

    @Override
    public void getLongArray(int offset, long[] array, int off, int len) {
        nm.getLongArray(address + offset, array, off, len);
    }

    @Override
    public void putLongArray(int offset, long[] array, int off, int len) {
        nm.putLongArray(address + offset, array, off, len);
    }

    @Override
    public void getFloatArray(int offset, float[] array, int off, int len) {
        nm.getFloatArray(address + offset, array, off, len);
    }

    @Override
    public void putFloatArray(int offset, float[] array, int off, int len) {
        nm.putFloatArray(address + offset, array, off, len);
    }

    @Override
    public void getDoubleArray(int offset, double[] array, int off, int len) {
        nm.getDoubleArray(address + offset, array, off, len);
    }

    @Override
    public void putDoubleArray(int offset, double[] array, int off, int len) {
        nm.putDoubleArray(address + offset, array, off, len);
    }

    @Override
    public void putStringUTF(int offset, String value) {
        nm.putStringUTF(address + offset, value);
    }

    @Override
    public String getStringUTF(int offset) {
        return nm.getStringUTF(address + offset);
    }

    @Override
    public Pointer getPointer(int offset) {
        return of(getAddress(offset));
    }

    @Override
    public void putPointer(int offset, Pointer pointer) {
        putAddress(offset, pointer != null ? pointer.address() : 0);
    }

    @Override
    public Pointer slice(int offset, int size) {
        if ((offset | size) < 0) {
            throw new IndexOutOfBoundsException();
        }
        return new Slice(this, offset, size);
    }

}
