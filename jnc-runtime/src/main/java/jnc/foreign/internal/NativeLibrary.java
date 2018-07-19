package jnc.foreign.internal;

import java.io.Closeable;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

class NativeLibrary implements NativeObject, Closeable {

    private static final NativeMethods nm = NativeMethods.getInstance();
    // maybe the classloader is finalized before the lib, meanwhile the native lib is also finalized
    // let it call our method finalizeAll to make sure we are closed before it's unloaded.
    // There is no issue with java builtin object.
    private static final Set<Runnable> SET = Collections.newSetFromMap(new ConcurrentHashMap<>(16));

    static {
        nm.onFinalize(SET);
    }

    static NativeLibrary open(String libname, int mode) {
        long addr;
        try {
            addr = nm.dlopen(libname, mode);
        } catch (UnsatisfiedLinkError error) {
            if (!"c".equals(libname) && !"libc.so".equals(libname)
                    || !DefaultPlatform.getInstance().getOS().isELF()) {
                throw error;
            }
            addr = nm.dlopen(null, 0);
        }
        boolean success = false;
        Dlclose dlclose = null;
        try {
            dlclose = new Dlclose(addr);
            NativeLibrary library = new NativeLibrary(addr, dlclose);
            SET.add(dlclose);
            success = true;
            return library;
        } finally {
            if (!success) {
                if (dlclose != null) {
                    dlclose.run();
                } else {
                    nm.dlclose(addr);
                }
            }
        }
    }

    private final long address;
    private final Dlclose dlclose;

    private NativeLibrary(long address, Dlclose dlclose) {
        this.address = address;
        this.dlclose = dlclose;
    }

    @Override
    public long address() {
        return address;
    }

    public long dlsym(String name) throws UnsatisfiedLinkError {
        return nm.dlsym(address, name);
    }

    @Override
    public void close() {
        Dlclose tmp = dlclose;
        try {
            tmp.run();
        } finally {
            SET.remove(tmp);
        }
    }

    @Override
    @SuppressWarnings("FinalizeDeclaration")
    protected void finalize() throws Throwable {
        try {
            close();
        } finally {
            super.finalize();
        }
    }

    private static class Dlclose implements Runnable {

        private final long address;
        private volatile boolean closed;

        Dlclose(long addr) {
            this.address = addr;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }

        @Override
        @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
        public boolean equals(Object obj) {
            return obj == this;
        }

        @Override
        public synchronized void run() {
            if (!closed) {
                closed = true;
                nm.dlclose(address);
            }
        }

    }

}
