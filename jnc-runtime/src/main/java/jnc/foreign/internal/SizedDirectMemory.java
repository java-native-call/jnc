package jnc.foreign.internal;

import java.nio.charset.StandardCharsets;
import javax.annotation.Nonnull;
import jnc.foreign.Pointer;

class SizedDirectMemory extends DirectMemory {

    private static final NativeMethods nm = NativeMethods.getInstance();

    private final long size;

    SizedDirectMemory(long address, long size) {
        super(address);
        this.size = size;
    }

    public long size() {
        return size;
    }

    /**
     * Do not rely on the String presentation, maybe changed in the future.
     *
     * @return
     */
    @Override
    public String toString() {
        return "[" + getClass().getSimpleName() + "#" + Long.toHexString(address()) + ",size=" + size + "]";
    }

    void checkIndex(int offset, int len) {
        if (offset < 0 || offset > this.size - len) {
            throw new IndexOutOfBoundsException();
        }
    }

    private void checkIndex(int offset, int len, int size) {
        if (offset < 0 || offset > this.size - (long) len * size) {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    void putDouble(int offset, InternalType internalType, double value) {
        checkIndex(offset, internalType.size());
        super.putDouble(offset, internalType, value);
    }

    @Override
    void putFloat(int offset, InternalType internalType, float value) {
        checkIndex(offset, internalType.size());
        super.putFloat(offset, internalType, value);
    }

    @Override
    void putBoolean(int offset, InternalType internalType, boolean value) {
        checkIndex(offset, internalType.size());
        super.putBoolean(offset, internalType, value);
    }

    @Override
    void putInt(int offset, InternalType internalType, int value) {
        checkIndex(offset, internalType.size());
        super.putInt(offset, internalType, value);
    }

    @Override
    void putLong(int offset, InternalType internalType, long value) {
        checkIndex(offset, internalType.size());
        super.putLong(offset, internalType, value);
    }

    @Override
    double getDouble(int offset, InternalType internalType) {
        checkIndex(offset, internalType.size());
        return super.getDouble(offset, internalType);
    }

    @Override
    float getFloat(int offset, InternalType internalType) {
        checkIndex(offset, internalType.size());
        return super.getFloat(offset, internalType);
    }

    @Override
    long getLong(int offset, InternalType internalType) {
        checkIndex(offset, internalType.size());
        return super.getLong(offset, internalType);
    }

    @Override
    int getInt(int offset, InternalType internalType) {
        checkIndex(offset, internalType.size());
        return super.getInt(offset, internalType);
    }

    @Override
    boolean getBoolean(int offset, InternalType internalType) {
        checkIndex(offset, internalType.size());
        return super.getBoolean(offset, internalType);
    }

    @Override
    public void putBytes(int offset, byte[] bytes, int off, int len) {
        checkIndex(offset, len);
        super.putBytes(offset, bytes, off, len);
    }

    @Override
    public void getBytes(int offset, byte[] bytes, int off, int len) {
        checkIndex(offset, len);
        super.getBytes(offset, bytes, off, len);
    }

    @Override
    public void getShortArray(int offset, short[] array, int off, int len) {
        checkIndex(offset, len, Short.BYTES);
        super.getShortArray(offset, array, off, len);
    }

    @Override
    public void putShortArray(int offset, short[] array, int off, int len) {
        checkIndex(offset, len, Short.BYTES);
        super.putShortArray(offset, array, off, len);
    }

    @Override
    public void putCharArray(int offset, char[] array, int off, int len) {
        checkIndex(offset, len, Character.BYTES);
        super.putCharArray(offset, array, off, len);
    }

    @Override
    public void getCharArray(int offset, char[] array, int off, int len) {
        checkIndex(offset, len, Character.BYTES);
        super.getCharArray(offset, array, off, len);
    }

    @Override
    public void putIntArray(int offset, int[] array, int off, int len) {
        checkIndex(offset, len, Integer.BYTES);
        super.putIntArray(offset, array, off, len);
    }

    @Override
    public void getIntArray(int offset, int[] array, int off, int len) {
        checkIndex(offset, len, Integer.BYTES);
        super.getIntArray(offset, array, off, len);
    }

    @Override
    public void putLongArray(int offset, long[] array, int off, int len) {
        checkIndex(offset, len, Long.BYTES);
        super.putLongArray(offset, array, off, len);
    }

    @Override
    public void getLongArray(int offset, long[] array, int off, int len) {
        checkIndex(offset, len, Long.BYTES);
        super.getLongArray(offset, array, off, len);
    }

    @Override
    public void putFloatArray(int offset, float[] array, int off, int len) {
        checkIndex(offset, len, Float.BYTES);
        super.putFloatArray(offset, array, off, len);
    }

    @Override
    public void getFloatArray(int offset, float[] array, int off, int len) {
        checkIndex(offset, len, Float.BYTES);
        super.getFloatArray(offset, array, off, len);
    }

    @Override
    public void putDoubleArray(int offset, double[] array, int off, int len) {
        checkIndex(offset, len, Double.BYTES);
        super.putDoubleArray(offset, array, off, len);
    }

    @Override
    public void getDoubleArray(int offset, double[] array, int off, int len) {
        checkIndex(offset, len, Double.BYTES);
        super.getDoubleArray(offset, array, off, len);
    }

    @Override
    public void putStringUTF(int offset, String value) {
        byte[] bytes = value.getBytes(StandardCharsets.UTF_8);
        int len = bytes.length;
        // we must check for offset might be negative
        checkIndex(offset, len + 1);
        // call super.put* without range check
        super.putInt(offset + len, BuiltinType.SINT8, 0);
        super.putBytes(offset, bytes, 0, len);
    }

    @Nonnull
    @Override
    public String getStringUTF(int offset) {
        checkIndex(offset, 1);
        return nm.getStringUTFN(address() + offset, size);
    }

    @Nonnull
    @Override
    public Pointer slice(int offset, int size) {
        if (size < 0) {
            throw new IllegalArgumentException();
        }
        checkIndex(offset, size);
        return new Slice(this, offset, size);
    }

}
