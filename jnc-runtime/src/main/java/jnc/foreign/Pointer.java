package jnc.foreign;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Pointer {

    /**
     * @return address of {@code this} pointer
     */
    long address();

    /**
     * @param offset
     * @return treat specified offset of this pointer as address value and read
     * the data.
     */
    long getAddress(int offset);

    byte getByte(int offset);

    char getChar(int offset);

    double getDouble(int offset);

    float getFloat(int offset);

    int getInt(int offset);

    long getLong(int offset);

    short getShort(int offset);

    void putAddress(int offset, long value);

    void putByte(int offset, byte value);

    void putChar(int offset, char value);

    void putDouble(int offset, double value);

    void putFloat(int offset, float value);

    void putInt(int offset, int value);

    void putLong(int offset, long value);

    void putShort(int offset, short value);

    void getBytes(int offset, byte[] bytes, int off, int len);

    void putBytes(int offset, byte[] bytes, int off, int len);

    void getCharArray(int offset, char[] array, int off, int len);

    void putCharArray(int offset, char[] array, int off, int len);

    void getShortArray(int offset, short[] array, int off, int len);

    void putShortArray(int offset, short[] array, int off, int len);

    void getIntArray(int offset, int[] array, int off, int len);

    void putIntArray(int offset, int[] array, int off, int len);

    void getLongArray(int offset, long[] array, int off, int len);

    void putLongArray(int offset, long[] array, int off, int len);

    void getFloatArray(int offset, float[] array, int off, int len);

    void putFloatArray(int offset, float[] array, int off, int len);

    void getDoubleArray(int offset, double[] array, int off, int len);

    void putDoubleArray(int offset, double[] array, int off, int len);

    @Nonnull
    String getStringUTF(int offset);

    void putStringUTF(int offset, @Nonnull String value);

    @Nonnull
    String getString16(int offset);

    void putString16(int offset, @Nonnull String value);

    boolean getBoolean(int offset, Type type);

    byte getByte(int offset, Type type);

    short getShort(int offset, Type type);

    int getInt(int offset, Type type);

    long getLong(int offset, Type type);

    float getFloat(int offset, Type type);

    double getDouble(int offset, Type type);

    void putBoolean(int offset, Type type, boolean value);

    void putInt(int offset, Type type, int value);

    void putLong(int offset, Type type, long value);

    void putFloat(int offset, Type type, float value);

    void putDouble(int offset, Type type, double value);

    @Nullable
    Pointer getPointer(int offset);

    void putPointer(int offset, @Nullable Pointer pointer);

    @Nonnull
    Pointer slice(int offset, int count);

}
