package jnc.foreign;

import java.nio.charset.Charset;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public interface Pointer {

    /**
     * @return address of {@code this} pointer
     */
    long address();

    byte getByte(int offset);

    char getChar(int offset);

    double getDouble(int offset);

    float getFloat(int offset);

    int getInt(int offset);

    long getLong(int offset);

    short getShort(int offset);

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

    /**
     * Call NewStringUTF with current Pointer. If this pointer is managed, value
     * will be truncated at the end of the pointer. The string is encoded in
     * modified UTF-8.
     *
     * Note: {@code p.getStringUTF(offset)} is not full equally to
     * {@code p.getString(offset, StandardCharsets.UTF_8)}. JNI use modified
     * UTF-8
     */
    @Nonnull
    String getStringUTF(int offset);

    /**
     * Call GetStringUTFRegion to get JNI format UTF-8 to this pointer. Null
     * terminated. The string is decoded in modified UTF-8.
     *
     * Note: {@code p.putStringUTF(offset)} is not full equally to
     * {@code p.getString(offset, StandardCharsets.UTF_8)}
     *
     * @throws IndexOutOfBoundsException If this pointer is managed and doesn't
     * have enough space to put the string
     */
    void putStringUTF(int offset, @Nonnull String value);

    /**
     * Read null terminated string in specified charset. Truncated if this
     * pointer is managed and no terminated sequence found when reach the end of
     * the pointer.
     *
     * @param offset the offset of this pointer
     * @throws NullPointerException if charset is null
     */
    @Nonnull
    String getString(int offset, @Nonnull Charset charset);

    /**
     * Put the String to this pointer in specified charset. Null Terminated.
     * If the string value has null terminated character, will be truncated when
     * read back with {@link #getString(int, Charset) getString}
     *
     * @param offset the offset of this pointer
     * @throws IndexOutOfBoundsException If this pointer is managed and doesn't
     * have enough space to put the string
     */
    void putString(int offset, @Nonnull String value, @Nonnull Charset charset);

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

    /**
     * Treat the value at {@code offset} of this Pointer as an address, read as
     * a pointer. The result is an unmanaged pointer.
     */
    @Nullable
    Pointer getPointer(int offset);

    void putPointer(int offset, @Nullable Pointer pointer);

    @Nonnull
    Pointer slice(int beginIndex, int endIndex);

}
