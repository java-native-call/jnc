package jnc.foreign.internal;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
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
    void putBoolean(int offset, InternalType internalType, boolean value) {
        nm.putInt(address + offset, internalType.address(), value ? 0 : 1);
    }

    @Override
    void putInt(int offset, InternalType internalType, int value) {
        nm.putInt(address + offset, internalType.address(), value);
    }

    @Override
    void putLong(int offset, InternalType internalType, long value) {
        nm.putLong(address + offset, internalType.address(), value);
    }

    @Override
    void putFloat(int offset, InternalType internalType, float value) {
        nm.putFloat(address + offset, internalType.address(), value);
    }

    @Override
    void putDouble(int offset, InternalType internalType, double value) {
        nm.putDouble(address + offset, internalType.address(), value);
    }

    @Override
    boolean getBoolean(int offset, InternalType internalType) {
        return nm.getBoolean(address + offset, internalType.address());
    }

    @Override
    int getInt(int offset, InternalType internalType) {
        return nm.getInt(address + offset, internalType.address());
    }

    @Override
    long getLong(int offset, InternalType internalType) {
        return nm.getLong(address + offset, internalType.address());
    }

    @Override
    float getFloat(int offset, InternalType internalType) {
        return nm.getFloat(address + offset, internalType.address());
    }

    @Override
    double getDouble(int offset, InternalType internalType) {
        return nm.getDouble(address + offset, internalType.address());
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
    public void putStringUTF(int offset, @Nonnull String value) {
        nm.putStringUTF(address + offset, value);
    }

    @Nonnull
    @Override
    public String getStringUTF(int offset) {
        return nm.getStringUTF(address + offset);
    }

    @Override
    public void putString16(int offset, @Nonnull String value) {
        nm.putStringChar16(address + offset, value);
    }

    @Nonnull
    @Override
    public String getString16(int offset) {
        return nm.getStringChar16(address + offset);
    }

    @Nullable
    @Override
    public Pointer getPointer(int offset) {
        return of(getAddress(offset));
    }

    @Override
    public void putPointer(int offset, @Nullable Pointer pointer) {
        putAddress(offset, pointer != null ? pointer.address() : 0);
    }

    @Nonnull
    @Override
    public Pointer slice(int offset, int size) {
        if (size < 0) {
            throw new IllegalArgumentException();
        }
        return new Slice(this, offset, size);
    }

}
