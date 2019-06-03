package jnc.foreign.internal;

import java.nio.charset.Charset;
import javax.annotation.Nonnull;
import jnc.foreign.MemoryManager;
import jnc.foreign.Pointer;

enum DefaultMemoryManager implements MemoryManager {

    INSTANCE;

    @Nonnull
    @Override
    public Pointer allocate(long size) {
        return AllocatedMemory.allocate(size);
    }

    @Nonnull
    @Override
    public Pointer allocateWithAlign(long size, long alignment) {
        return AllocatedMemory.allocate(size);
    }

    @Override
    public Pointer allocateString(String string, Charset charset) {
        byte[] bytes = string.getBytes(charset);
        int terminatorLength = CharsetUtil.getTerminatorLength(charset);
        AllocatedMemory memory = AllocatedMemory.allocate(bytes.length + terminatorLength);
        memory.putString(0, string, charset);
        return memory;
    }

}
