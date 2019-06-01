package jnc.foreign.internal;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

class AllocatedMemory extends SizedDirectMemory {

    private static final Cleaner CLEANER = Cleaner.getInstance();

    private static AllocatedMemory allocateImpl(long size) {
        Free free = new Free(size);
        boolean success = false;
        try {
            AllocatedMemory memory = new AllocatedMemory(size, free);
            CLEANER.register(memory, free);
            success = true;
            return memory;
        } finally {
            // very rare, maybe OutOfMemoryError when register Cleanable
            if (!success) {
                free.run();
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

    private AllocatedMemory(long size, Free free) {
        super(free.getAddress(), size);
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
        public void run() {
            long addr = UPDATER.getAndSet(this, 0);
            if (addr != 0) {
                NA.freeMemory(addr);
            }
        }
    }

}
