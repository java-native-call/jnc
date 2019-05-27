package jnc.foreign.internal;

import javax.annotation.Nonnull;
import jnc.foreign.Pointer;

final class UnboundedDirectMemory extends Memory implements NativeObject, Pointer {

    static Pointer of(long address) {
        return address == 0 ? null : new UnboundedDirectMemory(address);
    }

    private UnboundedDirectMemory(long address) {
        super(new MemoryAccessor(address));
    }

    @Override
    public long address() {
        return getMemoryAccessor().address();
    }

    /**
     * Do not rely on the String presentation, maybe changed in the future.
     */
    @Override
    public String toString() {
        return "[" + getClass().getSimpleName() + "@" + Long.toHexString(getMemoryAccessor().address()) + ",]";
    }

    @Override
    public final void putByte(int offset, byte value) {
        getMemoryAccessor().putByte(offset, value);
    }

    @Override
    public final void putChar(int offset, char value) {
        getMemoryAccessor().putShort(offset, (short) value);
    }

    @Override
    public final void putShort(int offset, short value) {
        getMemoryAccessor().putShort(offset, value);
    }

    @Override
    public final void putInt(int offset, int value) {
        getMemoryAccessor().putInt(offset, value);
    }

    @Override
    public final void putLong(int offset, long value) {
        getMemoryAccessor().putLong(offset, value);
    }

    @Override
    public final void putFloat(int offset, float value) {
        getMemoryAccessor().putFloat(offset, value);
    }

    @Override
    public final void putDouble(int offset, double value) {
        getMemoryAccessor().putDouble(offset, value);
    }

    @Override
    public final byte getByte(int offset) {
        return getMemoryAccessor().getByte(offset);
    }

    @Override
    public final short getShort(int offset) {
        return getMemoryAccessor().getShort(offset);
    }

    @Override
    public final char getChar(int offset) {
        return (char) getMemoryAccessor().getShort(offset);
    }

    @Override
    public final int getInt(int offset) {
        return getMemoryAccessor().getInt(offset);
    }

    @Override
    public final long getLong(int offset) {
        return getMemoryAccessor().getLong(offset);
    }

    @Override
    public final float getFloat(int offset) {
        return getMemoryAccessor().getFloat(offset);
    }

    @Override
    public final double getDouble(int offset) {
        return getMemoryAccessor().getDouble(offset);
    }

    @Override
    void putBoolean(int offset, InternalType internalType, boolean value) {
        getMemoryAccessor().putBoolean(offset, internalType, value);
    }

    @Override
    void putInt(int offset, InternalType internalType, int value) {
        getMemoryAccessor().putInt(offset, internalType, value);
    }

    @Override
    void putLong(int offset, InternalType internalType, long value) {
        getMemoryAccessor().putLong(offset, internalType, value);
    }

    @Override
    void putFloat(int offset, InternalType internalType, float value) {
        getMemoryAccessor().putFloat(offset, internalType, value);
    }

    @Override
    void putDouble(int offset, InternalType internalType, double value) {
        getMemoryAccessor().putDouble(offset, internalType, value);
    }

    @Override
    boolean getBoolean(int offset, InternalType internalType) {
        return getMemoryAccessor().getBoolean(offset, internalType);
    }

    @Override
    int getInt(int offset, InternalType internalType) {
        return getMemoryAccessor().getInt(offset, internalType);
    }

    @Override
    long getLong(int offset, InternalType internalType) {
        return getMemoryAccessor().getLong(offset, internalType);
    }

    @Override
    float getFloat(int offset, InternalType internalType) {
        return getMemoryAccessor().getFloat(offset, internalType);
    }

    @Override
    double getDouble(int offset, InternalType internalType) {
        return getMemoryAccessor().getDouble(offset, internalType);
    }

    @Override
    public void putBytes(int offset, byte[] bytes, int off, int len) {
        getMemoryAccessor().putBytes(offset, bytes, off, len);
    }

    @Override
    public void getBytes(int offset, byte[] bytes, int off, int len) {
        getMemoryAccessor().getBytes(offset, bytes, off, len);
    }

    @Override
    public void getShortArray(int offset, short[] array, int off, int len) {
        getMemoryAccessor().getShortArray(offset, array, off, len);
    }

    @Override
    public void putShortArray(int offset, short[] array, int off, int len) {
        getMemoryAccessor().putShortArray(offset, array, off, len);
    }

    @Override
    public void getCharArray(int offset, char[] array, int off, int len) {
        getMemoryAccessor().getCharArray(offset, array, off, len);
    }

    @Override
    public void putCharArray(int offset, char[] array, int off, int len) {
        getMemoryAccessor().putCharArray(offset, array, off, len);
    }

    @Override
    public void getIntArray(int offset, int[] array, int off, int len) {
        getMemoryAccessor().getIntArray(offset, array, off, len);
    }

    @Override
    public void putIntArray(int offset, int[] array, int off, int len) {
        getMemoryAccessor().putIntArray(offset, array, off, len);
    }

    @Override
    public void getLongArray(int offset, long[] array, int off, int len) {
        getMemoryAccessor().getLongArray(offset, array, off, len);
    }

    @Override
    public void putLongArray(int offset, long[] array, int off, int len) {
        getMemoryAccessor().putLongArray(offset, array, off, len);
    }

    @Override
    public void getFloatArray(int offset, float[] array, int off, int len) {
        getMemoryAccessor().getFloatArray(offset, array, off, len);
    }

    @Override
    public void putFloatArray(int offset, float[] array, int off, int len) {
        getMemoryAccessor().putFloatArray(offset, array, off, len);
    }

    @Override
    public void getDoubleArray(int offset, double[] array, int off, int len) {
        getMemoryAccessor().getDoubleArray(offset, array, off, len);
    }

    @Override
    public void putDoubleArray(int offset, double[] array, int off, int len) {
        getMemoryAccessor().putDoubleArray(offset, array, off, len);
    }

    @Override
    public void putStringUTF(int offset, @Nonnull String value) {
        getMemoryAccessor().putStringUTF(offset, value);
    }

    @Nonnull
    @Override
    public String getStringUTF(int offset) {
        return getMemoryAccessor().getStringUTF(offset);
    }

    @Override
    public void putString16(int offset, @Nonnull String value) {
        getMemoryAccessor().putString16(offset, value);
    }

    @Nonnull
    @Override
    public String getString16(int offset) {
        return getMemoryAccessor().getString16(offset);
    }

    /**
     * @return a direct memory of specified range, OK if offset is negative.
     * since we are unbounded.
     */
    @Nonnull
    @Override
    public Pointer slice(int offset, int size) {
        return new Slice(this, offset, size);
    }

}
