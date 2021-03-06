package jnc.provider;

import java.nio.charset.Charset;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import jnc.foreign.Pointer;

@ParametersAreNonnullByDefault
final class UnboundedDirectMemory extends Memory implements NativeObject, Pointer {

    @Nullable
    static Pointer of(long address) {
        return address == 0 ? null : new UnboundedDirectMemory(address);
    }

    private UnboundedDirectMemory(long address) {
        super(new MemoryAccessor(address));
    }

    @Override
    public long address() {
        return getAccessor().address();
    }

    @Override
    public int size() {
        return Integer.MAX_VALUE;
    }

    @Override
    public long capacity() throws UnsupportedOperationException {
        throw new UnsupportedOperationException();
    }

    /**
     * Do not rely on the String presentation, maybe changed in the future.
     */
    @Override
    public String toString() {
        return "[" + getClass().getSimpleName() + "@" + Long.toHexString(getAccessor().address()) + ",]";
    }

    @Override
    public final void putByte(int offset, byte value) {
        getAccessor().putByte(offset, value);
    }

    @Override
    public final void putShort(int offset, short value) {
        getAccessor().putShort(offset, value);
    }

    @Override
    public final void putInt(int offset, int value) {
        getAccessor().putInt(offset, value);
    }

    @Override
    public final void putLong(int offset, long value) {
        getAccessor().putLong(offset, value);
    }

    @Override
    public final void putFloat(int offset, float value) {
        getAccessor().putFloat(offset, value);
    }

    @Override
    public final void putDouble(int offset, double value) {
        getAccessor().putDouble(offset, value);
    }

    @Override
    public final byte getByte(int offset) {
        return getAccessor().getByte(offset);
    }

    @Override
    public final short getShort(int offset) {
        return getAccessor().getShort(offset);
    }

    @Override
    public final int getInt(int offset) {
        return getAccessor().getInt(offset);
    }

    @Override
    public final long getLong(int offset) {
        return getAccessor().getLong(offset);
    }

    @Override
    public final float getFloat(int offset) {
        return getAccessor().getFloat(offset);
    }

    @Override
    public final double getDouble(int offset) {
        return getAccessor().getDouble(offset);
    }

    @Override
    void putInt(int offset, InternalType internalType, int value) {
        getAccessor().putInt(offset, internalType, value);
    }

    @Override
    void putLong(int offset, InternalType internalType, long value) {
        getAccessor().putLong(offset, internalType, value);
    }

    @Override
    void putFloat(int offset, InternalType internalType, float value) {
        getAccessor().putFloat(offset, internalType, value);
    }

    @Override
    void putDouble(int offset, InternalType internalType, double value) {
        getAccessor().putDouble(offset, internalType, value);
    }

    @Override
    boolean getBoolean(int offset, InternalType internalType) {
        return getAccessor().getBoolean(offset, internalType);
    }

    @Override
    int getInt(int offset, InternalType internalType) {
        return getAccessor().getInt(offset, internalType);
    }

    @Override
    long getLong(int offset, InternalType internalType) {
        return getAccessor().getLong(offset, internalType);
    }

    @Override
    float getFloat(int offset, InternalType internalType) {
        return getAccessor().getFloat(offset, internalType);
    }

    @Override
    double getDouble(int offset, InternalType internalType) {
        return getAccessor().getDouble(offset, internalType);
    }

    @Override
    void putStringImpl(int offset, byte[] bytes, int terminatorLength) {
        StringCoding.put(getAccessor(), offset, bytes, terminatorLength);
    }

    @Override
    String getStringImpl(int offset, Charset charset) {
        return StringCoding.get(getAccessor(), offset, charset, Long.MAX_VALUE);
    }

    @Override
    public void putBytes(int offset, byte[] bytes, int off, int len) {
        getAccessor().putBytes(offset, bytes, off, len);
    }

    @Override
    public void getBytes(int offset, byte[] bytes, int off, int len) {
        getAccessor().getBytes(offset, bytes, off, len);
    }

    @Override
    public void getShortArray(int offset, short[] array, int off, int len) {
        getAccessor().getShortArray(offset, array, off, len);
    }

    @Override
    public void putShortArray(int offset, short[] array, int off, int len) {
        getAccessor().putShortArray(offset, array, off, len);
    }

    @Override
    public void getCharArray(int offset, char[] array, int off, int len) {
        getAccessor().getCharArray(offset, array, off, len);
    }

    @Override
    public void putCharArray(int offset, char[] array, int off, int len) {
        getAccessor().putCharArray(offset, array, off, len);
    }

    @Override
    public void getIntArray(int offset, int[] array, int off, int len) {
        getAccessor().getIntArray(offset, array, off, len);
    }

    @Override
    public void putIntArray(int offset, int[] array, int off, int len) {
        getAccessor().putIntArray(offset, array, off, len);
    }

    @Override
    public void getLongArray(int offset, long[] array, int off, int len) {
        getAccessor().getLongArray(offset, array, off, len);
    }

    @Override
    public void putLongArray(int offset, long[] array, int off, int len) {
        getAccessor().putLongArray(offset, array, off, len);
    }

    @Override
    public void getFloatArray(int offset, float[] array, int off, int len) {
        getAccessor().getFloatArray(offset, array, off, len);
    }

    @Override
    public void putFloatArray(int offset, float[] array, int off, int len) {
        getAccessor().putFloatArray(offset, array, off, len);
    }

    @Override
    public void getDoubleArray(int offset, double[] array, int off, int len) {
        getAccessor().getDoubleArray(offset, array, off, len);
    }

    @Override
    public void putDoubleArray(int offset, double[] array, int off, int len) {
        getAccessor().putDoubleArray(offset, array, off, len);
    }

    @Override
    public void putStringUTF(int offset, @Nonnull String value) {
        getAccessor().putStringUTF(offset, value);
    }

    @Nonnull
    @Override
    public String getStringUTF(int offset) {
        return getAccessor().getStringUTF(offset, Long.MAX_VALUE);
    }

    @Override
    void putString16(int offset, @Nonnull String value) {
        getAccessor().putString16(offset, value);
    }

    @Nonnull
    @Override
    String getString16(int offset) {
        return getAccessor().getString16(offset, Long.MAX_VALUE);
    }

    /**
     * @return a direct memory of specified range, OK if offset is negative.
     * since we are unbounded.
     */
    @Nonnull
    @Override
    public Pointer slice(int beginIndex, int endIndex) {
        if (beginIndex > endIndex) {
            String msg = "begin " + beginIndex + ", end " + endIndex;
            throw new IndexOutOfBoundsException(msg);
        }
        return new Slice(this, beginIndex, endIndex - beginIndex);
    }

    @Nullable
    @Override
    public Pointer getPointer(int offset) {
        return UnboundedDirectMemory.of(getAccessor().getAddress(offset));
    }

    @Override
    public void putPointer(int offset, @Nullable Pointer pointer) {
        getAccessor().putAddress(offset, pointer != null ? pointer.address() : 0);
    }

}
