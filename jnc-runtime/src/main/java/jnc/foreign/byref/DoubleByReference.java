package jnc.foreign.byref;

import jnc.foreign.Foreign;
import jnc.foreign.NativeType;
import jnc.foreign.Pointer;
import jnc.foreign.Type;

public class DoubleByReference implements ByReference {

    private double value;

    public DoubleByReference() {
    }

    public DoubleByReference(double value) {
        this.value = value;
    }

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    @Override
    public void fromNative(Foreign foreign, Pointer pointer) {
        value = pointer.getDouble(0);
    }

    @Override
    public void toNative(Foreign foreign, Pointer pointer) {
        pointer.putDouble(0, value);
    }

    @Override
    public Type componentType(Foreign foreign) {
        return foreign.findType(NativeType.DOUBLE);
    }

}
