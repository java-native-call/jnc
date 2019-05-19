package jnc.foreign.internal;

import jnc.foreign.TypeHandler;

interface InternalTypeHandler<T> extends TypeHandler<T> {

    @Deprecated(/* FIXME: 2019-05-19 for removal */)
    BuiltinType getBuiltinType();

    Invoker getInvoker();

    ParameterHandler<T> getParameterHandler();

}
