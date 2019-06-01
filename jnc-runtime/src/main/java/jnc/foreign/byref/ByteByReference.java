package jnc.foreign.byref;

import jnc.foreign.Foreign;
import jnc.foreign.NativeType;
import jnc.foreign.Pointer;
import jnc.foreign.Type;

public final class ByteByReference implements ByReference {

    private byte value;

    public ByteByReference() {
    }

    public ByteByReference(byte value) {
        this.value = value;
    }

    public byte getValue() {
        return value;
    }

    public void setValue(byte value) {
        this.value = value;
    }

    @Override
    public void fromNative(Foreign foreign, Pointer pointer) {
        value = pointer.getByte(0);
    }

    @Override
    public void toNative(Foreign foreign, Pointer pointer) {
        pointer.putByte(0, value);
    }

    @Override
    public Type componentType(Foreign foreign) {
        return foreign.findType(NativeType.SINT8);
    }

}
