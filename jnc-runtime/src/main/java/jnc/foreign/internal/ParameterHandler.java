package jnc.foreign.internal;

@FunctionalInterface
interface ParameterHandler<T> {

    void handle(CallContext context, int index, T obj);

}
