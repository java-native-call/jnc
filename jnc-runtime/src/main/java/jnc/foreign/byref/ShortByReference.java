package jnc.foreign.byref;

import jnc.foreign.Foreign;
import jnc.foreign.NativeType;
import jnc.foreign.Pointer;
import jnc.foreign.Type;

public final class ShortByReference extends ByReference {

    private short value;

    public ShortByReference() {
    }

    public ShortByReference(short value) {
        this.value = value;
    }

    public short getValue() {
        return value;
    }

    public void setValue(short value) {
        this.value = value;
    }

    @Override
    public void fromNative(Foreign foreign, Pointer pointer) {
        value = pointer.getShort(0);
    }

    @Override
    public void toNative(Foreign foreign, Pointer pointer) {
        pointer.putShort(0, value);
    }

    @Override
    public Type componentType(Foreign foreign) {
        return foreign.findType(NativeType.SINT16);
    }

}
