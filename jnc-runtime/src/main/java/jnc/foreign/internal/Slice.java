package jnc.foreign.internal;

import javax.annotation.Nonnull;

class Slice extends SizedDirectMemory {

    private final Memory outer;
    private final int offset;

    Slice(Memory outer, int offset, int size) {
        super(outer.address() + offset, size);
        this.outer = outer;
        this.offset = offset;
    }

    @Nonnull
    @Override
    public final Slice slice(int offset, int count) {
        getAccessor().checkIndex(offset, size(), count);
        return new Slice(outer, this.offset + offset, count);
    }

}
