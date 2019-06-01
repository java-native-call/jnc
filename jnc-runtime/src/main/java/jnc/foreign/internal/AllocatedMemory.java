package jnc.foreign.internal;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLongFieldUpdater;

class AllocatedMemory extends SizedDirectMemory {

    private static final Set<Runnable> SET = NativeLoader.getAccessor().onFinalize(Collections.newSetFromMap(new ConcurrentHashMap<>(32)));

    private static AllocatedMemory allocateImpl(long size) {
        Free free = new Free(size);
        boolean success = false;
        try {
            AllocatedMemory memory = new AllocatedMemory(size, free);
            SET.add(free);
            success = true;
            return memory;
        } finally {
            if (!success) {
                try {
                    free.run();
                } finally {
                    SET.remove(free);
                }
            }
        }
    }

    static AllocatedMemory allocate(long size) throws OutOfMemoryError {
        if (size < 0) {
            throw new IllegalArgumentException();
        }
        return allocateImpl(size);
    }

    static AllocatedMemory allocate(int count, int size) throws OutOfMemoryError {
        if ((count | size) < 0) {
            throw new IllegalArgumentException();
        }
        return allocateImpl((long) count * size);
    }

    private final Free free;

    private AllocatedMemory(long size, Free free) {
        super(free.getAddress(), size);
        this.free = free;
    }

    @Override
    @SuppressWarnings({"FinalizeDeclaration", "FinalizeDoesntCallSuperFinalize"})
    protected void finalize() {
        Free f = free;
        try {
            f.run();
        } finally {
            SET.remove(f);
        }
    }

    private static final class Free implements Runnable {

        private static final NativeAccessor NA = NativeLoader.getAccessor();
        private static final AtomicLongFieldUpdater<Free> UPDATER
                = AtomicLongFieldUpdater.newUpdater(Free.class, "address");
        @SuppressWarnings("unused")
        private volatile long address;

        Free(long size) {
            this.address = NA.allocateMemory(size);
        }

        long getAddress() {
            return address;
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
                NA.freeMemory(addr);
            }
        }
    }

}
