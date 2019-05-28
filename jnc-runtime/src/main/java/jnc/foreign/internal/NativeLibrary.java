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

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

/**
 * @author zhanhb
 */
class NativeLibrary implements Library {

    private static final NativeMethods nm = NativeMethods.getInstance();
    // maybe the classloader is finalized before the lib, meanwhile the native lib is also finalized
    // let it call our method onUnload to make sure we are closed before it's unloaded.
    // There is no issue with java builtin object.
    private static final Set<Runnable> SET = nm.onFinalize(Collections.newSetFromMap(new ConcurrentHashMap<>(16)));

    static NativeLibrary open(String libname, int mode) {
        long addr;
        try {
            addr = nm.dlopen(libname, mode);
        } catch (UnsatisfiedLinkError error) {
            if (!"c".equals(libname) && !"libc.so".equals(libname)
                    || !DefaultPlatform.INSTANCE.getOS().isELF()) {
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

    @Override
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

    private static final class Dlclose implements Runnable {

        private static final AtomicLongFieldUpdater<Dlclose> UPDATER
                = AtomicLongFieldUpdater.newUpdater(Dlclose.class, "address");
        @SuppressWarnings("unused")
        private volatile long address;

        Dlclose(long addr) {
            this.address = addr;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this;
        }

        @Override
        public void run() {
            long addr = UPDATER.getAndSet(this, 0);
            if (addr != 0) {
                nm.dlclose(addr);
            }
        }

    }

}
