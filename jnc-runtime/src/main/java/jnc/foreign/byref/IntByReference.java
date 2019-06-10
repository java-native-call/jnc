package jnc.foreign.byref;

import jnc.foreign.Foreign;
import jnc.foreign.NativeType;
import jnc.foreign.Pointer;
import jnc.foreign.Type;

public final class IntByReference extends ByReference {

    private int value;

    public IntByReference() {
    }

    public IntByReference(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    @Override
    public void fromNative(Foreign foreign, Pointer pointer) {
        value = pointer.getInt(0);
    }

    @Override
    public void toNative(Foreign foreign, Pointer pointer) {
        pointer.putInt(0, value);
    }

    @Override
    public Type componentType(Foreign foreign) {
        return foreign.findType(NativeType.SINT32);
    }

}
