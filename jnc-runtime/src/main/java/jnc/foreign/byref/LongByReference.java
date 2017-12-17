package jnc.foreign.byref;

import jnc.foreign.Foreign;
import jnc.foreign.NativeType;
import jnc.foreign.Pointer;
import jnc.foreign.Type;

public class LongByReference implements ByReference {

    private long value;

    public LongByReference() {
    }

    public LongByReference(long value) {
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
        value = pointer.getLong(0);
    }

    @Override
    public void toNative(Foreign foreign, Pointer pointer) {
        pointer.putLong(0, value);
    }

    @Override
    public Type componentType(Foreign foreign) {
        return foreign.findType(NativeType.SINT64);
    }

}
