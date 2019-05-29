package jnc.foreign.byref;

import jnc.foreign.Foreign;
import jnc.foreign.NativeType;
import jnc.foreign.Pointer;
import jnc.foreign.Type;

public class PointerByReference implements ByReference {

    private Pointer value;

    public PointerByReference() {
    }

    public PointerByReference(Pointer value) {
        this.value = value;
    }

    public Pointer getValue() {
        return value;
    }

    public void setValue(Pointer value) {
        this.value = value;
    }

    @Override
    public void fromNative(Foreign foreign, Pointer pointer) {
        value = pointer.getPointer(0);
    }

    @Override
    public void toNative(Foreign foreign, Pointer pointer) {
        pointer.putPointer(0, value);
    }

    @Override
    public Type componentType(Foreign foreign) {
        return foreign.findType(NativeType.POINTER);
    }

}
