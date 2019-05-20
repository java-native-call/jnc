package jnc.foreign.internal;

import jnc.foreign.NativeType;

interface InternalType extends FFIType {

    @Override
    long address();

    NativeType getNativeType();

    boolean isSigned();

}
