package jnc.foreign.internal;

import jnc.foreign.TypeHandler;

interface InternalTypeHandler<T> extends TypeHandler<T> {

    InternalType getDefaultType();

    Invoker<T> getInvoker();

    ParameterHandler<T> getParameterHandler();

}
