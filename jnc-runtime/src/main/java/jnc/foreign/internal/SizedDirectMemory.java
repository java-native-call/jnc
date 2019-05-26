package jnc.foreign.internal;

import java.nio.charset.StandardCharsets;
import javax.annotation.Nonnull;

class SizedDirectMemory extends Memory {

    private final long size;

    SizedDirectMemory(long address, long size) {
        super(new MemoryAccessor(address));
        this.size = size;
    }

    @Override
    public final long address() {
        return getMemoryAccessor().address();
    }

    public final long size() {
        return size;
    }

    /**
     * Do not rely on the String presentation, maybe changed in the future.
     */
    @Override
    public final String toString() {
        return "[" + getClass().getSimpleName() + "#" + Long.toHexString(address()) + ",size=" + size + "]";
    }

    @Override
    final void putDouble(int offset, InternalType internalType, double value) {
        getMemoryAccessor().checkIndex(offset, size, internalType.size())
                .putDouble(offset, internalType, value);
    }

    @Override
    final void putFloat(int offset, InternalType internalType, float value) {
        getMemoryAccessor().checkIndex(offset, size, internalType.size())
                .putFloat(offset, internalType, value);
    }

    @Override
    final void putBoolean(int offset, InternalType internalType, boolean value) {
        getMemoryAccessor().checkIndex(offset, size, internalType.size())
                .putBoolean(offset, internalType, value);
    }

    @Override
    final void putInt(int offset, InternalType internalType, int value) {
        getMemoryAccessor().checkIndex(offset, size, internalType.size())
                .putInt(offset, internalType, value);
    }

    @Override
    final void putLong(int offset, InternalType internalType, long value) {
        getMemoryAccessor().checkIndex(offset, size, internalType.size())
                .putLong(offset, internalType, value);
    }

    @Override
    final double getDouble(int offset, InternalType internalType) {
        return getMemoryAccessor().checkIndex(offset, size, internalType.size())
                .getDouble(offset, internalType);
    }

    @Override
    final float getFloat(int offset, InternalType internalType) {
        return getMemoryAccessor().checkIndex(offset, size, internalType.size())
                .getFloat(offset, internalType);
    }

    @Override
    final long getLong(int offset, InternalType internalType) {
        return getMemoryAccessor().checkIndex(offset, size, internalType.size())
                .getLong(offset, internalType);
    }

    @Override
    final int getInt(int offset, InternalType internalType) {
        return getMemoryAccessor().checkIndex(offset, size, internalType.size())
                .getInt(offset, internalType);
    }

    @Override
    final boolean getBoolean(int offset, InternalType internalType) {
        return getMemoryAccessor().checkIndex(offset, size, internalType.size())
                .getBoolean(offset, internalType);
    }

    @Override
    public final void putBytes(int offset, byte[] bytes, int off, int len) {
        getMemoryAccessor().checkIndex(offset, size, len)
                .putBytes(offset, bytes, off, len);
    }

    @Override
    public final void getBytes(int offset, byte[] bytes, int off, int len) {
        getMemoryAccessor().checkIndex(offset, size, len)
                .getBytes(offset, bytes, off, len);
    }

    @Override
    public final void getShortArray(int offset, short[] array, int off, int len) {
        getMemoryAccessor().checkIndex(offset, size, len, Short.BYTES)
                .getShortArray(offset, array, off, len);
    }

    @Override
    public final void putShortArray(int offset, short[] array, int off, int len) {
        getMemoryAccessor().checkIndex(offset, size, len, Short.BYTES)
                .putShortArray(offset, array, off, len);
    }

    @Override
    public final void putCharArray(int offset, char[] array, int off, int len) {
        getMemoryAccessor().checkIndex(offset, size, len, Character.BYTES)
                .putCharArray(offset, array, off, len);
    }

    @Override
    public final void getCharArray(int offset, char[] array, int off, int len) {
        getMemoryAccessor().checkIndex(offset, size, len, Character.BYTES)
                .getCharArray(offset, array, off, len);
    }

    @Override
    public final void putIntArray(int offset, int[] array, int off, int len) {
        getMemoryAccessor().checkIndex(offset, size, len, Integer.BYTES)
                .putIntArray(offset, array, off, len);
    }

    @Override
    public final void getIntArray(int offset, int[] array, int off, int len) {
        getMemoryAccessor().checkIndex(offset, size, len, Integer.BYTES)
                .getIntArray(offset, array, off, len);
    }

    @Override
    public final void putLongArray(int offset, long[] array, int off, int len) {
        getMemoryAccessor().checkIndex(offset, size, len, Long.BYTES)
                .putLongArray(offset, array, off, len);
    }

    @Override
    public final void getLongArray(int offset, long[] array, int off, int len) {
        getMemoryAccessor().checkIndex(offset, size, len, Long.BYTES)
                .getLongArray(offset, array, off, len);
    }

    @Override
    public final void putFloatArray(int offset, float[] array, int off, int len) {
        getMemoryAccessor().checkIndex(offset, size, len, Float.BYTES)
                .putFloatArray(offset, array, off, len);
    }

    @Override
    public final void getFloatArray(int offset, float[] array, int off, int len) {
        getMemoryAccessor().checkIndex(offset, size, len, Float.BYTES)
                .getFloatArray(offset, array, off, len);
    }

    @Override
    public final void putDoubleArray(int offset, double[] array, int off, int len) {
        getMemoryAccessor().checkIndex(offset, size, len, Double.BYTES)
                .putDoubleArray(offset, array, off, len);
    }

    @Override
    public final void getDoubleArray(int offset, double[] array, int off, int len) {
        getMemoryAccessor().checkIndex(offset, size, len, Double.BYTES)
                .getDoubleArray(offset, array, off, len);
    }

    @Override
    public final void putStringUTF(int offset, @Nonnull String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        int len = bytes.length;
        // we must check for offset might be negative
        MemoryAccessor ma = getMemoryAccessor();
        ma.checkIndex(offset, size, len + 1)
                // call ma.put* without range check
                .putInt(offset + len, BuiltinType.SINT8, 0);
        ma.putBytes(offset, bytes, 0, len);
    }

    @Nonnull
    @Override
    public final String getStringUTF(int offset) {
        return getMemoryAccessor().checkIndex(offset, size, 1)
                .getStringUTFN(getMemoryAccessor().address() + offset, size);
    }

    @Override
    public final void putString16(int offset, @Nonnull String value) {
        getMemoryAccessor().checkIndex(offset, size, value.length() + 1, Character.BYTES)
                .putString16(offset, value);
    }

    @Nonnull
    @Override
    public final String getString16(int offset) {
        return getMemoryAccessor().checkIndex(offset, size, Character.BYTES)
                .getStringChar16N(getMemoryAccessor().address() + offset, size);
    }

    @Nonnull
    @Override
    public Slice slice(int offset, int count) {
        getMemoryAccessor().checkIndex(offset, size, count);
        return new Slice(this, offset, count);
    }

}
