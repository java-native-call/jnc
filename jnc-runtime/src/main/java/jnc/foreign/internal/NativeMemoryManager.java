package jnc.foreign.internal;

import jnc.foreign.MemoryManager;
import jnc.foreign.Pointer;

class NativeMemoryManager implements MemoryManager {

    private static final NativeMemoryManager INSTANCE = new NativeMemoryManager();

    public static NativeMemoryManager getInstance() {
        return INSTANCE;
    }

    @Override
    public Pointer allocate(long size) throws OutOfMemoryError {
        return AllocatedMemory.allocate(size);
    }

    @Override
    public Pointer allocateWithAlign(long size, long alignment) throws OutOfMemoryError {
        return AllocatedMemory.allocate(size);
    }

}
