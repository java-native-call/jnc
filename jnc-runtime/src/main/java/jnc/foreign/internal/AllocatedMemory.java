package jnc.foreign.internal;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

class AllocatedMemory extends SizedDirectMemory {

    private static final NativeMethods nm = NativeMethods.getInstance();
    private static final Set<Runnable> SET = Collections.newSetFromMap(new ConcurrentHashMap<>(32));

    static {
        nm.onFinalize(SET);
    }

    private static AllocatedMemory allocateImpl(int size) {
        long addr = nm.allocateMemory(size);
        boolean success = false;
        Free free = null;
        try {
            free = new Free(addr);
            AllocatedMemory memory = new AllocatedMemory(addr, size, free);
            SET.add(free);
            success = true;
            return memory;
        } finally {
            if (!success) {
                if (free != null) {
                    free.run();
                } else {
                    nm.freeMemory(addr);
                }
            }
        }
    }

    static AllocatedMemory allocate(long size) throws OutOfMemoryError {
        if (size < 0) {
            throw new IllegalArgumentException();
        }
        int s = (int) size;
        if (size != s) {
            throw new OutOfMemoryError();
        }
        return allocateImpl(s);
    }

    static AllocatedMemory allocate(int count, int size) throws OutOfMemoryError {
        if ((count | size) < 0) {
            throw new IllegalArgumentException();
        }
        long r = (long) count * size;
        int s = (int) r;
        if (r != s) {
            throw new OutOfMemoryError();
        }
        return allocateImpl(s);
    }

    private final Free free;

    private AllocatedMemory(long addr, int size, Free free) {
        super(addr, size);
        this.free = free;
    }

    @Override
    @SuppressWarnings("FinalizeDeclaration")
    protected void finalize() throws Throwable {
        try {
            Free f = free;
            try {
                f.run();
            } finally {
                SET.remove(f);
            }
        } finally {
            super.finalize();
        }
    }

    private static class Free implements Runnable {

        private static final AtomicLongFieldUpdater<Free> UPDATER
                = AtomicLongFieldUpdater.newUpdater(Free.class, "address");
        @SuppressWarnings("unused")
        private volatile long address;

        Free(long addr) {
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
        public synchronized void run() {
            long addr = UPDATER.getAndSet(this, 0);
            if (addr != 0) {
                nm.freeMemory(addr);
            }
        }
    }

}
