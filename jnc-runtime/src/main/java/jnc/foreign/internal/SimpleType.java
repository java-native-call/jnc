package jnc.foreign.internal;

import jnc.foreign.NativeType;

interface SimpleType extends FFIType {

    NativeType getNativeType();

    boolean isSigned();

}
