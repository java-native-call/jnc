/*
 * Copyright 2017 zhanhb.
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
package jnc.provider;

import java.nio.charset.Charset;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import jnc.foreign.NativeType;
import jnc.foreign.Pointer;

@ParametersAreNonnullByDefault
@NotFinal(NotFinal.Reason.EXTENSION_PRESENT)
class SizedDirectMemory extends Memory {

    // not private
    // access by class Slice
    static void checkSliceRange(long capacity, int beginIndex, int endIndex) {
        if (beginIndex < 0 || endIndex > capacity || beginIndex > endIndex) {
            String msg = String.format("begin=%s,end=%s,capacity=%s", beginIndex, endIndex, capacity);
            throw new IndexOutOfBoundsException(msg);
        }
    }

    /**
     * check if specified {@code size} can be put in the specified
     * {@code offset}
     *
     * @param capacity capacity of the memory in bytes
     * @param offset the offset of the memory to put into
     * @param size the size of the object prepare to put
     */
    private static void checkSize(long capacity, int offset, int size) {
        if (offset < 0 || offset > capacity - size) {
            String format = "capacity of this pointer is %s, but trying to put an object with size=%s at position %s";
            String msg = String.format(format, capacity, size, offset);
            throw new IndexOutOfBoundsException(msg);
        }
    }

    private static void checkArrayIndex(long capacity, int offset, int len, int unit, String typeMsg) {
        if (offset < 0 || offset > capacity - (long) len * unit) {
            String msg = String.format("access(offset=%s,size=%s)[%s*%s]", offset, capacity, typeMsg, len);
            throw new IndexOutOfBoundsException(msg);
        }
    }

    private final long capacity;

    SizedDirectMemory(long address, long capacity) {
        super(new MemoryAccessor(address));
        this.capacity = capacity;
    }

    @Override
    public final long address() {
        return accessor().address();
    }

    @Override
    public final int size() {
        long cap = capacity;
        return cap > Integer.MAX_VALUE ? Integer.MAX_VALUE : (int) cap;
    }

    @Override
    public final long capacity() {
        return capacity;
    }

    /**
     * Do not rely on the String presentation, maybe changed in the future.
     */
    @Override
    public final String toString() {
        return "[" + getClass().getSimpleName() + "#" + Long.toHexString(address()) + ",size=" + capacity + "]";
    }

    @Override
    public final void putByte(int offset, byte value) {
        checkSize(capacity, offset, Byte.BYTES);
        accessor().putByte(offset, value);
    }

    @Override
    public final void putShort(int offset, short value) {
        checkSize(capacity, offset, Short.BYTES);
        accessor().putShort(offset, value);
    }

    @Override
    public final void putInt(int offset, int value) {
        checkSize(capacity, offset, Integer.BYTES);
        accessor().putInt(offset, value);
    }

    @Override
    public final void putLong(int offset, long value) {
        checkSize(capacity, offset, Long.BYTES);
        accessor().putLong(offset, value);
    }

    @Override
    public final void putFloat(int offset, float value) {
        checkSize(capacity, offset, Float.BYTES);
        accessor().putFloat(offset, value);
    }

    @Override
    public final void putDouble(int offset, double value) {
        checkSize(capacity, offset, Double.BYTES);
        accessor().putDouble(offset, value);
    }

    @Override
    public final byte getByte(int offset) {
        checkSize(capacity, offset, Byte.BYTES);
        return accessor().getByte(offset);
    }

    @Override
    public final short getShort(int offset) {
        checkSize(capacity, offset, Short.BYTES);
        return accessor().getShort(offset);
    }

    @Override
    public final int getInt(int offset) {
        checkSize(capacity, offset, Integer.BYTES);
        return accessor().getInt(offset);
    }

    @Override
    public final long getLong(int offset) {
        checkSize(capacity, offset, Long.BYTES);
        return accessor().getLong(offset);
    }

    @Override
    public final float getFloat(int offset) {
        checkSize(capacity, offset, Float.BYTES);
        return accessor().getFloat(offset);
    }

    @Override
    public final double getDouble(int offset) {
        checkSize(capacity, offset, Double.BYTES);
        return accessor().getDouble(offset);
    }

    @Override
    final void putDouble(int offset, InternalType internalType, double value) {
        checkSize(capacity, offset, internalType.size());
        accessor().putDouble(offset, internalType, value);
    }

    @Override
    final void putFloat(int offset, InternalType internalType, float value) {
        checkSize(capacity, offset, internalType.size());
        accessor().putFloat(offset, internalType, value);
    }

    @Override
    final void putInt(int offset, InternalType internalType, int value) {
        checkSize(capacity, offset, internalType.size());
        accessor().putInt(offset, internalType, value);
    }

    @Override
    final void putLong(int offset, InternalType internalType, long value) {
        checkSize(capacity, offset, internalType.size());
        accessor().putLong(offset, internalType, value);
    }

    @Override
    final double getDouble(int offset, InternalType internalType) {
        checkSize(capacity, offset, internalType.size());
        return accessor().getDouble(offset, internalType);
    }

    @Override
    final float getFloat(int offset, InternalType internalType) {
        checkSize(capacity, offset, internalType.size());
        return accessor().getFloat(offset, internalType);
    }

    @Override
    final long getLong(int offset, InternalType internalType) {
        checkSize(capacity, offset, internalType.size());
        return accessor().getLong(offset, internalType);
    }

    @Override
    final int getInt(int offset, InternalType internalType) {
        checkSize(capacity, offset, internalType.size());
        return accessor().getInt(offset, internalType);
    }

    @Override
    final boolean getBoolean(int offset, InternalType internalType) {
        checkSize(capacity, offset, internalType.size());
        return accessor().getBoolean(offset, internalType);
    }

    @Override
    public final void putBytes(int offset, byte[] bytes, int off, int len) {
        checkArrayIndex(capacity, offset, len, Byte.BYTES, "byte");
        accessor().putBytes(offset, bytes, off, len);
    }

    @Override
    public final void getBytes(int offset, byte[] bytes, int off, int len) {
        checkArrayIndex(capacity, offset, len, Byte.BYTES, "byte");
        accessor().getBytes(offset, bytes, off, len);
    }

    @Override
    public final void getShortArray(int offset, short[] array, int off, int len) {
        checkArrayIndex(capacity, offset, len, Short.BYTES, "short");
        accessor().getShortArray(offset, array, off, len);
    }

    @Override
    public final void putShortArray(int offset, short[] array, int off, int len) {
        checkArrayIndex(capacity, offset, len, Short.BYTES, "short");
        accessor().putShortArray(offset, array, off, len);
    }

    @Override
    public final void putCharArray(int offset, char[] array, int off, int len) {
        checkArrayIndex(capacity, offset, len, Character.BYTES, "char");
        accessor().putCharArray(offset, array, off, len);
    }

    @Override
    public final void getCharArray(int offset, char[] array, int off, int len) {
        checkArrayIndex(capacity, offset, len, Character.BYTES, "char");
        accessor().getCharArray(offset, array, off, len);
    }

    @Override
    public final void putIntArray(int offset, int[] array, int off, int len) {
        checkArrayIndex(capacity, offset, len, Integer.BYTES, "int");
        accessor().putIntArray(offset, array, off, len);
    }

    @Override
    public final void getIntArray(int offset, int[] array, int off, int len) {
        checkArrayIndex(capacity, offset, len, Integer.BYTES, "int");
        accessor().getIntArray(offset, array, off, len);
    }

    @Override
    public final void putLongArray(int offset, long[] array, int off, int len) {
        checkArrayIndex(capacity, offset, len, Long.BYTES, "long");
        accessor().putLongArray(offset, array, off, len);
    }

    @Override
    public final void getLongArray(int offset, long[] array, int off, int len) {
        checkArrayIndex(capacity, offset, len, Long.BYTES, "long");
        accessor().getLongArray(offset, array, off, len);
    }

    @Override
    public final void putFloatArray(int offset, float[] array, int off, int len) {
        checkArrayIndex(capacity, offset, len, Float.BYTES, "float");
        accessor().putFloatArray(offset, array, off, len);
    }

    @Override
    public final void getFloatArray(int offset, float[] array, int off, int len) {
        checkArrayIndex(capacity, offset, len, Float.BYTES, "float");
        accessor().getFloatArray(offset, array, off, len);
    }

    @Override
    public final void putDoubleArray(int offset, double[] array, int off, int len) {
        checkArrayIndex(capacity, offset, len, Double.BYTES, "double");
        accessor().putDoubleArray(offset, array, off, len);
    }

    @Override
    public final void getDoubleArray(int offset, double[] array, int off, int len) {
        checkArrayIndex(capacity, offset, len, Double.BYTES, "double");
        accessor().getDoubleArray(offset, array, off, len);
    }

    private void checkPutStringCapacity(long limit, int offset, int require) {
        if (offset < 0 || offset > limit - require) {
            String format = "capacity of this pointer is %s, but require %s to process the string at position %s";
            String msg = String.format(format, limit, require, offset);
            throw new IndexOutOfBoundsException(msg);
        }
    }

    @Override
    public final void putStringUTF(int offset, @Nonnull String value) {
        // TODO assume time complexity of getStringUTFLength is O(1) ?? 
        //  maybe we should cache the length
        int utfLength = NativeLoader.getAccessor().getStringUTFLength(value);
        checkPutStringCapacity(capacity, offset, utfLength + 1);
        accessor().putStringUTF(offset, value);
    }

    @Nonnull
    @Override
    public final String getStringUTF(int offset) {
        if (offset < 0 || offset > capacity) {
            throw new IndexOutOfBoundsException();
        }
        return accessor().getStringUTF(offset, capacity - offset);
    }

    @Override
    final void putStringImpl(int offset, byte[] bytes, int terminatorLength) {
        checkPutStringCapacity(capacity, offset, bytes.length + terminatorLength);
        StringCoding.put(accessor(), offset, bytes, terminatorLength);
    }

    @Override
    final String getStringImpl(int offset, Charset charset) {
        if (offset < 0 || offset > capacity) {
            throw new IndexOutOfBoundsException();
        }
        return StringCoding.get(accessor(), offset, charset, capacity - offset);
    }

    @Override
    final void putString16(int offset, @Nonnull String value) {
        checkPutStringCapacity(capacity, offset, (value.length() + 1) * Character.BYTES);
        accessor().putString16(offset, value);
    }

    @Nonnull
    @Override
    final String getString16(int offset) {
        if (offset < 0 || offset > capacity) {
            throw new IndexOutOfBoundsException();
        }
        return accessor().getString16(offset, capacity - offset);
    }

    @NotFinal(NotFinal.Reason.EXTENSION_PRESENT)
    @Nonnull
    @Override
    public Slice slice(int beginIndex, int endIndex) {
        checkSliceRange(capacity, beginIndex, endIndex);
        return new Slice(this, beginIndex, endIndex - beginIndex);
    }

    @Nullable
    @Override
    public final Pointer getPointer(int offset) {
        checkSize(capacity, offset, DefaultForeign.INSTANCE.getTypeFactory().findByNativeType(NativeType.POINTER).size());
        return UnboundedDirectMemory.of(accessor().getAddress(offset));
    }

    @Override
    public final void putPointer(int offset, @Nullable Pointer pointer) {
        checkSize(capacity, offset, DefaultForeign.INSTANCE.getTypeFactory().findByNativeType(NativeType.POINTER).size());
        accessor().putAddress(offset, pointer != null ? pointer.address() : 0);
    }

}
