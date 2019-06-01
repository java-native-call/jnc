package jnc.foreign.byref;

import jnc.foreign.Foreign;
import jnc.foreign.Pointer;
import jnc.foreign.Type;
import jnc.foreign.enums.TypeAlias;

public final class NativeLongByReference implements ByReference {

    private long value;

    public NativeLongByReference() {
    }

    public NativeLongByReference(long value) {
        this.value = value;
    }

    public long getValue() {
        return value;
    }

    public void setValue(long value) {
        this.value = value;
    }

    @Override
    public void fromNative(Foreign foreign, Pointer pointer) {
        value = pointer.getLong(0, componentType(foreign));
    }

    @Override
    public void toNative(Foreign foreign, Pointer pointer) {
        pointer.putLong(0, componentType(foreign), value);
    }

    @Override
    public Type componentType(Foreign foreign) {
        return foreign.findType(TypeAlias.clong);
    }

}
