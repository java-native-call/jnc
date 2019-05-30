package jnc.foreign.internal;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nullable;

class NativeMethods implements NativeAccessor {

    private static final ReentrantLock lock = new ReentrantLock();
    private static final List<Runnable> ON_UNLOAD = new ArrayList<>(4);

    // access by native method
    @SuppressWarnings({"unused", "CollectionsToArray"})
    private static void onUnload() {
        Runnable[] array = {};
        lock.lock();
        try {
            array = ON_UNLOAD.toArray(array);
        } finally {
            lock.unlock();
        }
        for (int i = array.length - 1; i >= 0; --i) {
            array[i].run();
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
    public final native String getStringUTF(long address);

    @Override
    public final native String getStringUTFN(long address, long limit);

    @Override
    public final native void putStringChar16(long address, String value);

    @Override
    public final native String getStringChar16(long address);

    @Override
    public final native String getStringChar16N(long address, long limit);

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

    /**
     * {@inheritDoc}
     */
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
    public final native boolean invokeBoolean(long cif, long function, long base, @Nullable int[] offsets, Object obj, long methodId);

    @Override
    public final native int invokeInt(long cif, long function, long base, @Nullable int[] offsets, Object obj, long methodId);

    @Override
    public final native void invokeVoid(long cif, long function, long base, @Nullable int[] offsets, Object obj, long methodId);

    @Override
    public final native long invokeLong(long cif, long function, long base, @Nullable int[] offsets, Object obj, long methodId);

    @Override
    public final native double invokeDouble(long cif, long function, long base, @Nullable int[] offsets, Object obj, long methodId);

    @Override
    public final native void invokeStruct(long cif, long function, long base, @Nullable int[] offsets, long struct, Object obj, long methodId);

    @Override
    public final native long getMethodId(Method method);

    @Override
    public final Set<Runnable> onFinalize(Set<Runnable> set) {
        Objects.requireNonNull(set);
        lock.lock();
        try {
            ON_UNLOAD.add(() -> {
                for (Iterator<Runnable> it = set.iterator(); it.hasNext(); it.remove()) {
                    try {
                        it.next().run();
                    } catch (Throwable ignored) {
                    }
                }
            });
        } finally {
            lock.unlock();
        }
        return set;
    }

}
