package jnc.foreign.internal;

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

final class AllocatedMemory extends SizedDirectMemory {

    private static final Cleaner CLEANER = Cleaner.getInstance();

    private static AllocatedMemory allocateImpl(long size) {
        Free free = new Free(size);
        try {
            return new AllocatedMemory(size, free);
        } catch (Throwable t) {
            // very rare, maybe OutOfMemoryError when register Cleanable
            free.run();
            throw t;
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

    @SuppressWarnings("LeakingThisInConstructor")
    private AllocatedMemory(long size, Free free) {
        super(free.getAddress(), size);
        CLEANER.register(this, free);
    }

    private static final class Free implements Runnable {

        private static final NativeAccessor NA = NativeLoader.getAccessor();
        private static final AtomicLongFieldUpdater<Free> UPDATER
                = AtomicLongFieldUpdater.newUpdater(Free.class, "address");
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
