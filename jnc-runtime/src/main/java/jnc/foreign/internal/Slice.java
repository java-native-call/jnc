package jnc.foreign.internal;

import javax.annotation.Nonnull;

class Slice extends SizedDirectMemory {

    private final DirectMemory parent;
    private final int offset;

    Slice(DirectMemory parent, int offset, int size) {
        super(parent.address() + offset, size);
        this.parent = parent;
        this.offset = offset;
    }

    @Nonnull
    @Override
    public DirectMemory slice(int offset, int size) {
        checkIndex(offset, size);
        return new Slice(parent, this.offset + offset, size);
    }

}
