package jnc.foreign.byref;

import jnc.foreign.Foreign;
import jnc.foreign.NativeType;
import jnc.foreign.Pointer;
import jnc.foreign.Type;

public final class CharByReference implements ByReference {

    private char value;

    public CharByReference() {
    }

    public CharByReference(char value) {
        this.value = value;
    }

    public char getValue() {
        return value;
    }

    public void setValue(char value) {
        this.value = value;
    }

    @Override
    public void fromNative(Foreign foreign, Pointer pointer) {
        value = pointer.getChar(0);
    }

    @Override
    public void toNative(Foreign foreign, Pointer pointer) {
        pointer.putChar(0, value);
    }

    @Override
    public Type componentType(Foreign foreign) {
        return foreign.findType(NativeType.UINT16);
    }

}
