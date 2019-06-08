package jnc.provider;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
enum NativeMethods implements NativeAccessor {

    INSTANCE;

    private static final AtomicReference<Runnable> onFinalize = new AtomicReference<>();

    // access by native code
    @SuppressWarnings("unused")
    private static void onUnload() {
        Runnable runnable = onFinalize.getAndSet(null);
        if (runnable != null) {
            runnable.run();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public final native long[][] getTypes();

    @Override
    public final native long dlopen(@Nullable String path, int mode) throws UnsatisfiedLinkError;

    @Override
    public final native long dlsym(long handle, String symbol) throws UnsatisfiedLinkError;

    @Override
    public final native void dlclose(long handle);

    @Override
    public final native int getJniVersion();

    @Override
    public final native byte getRawByte(long address);

    @Override
    public final native void putRawByte(long address, byte value);

    @Override
    public final native short getRawShort(long address);

    @Override
    public final native void putRawShort(long address, short value);

    @Override
    public final native int getRawInt(long address);

    @Override
    public final native void putRawInt(long address, int value);

    @Override
    public final native long getRawLong(long address);

    @Override
    public final native void putRawLong(long address, long value);

    @Override
    public final native float getRawFloat(long address);

    @Override
    public final native void putRawFloat(long address, float value);

    @Override
    public final native double getRawDouble(long address);

    @Override
    public final native void putRawDouble(long address, double value);

    @Override
    public final native void putInt(long address, long type, int value);

    @Override
    public final native void putLong(long address, long type, long value);

    @Override
    public final native void putFloat(long address, long type, float value);

    @Override
    public final native void putDouble(long address, long type, double value);

    @Override
    public final native boolean getBoolean(long address, long type);

    @Override
    public final native int getInt(long address, long type);

    @Override
    public final native long getLong(long address, long type);

    @Override
    public final native float getFloat(long address, long type);

    @Override
    public final native double getDouble(long address, long type);

    @Override
    public final native void putStringUTF(long address, String value);

    @Override
    public final native String getStringUTF(long address, long limit);

    @Override
    public final native void putStringChar16(long address, String value);

    @Override
    public final native String getStringChar16(long address, long limit);

    @Override
    public final native int getStringUTFLength(String value);

    @Override
    public final native int getStringLength(long address, long limit, int terminatorLength);

    @Override
    public final native void putRawAddress(long address, long value);

    @Override
    public final native long getRawAddress(long address);

    @Override
    public final native void getBytes(long address, byte[] bytes, int off, int len);

    @Override
    public final native void putBytes(long address, byte[] bytes, int off, int len);

    @Override
    public final native void getShortArray(long address, short[] bytes, int off, int len);

    @Override
    public final native void putShortArray(long address, short[] bytes, int off, int len);

    @Override
    public final native void getCharArray(long address, char[] bytes, int off, int len);

    @Override
    public final native void putCharArray(long address, char[] bytes, int off, int len);

    @Override
    public final native void getIntArray(long address, int[] bytes, int off, int len);

    @Override
    public final native void putIntArray(long address, int[] bytes, int off, int len);

    @Override
    public final native void getLongArray(long address, long[] bytes, int off, int len);

    @Override
    public final native void putLongArray(long address, long[] bytes, int off, int len);

    @Override
    public final native void getFloatArray(long address, float[] bytes, int off, int len);

    @Override
    public final native void putFloatArray(long address, float[] bytes, int off, int len);

    @Override
    public final native void getDoubleArray(long address, double[] bytes, int off, int len);

    @Override
    public final native void putDoubleArray(long address, double[] bytes, int off, int len);

    @Override
    public final native void initAlias(Map<String, Integer> map);

    /**
     * {@inheritDoc}
     */
    @Override
    public final native long allocateMemory(long size) throws OutOfMemoryError;

    @Override
    public final native void copyMemory(long dst, long src, long n);

    @Override
    public final native void freeMemory(long address);

    @Override
    public final native int pageSize();

    /**
     * {@inheritDoc}
     */
    @Override
    public final native long getCifInfo();

    @Override
    public final native void prepareInvoke(long cif, int abi, int len, long retType, long atypes);

    @Override
    public final native void prepareInvokeVariadic(long cif, int abi, int fixedArgs, int totalArgs, long retType, long atypes);

    @Override
    public final native long invoke(long cif, long function, long base, @Nullable int[] offsets, Object obj, long methodId);

    @Override
    public final native void invokeStruct(long cif, long function, long base, @Nullable int[] offsets, long struct, Object obj, long methodId);

    @Override
    public final native long getMethodId(Method method);

    /**
     * Maybe the classloader instance is finalized before the lib, meanwhile the
     * native lib is also finalized. There's no guarantee who is finalized
     * first. Let it call our method onUnload to make sure these are finalized
     * before native library unloaded.
     *
     * @param action when deployed, should only be invoked by Cleaner, nullable
     * for test
     * @see Cleaner.Ref#cleanAll()
     * @see NativeMethods#onFinalize(java.lang.Runnable)
     * @return true if registered successfully
     */
    @Override
    public boolean onFinalize(@Nullable Runnable action) {
        return onFinalize.compareAndSet(null, action);
    }

}
