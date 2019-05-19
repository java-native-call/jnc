package jnc.foreign.internal;

import jnc.foreign.TypeHandler;

interface InternalTypeHandler<T> extends TypeHandler<T> {

    BuiltinType getBuiltinType();

    Invoker<T> getInvoker();

    ParameterHandler<T> getParameterHandler();

}
