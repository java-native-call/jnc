package jnc.foreign.byref;

import jnc.foreign.Foreign;
import jnc.foreign.Pointer;
import jnc.foreign.Type;
import jnc.foreign.enums.TypeAlias;

public final class Uint32ByReference extends ByReference {

    private long value;

    public Uint32ByReference() {
    }

    public Uint32ByReference(long value) {
        this.value = value & 0xFFFFFFFFL;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value & 0xFFFFFFFFL;
    }

    @Override
    public void fromNative(Foreign foreign, Pointer pointer) {
        pointer.putInt(0, (int) value);
    }

    @Override
    public void toNative(Foreign foreign, Pointer pointer) {
        value = pointer.getInt(0) & 0xFFFFFFFFL;
    }

    @Override
    public Type componentType(Foreign foreign) {
        return foreign.findType(TypeAlias.uint32_t);
    }

}
