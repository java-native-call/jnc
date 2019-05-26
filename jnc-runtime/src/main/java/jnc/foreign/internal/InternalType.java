package jnc.foreign.internal;

import jnc.foreign.Type;

interface InternalType extends Type, NativeObject {

    @Override
    int size();

    int type();

    @Override
    int alignment();

    @Override
    long address();

    // for testing
    boolean isSigned();

    boolean isFloatingPoint();

    boolean isIntegral();

}
