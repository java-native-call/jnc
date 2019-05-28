package jnc.foreign.internal;

import javax.annotation.Nonnull;
import jnc.foreign.MemoryManager;
import jnc.foreign.Pointer;

enum DefaultMemoryManager implements MemoryManager {

    INSTANCE;

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
