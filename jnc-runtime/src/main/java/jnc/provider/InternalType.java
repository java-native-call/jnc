package jnc.provider;

import jnc.foreign.Type;

interface InternalType extends Type, NativeObject {

    @Override
    int size();

    int type();

    @Override
    int alignment();

    @Override
    long address();

    boolean isSigned();

    boolean isFloatingPoint();

    boolean isIntegral();

}
