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

import java.lang.annotation.Native;
import java.lang.reflect.Method;
import java.util.Map;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * @author zhanhb
 */
@ParametersAreNonnullByDefault
interface NativeAccessor {

    @Native
    int TYPE_VOID = 0;
    @Native
    int TYPE_FLOAT = 2;
    @Native
    int TYPE_DOUBLE = 3;
    @Native
    int TYPE_UINT8 = 5;
    @Native
    int TYPE_SINT8 = 6;
    @Native
    int TYPE_UINT16 = 7;
    @Native
    int TYPE_SINT16 = 8;
    @Native
    int TYPE_UINT32 = 9;
    @Native
    int TYPE_SINT32 = 10;
    @Native
    int TYPE_UINT64 = 11;
    @Native
    int TYPE_SINT64 = 12;
    @SuppressWarnings("unused")
    @Native
    int TYPE_STRUCT = 13;
    @Native
    int TYPE_POINTER = 14;

    @Native
    int RTLD_LAZY = 1;
    @Native
    int RTLD_NOW = 2;
    @Native
    @SuppressWarnings("unused")
    int RTLD_LOCAL = 4;
    @Native
    int RTLD_GLOBAL = 8;

    @Native
    int CONVENTION_DEFAULT = 0;
    @Native
    int CONVENTION_STDCALL = 1;

    /**
     * array index also indicate the type, has null entry
     *
     * @return [type_address, size&lt;&lt;32|align&lt;&lt;16|type]
     */
    long[][] getTypes();

    long dlopen(@Nullable String path, int mode) throws UnsatisfiedLinkError;

    long dlsym(long handle, String symbol) throws UnsatisfiedLinkError;

    void dlclose(long handle);

    int getJniVersion();

    byte getRawByte(long address);

    void putRawByte(long address, byte value);

    short getRawShort(long address);

    void putRawShort(long address, short value);

    int getRawInt(long address);

    void putRawInt(long address, int value);

    long getRawLong(long address);

    void putRawLong(long address, long value);

    float getRawFloat(long address);

    void putRawFloat(long address, float value);

    double getRawDouble(long address);

    void putRawDouble(long address, double value);

    void putInt(long address, long type, int value);

    void putLong(long address, long type, long value);

    void putFloat(long address, long type, float value);

    void putDouble(long address, long type, double value);

    boolean getBoolean(long address, long type);

    int getInt(long address, long type);

    long getLong(long address, long type);

    float getFloat(long address, long type);

    double getDouble(long address, long type);

    void putStringUTF(long address, String value);

    String getStringUTF(long address, long limit);

    void putStringChar16(long address, String value);

    String getStringChar16(long address, long limit);

    int getStringUTFLength(String value);

    int getStringLength(long address, long limit, int terminatorLength);

    void putRawAddress(long address, long value);

    long getRawAddress(long address);

    void getBytes(long address, byte[] bytes, int off, int len);

    void putBytes(long address, byte[] bytes, int off, int len);

    void getShortArray(long address, short[] bytes, int off, int len);

    void putShortArray(long address, short[] bytes, int off, int len);

    void getCharArray(long address, char[] bytes, int off, int len);

    void putCharArray(long address, char[] bytes, int off, int len);

    void getIntArray(long address, int[] bytes, int off, int len);

    void putIntArray(long address, int[] bytes, int off, int len);

    void getLongArray(long address, long[] bytes, int off, int len);

    void putLongArray(long address, long[] bytes, int off, int len);

    void getFloatArray(long address, float[] bytes, int off, int len);

    void putFloatArray(long address, float[] bytes, int off, int len);

    void getDoubleArray(long address, double[] bytes, int off, int len);

    void putDoubleArray(long address, double[] bytes, int off, int len);

    void initAlias(Map<String, Integer> map);

    /**
     * allocate clean memory of specified size
     *
     * @param size the size of memory to allocate
     * @return the address of the memory
     * @throws IllegalArgumentException size&lt;0
     */
    long allocateMemory(long size) throws OutOfMemoryError;

    /**
     * copy memory
     *
     * @throws IllegalArgumentException n&lt;0
     * @throws NullPointerException n&gt;0 and either dst or src is zero
     */
    @SuppressWarnings("unused")
    void copyMemory(long dst, long src, long n);

    void freeMemory(long address);

    int pageSize();

    /*
     * align<<32|size
     */
    default long getCifInfo() {
        return 0;
    }

    void prepareInvoke(long cif, int abi, int len, long retType, long atypes);

    void prepareInvokeVariadic(long cif, int abi, int fixedArgs, int totalArgs, long retType, long atypes);

    long invoke(long cif, long function, long base, @Nullable int[] offsets, Object obj, long methodId);

    @SuppressWarnings("unused")
    void invokeStruct(long cif, long function, long base, @Nullable int[] offsets, long struct, Object obj, long methodId);

    default long getMethodId(Method method) {
        return 0;
    }

    /*
     * action nullable for test. Will only be accessed by class Cleaner when published.
     */
    default boolean onFinalize(@Nullable Runnable action) {
        return false;
    }

}
