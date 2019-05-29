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

    private static final NativeAccessor NA = NativeLoader.getAccessor();

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
        return NA.getRawByte(address + offset);
    }

    void putByte(int offset, byte value) {
        NA.putRawByte(address + offset, value);
    }

    short getShort(int offset) {
        return NA.getRawShort(address + offset);
    }

    void putShort(int offset, short value) {
        NA.putRawShort(address + offset, value);
    }

    int getInt(int offset) {
        return NA.getRawInt(address + offset);
    }

    void putInt(int offset, int value) {
        NA.putRawInt(address + offset, value);
    }

    long getLong(int offset) {
        return NA.getRawLong(address + offset);
    }

    void putLong(int offset, long value) {
        NA.putRawLong(address + offset, value);
    }

    float getFloat(int offset) {
        return NA.getRawFloat(address + offset);
    }

    void putFloat(int offset, float value) {
        NA.putRawFloat(address + offset, value);
    }

    double getDouble(int offset) {
        return NA.getRawDouble(address + offset);
    }

    void putDouble(int offset, double value) {
        NA.putRawDouble(address + offset, value);
    }

    long getAddress(int offset) {
        return NA.getRawAddress(address + offset);
    }

    void putAddress(int offset, long value) {
        NA.putRawAddress(address + offset, value);
    }

    void putBoolean(int offset, InternalType internalType, boolean value) {
        putInt(offset, internalType, value ? 0 : 1);
    }

    void putInt(int offset, InternalType internalType, int value) {
        NA.putInt(address + offset, internalType.address(), value);
    }

    void putLong(int offset, InternalType internalType, long value) {
        NA.putLong(address + offset, internalType.address(), value);
    }

    void putFloat(int offset, InternalType internalType, float value) {
        NA.putFloat(address + offset, internalType.address(), value);
    }

    void putDouble(int offset, InternalType internalType, double value) {
        NA.putDouble(address + offset, internalType.address(), value);
    }

    boolean getBoolean(int offset, InternalType internalType) {
        return NA.getBoolean(address + offset, internalType.address());
    }

    int getInt(int offset, InternalType internalType) {
        return NA.getInt(address + offset, internalType.address());
    }

    long getLong(int offset, InternalType internalType) {
        return NA.getLong(address + offset, internalType.address());
    }

    float getFloat(int offset, InternalType internalType) {
        return NA.getFloat(address + offset, internalType.address());
    }

    double getDouble(int offset, InternalType internalType) {
        return NA.getDouble(address + offset, internalType.address());
    }

    void putBytes(int offset, byte[] bytes, int off, int len) {
        NA.putBytes(address + offset, bytes, off, len);
    }

    void getBytes(int offset, byte[] bytes, int off, int len) {
        NA.getBytes(address + offset, bytes, off, len);
    }

    void getShortArray(int offset, short[] array, int off, int len) {
        NA.getShortArray(address + offset, array, off, len);
    }

    void putShortArray(int offset, short[] array, int off, int len) {
        NA.putShortArray(address + offset, array, off, len);
    }

    void getCharArray(int offset, char[] array, int off, int len) {
        NA.getCharArray(address + offset, array, off, len);
    }

    void putCharArray(int offset, char[] array, int off, int len) {
        NA.putCharArray(address + offset, array, off, len);
    }

    void getIntArray(int offset, int[] array, int off, int len) {
        NA.getIntArray(address + offset, array, off, len);
    }

    void putIntArray(int offset, int[] array, int off, int len) {
        NA.putIntArray(address + offset, array, off, len);
    }

    void getLongArray(int offset, long[] array, int off, int len) {
        NA.getLongArray(address + offset, array, off, len);
    }

    void putLongArray(int offset, long[] array, int off, int len) {
        NA.putLongArray(address + offset, array, off, len);
    }

    void getFloatArray(int offset, float[] array, int off, int len) {
        NA.getFloatArray(address + offset, array, off, len);
    }

    void putFloatArray(int offset, float[] array, int off, int len) {
        NA.putFloatArray(address + offset, array, off, len);
    }

    void getDoubleArray(int offset, double[] array, int off, int len) {
        NA.getDoubleArray(address + offset, array, off, len);
    }

    void putDoubleArray(int offset, double[] array, int off, int len) {
        NA.putDoubleArray(address + offset, array, off, len);
    }

    void putStringUTF(int offset, @Nonnull String value) {
        NA.putStringUTF(address + offset, value);
    }

    @Nonnull
    String getStringUTF(int offset) {
        return NA.getStringUTF(address + offset);
    }

    void putString16(int offset, @Nonnull String value) {
        NA.putStringChar16(address + offset, value);
    }

    @Nonnull
    String getString16(int offset) {
        return NA.getStringChar16(address + offset);
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
        return NA.getStringUTFN(address, limit);
    }

    String getStringChar16N(long address, long limit) {
        return NA.getStringChar16N(address, limit);
    }

}
