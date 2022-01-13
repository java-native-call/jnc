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
        return accessor().address();
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
        return "[" + getClass().getSimpleName() + "@" + Long.toHexString(accessor().address()) + ",]";
    }

    @Override
    public final void putByte(int offset, byte value) {
        accessor().putByte(offset, value);
    }

    @Override
    public final void putShort(int offset, short value) {
        accessor().putShort(offset, value);
    }

    @Override
    public final void putInt(int offset, int value) {
        accessor().putInt(offset, value);
    }

    @Override
    public final void putLong(int offset, long value) {
        accessor().putLong(offset, value);
    }

    @Override
    public final void putFloat(int offset, float value) {
        accessor().putFloat(offset, value);
    }

    @Override
    public final void putDouble(int offset, double value) {
        accessor().putDouble(offset, value);
    }

    @Override
    public final byte getByte(int offset) {
        return accessor().getByte(offset);
    }

    @Override
    public final short getShort(int offset) {
        return accessor().getShort(offset);
    }

    @Override
    public final int getInt(int offset) {
        return accessor().getInt(offset);
    }

    @Override
    public final long getLong(int offset) {
        return accessor().getLong(offset);
    }

    @Override
    public final float getFloat(int offset) {
        return accessor().getFloat(offset);
    }

    @Override
    public final double getDouble(int offset) {
        return accessor().getDouble(offset);
    }

    @Override
    void putInt(int offset, InternalType internalType, int value) {
        accessor().putInt(offset, internalType, value);
    }

    @Override
    void putLong(int offset, InternalType internalType, long value) {
        accessor().putLong(offset, internalType, value);
    }

    @Override
    void putFloat(int offset, InternalType internalType, float value) {
        accessor().putFloat(offset, internalType, value);
    }

    @Override
    void putDouble(int offset, InternalType internalType, double value) {
        accessor().putDouble(offset, internalType, value);
    }

    @Override
    boolean getBoolean(int offset, InternalType internalType) {
        return accessor().getBoolean(offset, internalType);
    }

    @Override
    int getInt(int offset, InternalType internalType) {
        return accessor().getInt(offset, internalType);
    }

    @Override
    long getLong(int offset, InternalType internalType) {
        return accessor().getLong(offset, internalType);
    }

    @Override
    float getFloat(int offset, InternalType internalType) {
        return accessor().getFloat(offset, internalType);
    }

    @Override
    double getDouble(int offset, InternalType internalType) {
        return accessor().getDouble(offset, internalType);
    }

    @Override
    void putStringImpl(int offset, byte[] bytes, int terminatorLength) {
        StringCoding.put(accessor(), offset, bytes, terminatorLength);
    }

    @Override
    String getStringImpl(int offset, Charset charset) {
        return StringCoding.get(accessor(), offset, charset, Long.MAX_VALUE);
    }

    @Override
    public void putBytes(int offset, byte[] bytes, int off, int len) {
        accessor().putBytes(offset, bytes, off, len);
    }

    @Override
    public void getBytes(int offset, byte[] bytes, int off, int len) {
        accessor().getBytes(offset, bytes, off, len);
    }

    @Override
    public void getShortArray(int offset, short[] array, int off, int len) {
        accessor().getShortArray(offset, array, off, len);
    }

    @Override
    public void putShortArray(int offset, short[] array, int off, int len) {
        accessor().putShortArray(offset, array, off, len);
    }

    @Override
    public void getCharArray(int offset, char[] array, int off, int len) {
        accessor().getCharArray(offset, array, off, len);
    }

    @Override
    public void putCharArray(int offset, char[] array, int off, int len) {
        accessor().putCharArray(offset, array, off, len);
    }

    @Override
    public void getIntArray(int offset, int[] array, int off, int len) {
        accessor().getIntArray(offset, array, off, len);
    }

    @Override
    public void putIntArray(int offset, int[] array, int off, int len) {
        accessor().putIntArray(offset, array, off, len);
    }

    @Override
    public void getLongArray(int offset, long[] array, int off, int len) {
        accessor().getLongArray(offset, array, off, len);
    }

    @Override
    public void putLongArray(int offset, long[] array, int off, int len) {
        accessor().putLongArray(offset, array, off, len);
    }

    @Override
    public void getFloatArray(int offset, float[] array, int off, int len) {
        accessor().getFloatArray(offset, array, off, len);
    }

    @Override
    public void putFloatArray(int offset, float[] array, int off, int len) {
        accessor().putFloatArray(offset, array, off, len);
    }

    @Override
    public void getDoubleArray(int offset, double[] array, int off, int len) {
        accessor().getDoubleArray(offset, array, off, len);
    }

    @Override
    public void putDoubleArray(int offset, double[] array, int off, int len) {
        accessor().putDoubleArray(offset, array, off, len);
    }

    @Override
    public void putStringUTF(int offset, @Nonnull String value) {
        accessor().putStringUTF(offset, value);
    }

    @Nonnull
    @Override
    public String getStringUTF(int offset) {
        return accessor().getStringUTF(offset, Long.MAX_VALUE);
    }

    @Override
    void putString16(int offset, @Nonnull String value) {
        accessor().putString16(offset, value);
    }

    @Nonnull
    @Override
    String getString16(int offset) {
        return accessor().getString16(offset, Long.MAX_VALUE);
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
        return UnboundedDirectMemory.of(accessor().getAddress(offset));
    }

    @Override
    public void putPointer(int offset, @Nullable Pointer pointer) {
        accessor().putAddress(offset, pointer != null ? pointer.address() : 0);
    }

}
