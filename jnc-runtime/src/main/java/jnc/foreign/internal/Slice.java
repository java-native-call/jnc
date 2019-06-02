package jnc.foreign.internal;

import javax.annotation.Nonnull;

final class Slice extends SizedDirectMemory {

    // a holder to keep reference of the memory
    private final Memory outer;
    private final int offset;

    Slice(Memory outer, int offset, int size) {
        super(outer.address() + offset, size);
        this.outer = outer;
        this.offset = offset;
    }

    @Nonnull
    @Override
    public final Slice slice(int beginIndex, int endIndex) {
        MemoryAccessor.checkRange(size(), beginIndex, endIndex);
        return new Slice(outer, offset + beginIndex, endIndex - beginIndex);
    }

}
