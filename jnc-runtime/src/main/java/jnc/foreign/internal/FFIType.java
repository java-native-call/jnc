package jnc.foreign.internal;

import jnc.foreign.Type;

interface FFIType extends Type, NativeObject {

    @Override
    int size();

    int type();

    @Override
    int alignment();

}
