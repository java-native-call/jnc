package jnc.foreign;

public interface MemoryManager {

    Pointer allocate(long size) throws OutOfMemoryError;

    Pointer allocateWithAlign(long size, long alignment) throws OutOfMemoryError;

}
