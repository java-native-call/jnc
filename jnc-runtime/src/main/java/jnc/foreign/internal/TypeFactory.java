package jnc.foreign.internal;

import jnc.foreign.NativeType;
import jnc.foreign.enums.TypeAlias;

interface TypeFactory {

    TypeInfo getPointerType();

    TypeInfo findByType(int type);

    Alias findByAlias(TypeAlias typeAlias);

    InternalType findByNativeType(NativeType nativeType);

    InternalType findByPrimaryType(Class<?> type);

}
