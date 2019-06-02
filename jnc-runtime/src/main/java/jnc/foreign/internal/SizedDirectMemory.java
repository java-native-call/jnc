package jnc.foreign.internal;

import java.nio.charset.StandardCharsets;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import jnc.foreign.NativeType;
import jnc.foreign.Pointer;

class SizedDirectMemory extends Memory {

    private final long size;

    SizedDirectMemory(long address, long size) {
        super(new MemoryAccessor(address));
        this.size = size;
    }

    @Override
    public final long address() {
        return getAccessor().address();
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
    public final void putByte(int offset, byte value) {
        getAccessor().checkSize(size, offset, Byte.BYTES)
                .putByte(offset, value);
    }

    @Override
    public final void putChar(int offset, char value) {
        getAccessor().checkSize(size, offset, Character.BYTES)
                .putShort(offset, (short) value);
    }

    @Override
    public final void putShort(int offset, short value) {
        getAccessor().checkSize(size, offset, Short.BYTES)
                .putShort(offset, value);
    }

    @Override
    public final void putInt(int offset, int value) {
        getAccessor().checkSize(size, offset, Integer.BYTES)
                .putInt(offset, value);
    }

    @Override
    public final void putLong(int offset, long value) {
        getAccessor().checkSize(size, offset, Long.BYTES)
                .putLong(offset, value);
    }

    @Override
    public final void putFloat(int offset, float value) {
        getAccessor().checkSize(size, offset, Float.BYTES)
                .putFloat(offset, value);
    }

    @Override
    public final void putDouble(int offset, double value) {
        getAccessor().checkSize(size, offset, Double.BYTES)
                .putDouble(offset, value);
    }

    @Override
    public final byte getByte(int offset) {
        return getAccessor().checkSize(size, offset, Byte.BYTES)
                .getByte(offset);
    }

    @Override
    public final short getShort(int offset) {
        return getAccessor().checkSize(size, offset, Short.BYTES)
                .getShort(offset);
    }

    @Override
    public final char getChar(int offset) {
        return (char) getAccessor().checkSize(size, offset, Character.BYTES)
                .getShort(offset);
    }

    @Override
    public final int getInt(int offset) {
        return getAccessor().checkSize(size, offset, Integer.BYTES)
                .getInt(offset);
    }

    @Override
    public final long getLong(int offset) {
        return getAccessor().checkSize(size, offset, Long.BYTES)
                .getLong(offset);
    }

    @Override
    public final float getFloat(int offset) {
        return getAccessor().checkSize(size, offset, Float.BYTES)
                .getFloat(offset);
    }

    @Override
    public final double getDouble(int offset) {
        return getAccessor().checkSize(size, offset, Double.BYTES)
                .getDouble(offset);
    }

    @Override
    final void putDouble(int offset, InternalType internalType, double value) {
        getAccessor().checkSize(size, offset, internalType.size())
                .putDouble(offset, internalType, value);
    }

    @Override
    final void putFloat(int offset, InternalType internalType, float value) {
        getAccessor().checkSize(size, offset, internalType.size())
                .putFloat(offset, internalType, value);
    }

    @Override
    final void putBoolean(int offset, InternalType internalType, boolean value) {
        getAccessor().checkSize(size, offset, internalType.size())
                .putBoolean(offset, internalType, value);
    }

    @Override
    final void putInt(int offset, InternalType internalType, int value) {
        getAccessor().checkSize(size, offset, internalType.size())
                .putInt(offset, internalType, value);
    }

    @Override
    final void putLong(int offset, InternalType internalType, long value) {
        getAccessor().checkSize(size, offset, internalType.size())
                .putLong(offset, internalType, value);
    }

    @Override
    final double getDouble(int offset, InternalType internalType) {
        return getAccessor().checkSize(size, offset, internalType.size())
                .getDouble(offset, internalType);
    }

    @Override
    final float getFloat(int offset, InternalType internalType) {
        return getAccessor().checkSize(size, offset, internalType.size())
                .getFloat(offset, internalType);
    }

    @Override
    final long getLong(int offset, InternalType internalType) {
        return getAccessor().checkSize(size, offset, internalType.size())
                .getLong(offset, internalType);
    }

    @Override
    final int getInt(int offset, InternalType internalType) {
        return getAccessor().checkSize(size, offset, internalType.size())
                .getInt(offset, internalType);
    }

    @Override
    final boolean getBoolean(int offset, InternalType internalType) {
        return getAccessor().checkSize(size, offset, internalType.size())
                .getBoolean(offset, internalType);
    }

    @Override
    public final void putBytes(int offset, byte[] bytes, int off, int len) {
        getAccessor().checkArrayIndex(size, offset, len, Byte.BYTES, "byte")
                .putBytes(offset, bytes, off, len);
    }

    @Override
    public final void getBytes(int offset, byte[] bytes, int off, int len) {
        getAccessor().checkArrayIndex(size, offset, len, Byte.BYTES, "byte")
                .getBytes(offset, bytes, off, len);
    }

    @Override
    public final void getShortArray(int offset, short[] array, int off, int len) {
        getAccessor().checkArrayIndex(size, offset, len, Short.BYTES, "short")
                .getShortArray(offset, array, off, len);
    }

    @Override
    public final void putShortArray(int offset, short[] array, int off, int len) {
        getAccessor().checkArrayIndex(size, offset, len, Short.BYTES, "short")
                .putShortArray(offset, array, off, len);
    }

    @Override
    public final void putCharArray(int offset, char[] array, int off, int len) {
        getAccessor().checkArrayIndex(size, offset, len, Character.BYTES, "char")
                .putCharArray(offset, array, off, len);
    }

    @Override
    public final void getCharArray(int offset, char[] array, int off, int len) {
        getAccessor().checkArrayIndex(size, offset, len, Character.BYTES, "char")
                .getCharArray(offset, array, off, len);
    }

    @Override
    public final void putIntArray(int offset, int[] array, int off, int len) {
        getAccessor().checkArrayIndex(size, offset, len, Integer.BYTES, "int")
                .putIntArray(offset, array, off, len);
    }

    @Override
    public final void getIntArray(int offset, int[] array, int off, int len) {
        getAccessor().checkArrayIndex(size, offset, len, Integer.BYTES, "int")
                .getIntArray(offset, array, off, len);
    }

    @Override
    public final void putLongArray(int offset, long[] array, int off, int len) {
        getAccessor().checkArrayIndex(size, offset, len, Long.BYTES, "long")
                .putLongArray(offset, array, off, len);
    }

    @Override
    public final void getLongArray(int offset, long[] array, int off, int len) {
        getAccessor().checkArrayIndex(size, offset, len, Long.BYTES, "long")
                .getLongArray(offset, array, off, len);
    }

    @Override
    public final void putFloatArray(int offset, float[] array, int off, int len) {
        getAccessor().checkArrayIndex(size, offset, len, Float.BYTES, "float")
                .putFloatArray(offset, array, off, len);
    }

    @Override
    public final void getFloatArray(int offset, float[] array, int off, int len) {
        getAccessor().checkArrayIndex(size, offset, len, Float.BYTES, "float")
                .getFloatArray(offset, array, off, len);
    }

    @Override
    public final void putDoubleArray(int offset, double[] array, int off, int len) {
        getAccessor().checkArrayIndex(size, offset, len, Double.BYTES, "double")
                .putDoubleArray(offset, array, off, len);
    }

    @Override
    public final void getDoubleArray(int offset, double[] array, int off, int len) {
        getAccessor().checkArrayIndex(size, offset, len, Double.BYTES, "double")
                .getDoubleArray(offset, array, off, len);
    }

    @Override
    public final void putStringUTF(int offset, @Nonnull String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        int len = bytes.length;
        // we must check for offset might be negative
        MemoryAccessor ma = getAccessor();
        ma.checkSize(size, offset, len + 1)
                // call ma.put* without range check
                .putByte(offset + len, (byte) 0);
        ma.putBytes(offset, bytes, 0, len);
    }

    @Nonnull
    @Override
    public final String getStringUTF(int offset) {
        MemoryAccessor ma = getAccessor();
        return ma.checkSize(size, offset, 1)
                .getStringUTFN(ma.address() + offset, size);
    }

    @Override
    public final void putString16(int offset, @Nonnull String value) {
        getAccessor().checkArrayIndex(size, offset, value.length() + 1, Character.BYTES, "byte")
                .putString16(offset, value);
    }

    @Nonnull
    @Override
    public final String getString16(int offset) {
        MemoryAccessor ma = getAccessor();
        return ma.checkSize(size, offset, Character.BYTES)
                .getStringChar16N(ma.address() + offset, size);
    }

    @Nonnull
    @Override
    public Slice slice(int beginIndex, int endIndex) {
        MemoryAccessor.checkRange(size, beginIndex, endIndex);
        return new Slice(this, beginIndex, endIndex - beginIndex);
    }

    @Nullable
    @Override
    public final Pointer getPointer(int offset) {
        return UnboundedDirectMemory.of(
                getAccessor()
                        .checkSize(size, offset, DefaultForeign.INSTANCE.getTypeFactory().findByNativeType(NativeType.POINTER).size())
                        .getAddress(offset)
        );
    }

    @Override
    public final void putPointer(int offset, @Nullable Pointer pointer) {
        getAccessor()
                .checkSize(size, offset, DefaultForeign.INSTANCE.getTypeFactory().findByNativeType(NativeType.POINTER).size())
                .putAddress(offset, pointer != null ? pointer.address() : 0);
    }

}
