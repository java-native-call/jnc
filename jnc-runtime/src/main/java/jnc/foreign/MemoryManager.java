package jnc.foreign;

import javax.annotation.Nonnull;

public interface MemoryManager {

    @Nonnull
    Pointer allocate(long size) throws OutOfMemoryError;

    @Nonnull
    Pointer allocateWithAlign(long size, long alignment) throws OutOfMemoryError;

}
