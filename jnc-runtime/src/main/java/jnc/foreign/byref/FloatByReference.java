package jnc.foreign.byref;

import jnc.foreign.Foreign;
import jnc.foreign.NativeType;
import jnc.foreign.Pointer;
import jnc.foreign.Type;

public final class FloatByReference implements ByReference {

    private float value;

    public FloatByReference() {
    }

    public FloatByReference(float value) {
        this.value = value;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    @Override
    public void fromNative(Foreign foreign, Pointer pointer) {
        value = pointer.getFloat(0);
    }

    @Override
    public void toNative(Foreign foreign, Pointer pointer) {
        pointer.putFloat(0, value);
    }

    @Override
    public Type componentType(Foreign foreign) {
        return foreign.findType(NativeType.FLOAT);
    }

}
