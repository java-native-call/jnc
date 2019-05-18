package jnc.foreign.internal;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.Buffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;
import javax.annotation.Nullable;
import jnc.foreign.Arch;
import jnc.foreign.OS;
import jnc.foreign.Platform;

class NativeMethods {

    private static final NativeMethods theInstance = new NativeMethods();

    static final int TYPE_VOID = 0;
    static final int TYPE_INT = 1;
    static final int TYPE_FLOAT = 2;
    static final int TYPE_DOUBLE = 3;
    static final int TYPE_UINT8 = 5;
    static final int TYPE_SINT8 = 6;
    static final int TYPE_UINT16 = 7;
    static final int TYPE_SINT16 = 8;
    static final int TYPE_UINT32 = 9;
    static final int TYPE_SINT32 = 10;
    static final int TYPE_UINT64 = 11;
    static final int TYPE_SINT64 = 12;
    static final int TYPE_STRUCT = 13;
    static final int TYPE_POINTER = 14;

    static final int RTLD_LAZY = 1;
    static final int RTLD_NOW = 2;
    static final int RTLD_LOCAL = 4;
    static final int RTLD_GLOBAL = 8;

    static final int CONVENTION_DEFAULT = 0;
    static final int CONVENTION_STDCALL = 1;
    static final int CONVENTION_SYSV = 2;

    private static final ReentrantLock lock = new ReentrantLock();
    private static final List<Runnable> ON_UNLOAD = new ArrayList<>(4);

    static {
        String libPath = getLibPath();
        URL url = NativeMethods.class.getClassLoader().getResource(libPath);
        if (url == null) {
            throw new UnsatisfiedLinkError(libPath);
        }
        load(url);
    }

    public static NativeMethods getInstance() {
        return theInstance;
    }

    @SuppressWarnings("NestedAssignment")
    private static void load(URL url) {
        try {
            if ("file".equalsIgnoreCase(url.getProtocol())) {
                System.load(new File(url.toURI()).getPath());
            } else {
                Path tmp = Files.createTempFile("lib", System.mapLibraryName("jnc"));
                try {
                    try (InputStream is = url.openStream()) {
                        Files.copy(is, tmp, StandardCopyOption.REPLACE_EXISTING);
                    }
                    System.load(tmp.toAbsolutePath().toString());
                } finally {
                    try {
                        Files.delete(tmp);
                    } catch (IOException ignored) {
                    }
                }
            }
        } catch (IOException | URISyntaxException ex) {
            throw new ExceptionInInitializerError(ex);
        }
    }

    private static String getLibPath() {
        String prefix = NativeMethods.class.getPackage().getName().replace(".", "/").concat("/native/");
        Platform platform = DefaultPlatform.getInstance();
        OS os = platform.getOS();
        String osPrefix;
        switch (os) {
            case WINDOWS:
                osPrefix = "win32";
                break;
            case DARWIN:
                return prefix + "darwin/libjnc.jnilib";
            case UNKNOWN:
                throw new UnsupportedOperationException("unsupport operation system");
            case LINUX:
            default:
                osPrefix = os.name().toLowerCase(Locale.US);
                break;
        }
        Arch arch = platform.getArch();
        String archName;
        switch (arch) {
            case I386:
            case X86_64:
                archName = arch.name().toLowerCase(Locale.US);
                break;
            default:
                throw new UnsupportedOperationException("unsupport operation system arch");
        }
        return prefix + osPrefix + "/" + System.mapLibraryName("jnc-" + archName);
    }

    // access by native method
    @SuppressWarnings({"unused", "CollectionsToArray"})
    private static void onUnload() {
        Runnable[] array;
        lock.lock();
        try {
            array = ON_UNLOAD.toArray(new Runnable[ON_UNLOAD.size()]);
        } finally {
            lock.unlock();
        }
        for (int i = array.length - 1; i >= 0; --i) {
            array[i].run();
        }
    }

    /**
     * @param id type id
     * @return the address of the specified type
     */
    native long findType(int id) throws IllegalArgumentException;

    /**
     * get type info size:32 alignment:16 type:16
     *
     * @param address address of ffi_type
     * @return type info
     * @throws NullPointerException if address is zero
     * @see #findType(int)
     */
    native long getTypeInfo(long address);

    native long dlopen(@Nullable String path, int mode) throws UnsatisfiedLinkError;

    native long dlsym(long handle, String symbol) throws UnsatisfiedLinkError;

    native void dlclose(long handle);

    native int getJniVersion();

    native short getBytesRawShort(byte[] bytes, int offset);

    native void putBytesRawShort(byte[] bytes, int offset, short value);

    native char getBytesRawChar(byte[] bytes, int offset);

    native void putBytesRawChar(byte[] bytes, int offset, char value);

    native int getBytesRawInt(byte[] bytes, int offset);

    native void putBytesRawInt(byte[] bytes, int offset, int value);

    native long getBytesRawLong(byte[] bytes, int offset);

    native void putBytesRawLong(byte[] bytes, int offset, long value);

    native float getBytesRawFloat(byte[] bytes, int offset);

    native void putBytesRawFloat(byte[] bytes, int offset, float value);

    native double getBytesRawDouble(byte[] bytes, int offset);

    native void putBytesRawDouble(byte[] bytes, int offset, double value);

    native long getBytesRawAddress(byte[] bytes, int offset);

    native void putBytesRawAddress(byte[] bytes, int offset, long value);

    native void putInt(long address, long type, int value);

    native void putLong(long address, long type, long value);

    native void putFloat(long address, long type, float value);

    native void putDouble(long address, long type, double value);

    native boolean getBoolean(long address, long type);

    native int getInt(long address, long type);

    native long getLong(long address, long type);

    native float getFloat(long address, long type);

    native double getDouble(long address, long type);

    native void putStringUTF(long address, String value);

    native String getStringUTF(long address);

    native String getStringUTFN(long address, long limit);

    native void putRawAddress(long address, long value);

    native long getRawAddress(long address);

    native void getBytes(long address, byte[] bytes, int off, int len);

    native void putBytes(long address, byte[] bytes, int off, int len);

    native void getShortArray(long address, short[] bytes, int off, int len);

    native void putShortArray(long address, short[] bytes, int off, int len);

    native void getCharArray(long address, char[] bytes, int off, int len);

    native void putCharArray(long address, char[] bytes, int off, int len);

    native void getIntArray(long address, int[] bytes, int off, int len);

    native void putIntArray(long address, int[] bytes, int off, int len);

    native void getLongArray(long address, long[] bytes, int off, int len);

    native void putLongArray(long address, long[] bytes, int off, int len);

    native void getFloatArray(long address, float[] bytes, int off, int len);

    native void putFloatArray(long address, float[] bytes, int off, int len);

    native void getDoubleArray(long address, double[] bytes, int off, int len);

    native void putDoubleArray(long address, double[] bytes, int off, int len);

    native int findAlias(String name) throws UnsupportedOperationException;

    /**
     * allocate clean memory of specified size
     *
     * @param size
     * @return the address of the memory
     * @throws IllegalArgumentException size&lt;0
     */
    native long allocateMemory(long size) throws OutOfMemoryError;

    /**
     * copy memory
     *
     * @param dst
     * @param src
     * @param n
     * @throws IllegalArgumentException n&lt;0
     * @throws NullPointerException n&gt;0 and either dst or src is zero
     */
    native void copyMemory(long dst, long src, long n);

    native void freeMemory(long address);

    native int pageSize();

    // size_t
    native int strlen(long address);

    // size_t
    native int wcslen(long address);

    native int sizeof_ffi_cif();

    native void prepareInvoke(long cif, int abi, int len, long retType, long atypes);

    native boolean invokeBoolean(long cif, long function, long params, Object obj, long methodId);

    //native byte invokeByte(long cif, long function, long params,Object obj,long methodId);
    //native short invokeShort(long cif, long function, long params,Object obj,long methodId);
    //native char invokeChar(long cif, long function, long params,Object obj,long methodId);
    native int invokeInt(long cif, long function, long params, Object obj, long methodId);

    native void invokeVoid(long cif, long function, long params, Object obj, long methodId);

    native long invokeLong(long cif, long function, long params, Object obj, long methodId);

    native float invokeFloat(long cif, long function, long params, Object obj, long methodId);

    native double invokeDouble(long cif, long function, long params, Object obj, long methodId);

    native void invokeStruct(long cif, long function, long params, long struct, Object obj, long methodId);

    native long getMethodId(Method method);

    /**
     * @param buffer
     * @return address
     * @throws NullPointerException if buffer is null
     * @throws IllegalArgumentException if buffer is not direct
     */
    native long getBufferAddress(Buffer buffer);

    Set<Runnable> onFinalize(Set<Runnable> set) {
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
