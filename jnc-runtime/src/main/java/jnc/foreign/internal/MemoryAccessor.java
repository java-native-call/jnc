/*
 * Copyright 2019 zhanhb.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jnc.foreign.internal;

import javax.annotation.Nonnull;

/**
 *
 * @author zhanhb
 */
final class MemoryAccessor {

    private static final NativeMethods nm = NativeMethods.getInstance();

    private long address;

    MemoryAccessor(long address) {
        if (address == 0) {
            throw new NullPointerException();
        }
        this.address = address;
    }

    long address() {
        return address;
    }

    byte getByte(int offset) {
        return nm.getRawByte(address + offset);
    }

    void putByte(int offset, byte value) {
        nm.putRawByte(address + offset, value);
    }

    short getShort(int offset) {
        return nm.getRawShort(address + offset);
    }

    void putShort(int offset, short value) {
        nm.putRawShort(address + offset, value);
    }

    int getInt(int offset) {
        return nm.getRawInt(address + offset);
    }

    void putInt(int offset, int value) {
        nm.putRawInt(address + offset, value);
    }

    long getLong(int offset) {
        return nm.getRawLong(address + offset);
    }

    void putLong(int offset, long value) {
        nm.putRawLong(address + offset, value);
    }

    float getFloat(int offset) {
        return nm.getRawFloat(address + offset);
    }

    void putFloat(int offset, float value) {
        nm.putRawFloat(address + offset, value);
    }

    double getDouble(int offset) {
        return nm.getRawDouble(address + offset);
    }

    void putDouble(int offset, double value) {
        nm.putRawDouble(address + offset, value);
    }

    void putBoolean(int offset, InternalType internalType, boolean value) {
        nm.putInt(address + offset, internalType.address(), value ? 0 : 1);
    }

    void putInt(int offset, InternalType internalType, int value) {
        nm.putInt(address + offset, internalType.address(), value);
    }

    void putLong(int offset, InternalType internalType, long value) {
        nm.putLong(address + offset, internalType.address(), value);
    }

    void putFloat(int offset, InternalType internalType, float value) {
        nm.putFloat(address + offset, internalType.address(), value);
    }

    void putDouble(int offset, InternalType internalType, double value) {
        nm.putDouble(address + offset, internalType.address(), value);
    }

    boolean getBoolean(int offset, InternalType internalType) {
        return nm.getBoolean(address + offset, internalType.address());
    }

    int getInt(int offset, InternalType internalType) {
        return nm.getInt(address + offset, internalType.address());
    }

    long getLong(int offset, InternalType internalType) {
        return nm.getLong(address + offset, internalType.address());
    }

    float getFloat(int offset, InternalType internalType) {
        return nm.getFloat(address + offset, internalType.address());
    }

    double getDouble(int offset, InternalType internalType) {
        return nm.getDouble(address + offset, internalType.address());
    }

    void putBytes(int offset, byte[] bytes, int off, int len) {
        nm.putBytes(address + offset, bytes, off, len);
    }

    void getBytes(int offset, byte[] bytes, int off, int len) {
        nm.getBytes(address + offset, bytes, off, len);
    }

    void getShortArray(int offset, short[] array, int off, int len) {
        nm.getShortArray(address + offset, array, off, len);
    }

    void putShortArray(int offset, short[] array, int off, int len) {
        nm.putShortArray(address + offset, array, off, len);
    }

    void getCharArray(int offset, char[] array, int off, int len) {
        nm.getCharArray(address + offset, array, off, len);
    }

    void putCharArray(int offset, char[] array, int off, int len) {
        nm.putCharArray(address + offset, array, off, len);
    }

    void getIntArray(int offset, int[] array, int off, int len) {
        nm.getIntArray(address + offset, array, off, len);
    }

    void putIntArray(int offset, int[] array, int off, int len) {
        nm.putIntArray(address + offset, array, off, len);
    }

    void getLongArray(int offset, long[] array, int off, int len) {
        nm.getLongArray(address + offset, array, off, len);
    }

    void putLongArray(int offset, long[] array, int off, int len) {
        nm.putLongArray(address + offset, array, off, len);
    }

    void getFloatArray(int offset, float[] array, int off, int len) {
        nm.getFloatArray(address + offset, array, off, len);
    }

    void putFloatArray(int offset, float[] array, int off, int len) {
        nm.putFloatArray(address + offset, array, off, len);
    }

    void getDoubleArray(int offset, double[] array, int off, int len) {
        nm.getDoubleArray(address + offset, array, off, len);
    }

    void putDoubleArray(int offset, double[] array, int off, int len) {
        nm.putDoubleArray(address + offset, array, off, len);
    }

    void putStringUTF(int offset, @Nonnull String value) {
        nm.putStringUTF(address + offset, value);
    }

    @Nonnull
    String getStringUTF(int offset) {
        return nm.getStringUTF(address + offset);
    }

    void putString16(int offset, @Nonnull String value) {
        nm.putStringChar16(address + offset, value);
    }

    @Nonnull
    String getString16(int offset) {
        return nm.getStringChar16(address + offset);
    }

    MemoryAccessor checkIndex(int offset, long total, int len) {
        if (offset < 0 || offset > total - len) {
            throw new IndexOutOfBoundsException();
        }
        return this;
    }

    MemoryAccessor checkIndex(int offset, long total, int len, int unit) {
        if (offset < 0 || offset > total - (long) len * unit) {
            throw new IndexOutOfBoundsException();
        }
        return this;
    }

    String getStringUTFN(long address, long limit) {
        return nm.getStringUTFN(address, limit);
    }

    String getStringChar16N(long address, long limit) {
        return nm.getStringChar16N(address, limit);
    }

}
