package jnc.foreign.internal;

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
    void putDouble(int offset, BuiltinType builtinType, double value) {
        checkIndex(offset, builtinType.size());
        super.putDouble(offset, builtinType, value);
    }

    @Override
    void putFloat(int offset, BuiltinType builtinType, float value) {
        checkIndex(offset, builtinType.size());
        super.putFloat(offset, builtinType, value);
    }

    @Override
    void putBoolean(int offset, BuiltinType builtinType, boolean value) {
        checkIndex(offset, builtinType.size());
        super.putBoolean(offset, builtinType, value);
    }

    @Override
    void putInt(int offset, BuiltinType builtinType, int value) {
        checkIndex(offset, builtinType.size());
        super.putInt(offset, builtinType, value);
    }

    @Override
    void putLong(int offset, BuiltinType builtinType, long value) {
        checkIndex(offset, builtinType.size());
        super.putLong(offset, builtinType, value);
    }

    @Override
    double getDouble(int offset, BuiltinType builtinType) {
        checkIndex(offset, builtinType.size());
        return super.getDouble(offset, builtinType);
    }

    @Override
    float getFloat(int offset, BuiltinType builtinType) {
        checkIndex(offset, builtinType.size());
        return super.getFloat(offset, builtinType);
    }

    @Override
    long getLong(int offset, BuiltinType builtinType) {
        checkIndex(offset, builtinType.size());
        return super.getLong(offset, builtinType);
    }

    @Override
    int getInt(int offset, BuiltinType builtinType) {
        checkIndex(offset, builtinType.size());
        return super.getInt(offset, builtinType);
    }

    @Override
    boolean getBoolean(int offset, BuiltinType builtinType) {
        checkIndex(offset, builtinType.size());
        return super.getBoolean(offset, builtinType);
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
        byte[] bytes = value.getBytes(Charsets.UTF_8);
        int len = bytes.length;
        // we must check for offset might be negative
        checkIndex(offset, len + 1);
        // call super.put* without range check
        super.putInt(offset + len, BuiltinType.SINT8, 0);
        super.putBytes(offset, bytes, 0, len);
    }

    @Override
    public String getStringUTF(int offset) {
        checkIndex(offset, 1);
        return nm.getStringUTFN(address() + offset, size);
    }

    @Override
    public Pointer slice(int offset, int size) {
        checkIndex(offset, size);
        return new Slice(this, offset, size);
    }

}
