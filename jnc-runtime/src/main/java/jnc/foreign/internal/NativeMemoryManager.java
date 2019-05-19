package jnc.foreign.internal;

import javax.annotation.Nonnull;
import jnc.foreign.MemoryManager;
import jnc.foreign.Pointer;

class NativeMemoryManager implements MemoryManager {

    private static final NativeMemoryManager INSTANCE = new NativeMemoryManager();

    public static NativeMemoryManager getInstance() {
        return INSTANCE;
    }

    @Nonnull
    @Override
    public Pointer allocate(long size) throws OutOfMemoryError {
        return AllocatedMemory.allocate(size);
    }

    @Nonnull
    @Override
    public Pointer allocateWithAlign(long size, long alignment) throws OutOfMemoryError {
        return AllocatedMemory.allocate(size);
    }

}
