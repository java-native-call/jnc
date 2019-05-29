package jnc.foreign.byref;

import jnc.foreign.Foreign;
import jnc.foreign.NativeType;
import jnc.foreign.Pointer;
import jnc.foreign.Type;

public class AddressByReference implements ByReference {

    private long value;

    public AddressByReference() {
    }

    public AddressByReference(long value) {
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
        value = pointer.getAddress(0);
    }

    @Override
    public void toNative(Foreign foreign, Pointer pointer) {
        pointer.putAddress(0, value);
    }

    @Override
    public Type componentType(Foreign foreign) {
        return foreign.findType(NativeType.POINTER);
    }

}
