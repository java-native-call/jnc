package jnc.foreign.internal;

import jnc.foreign.NativeType;
import jnc.foreign.enums.TypeAlias;

interface TypeFactory {

    Alias findByAlias(TypeAlias typeAlias);

    InternalType findByNativeType(NativeType nativeType);

    @SuppressWarnings("unused")
    InternalType findByPrimaryType(Class<?> type);

}
