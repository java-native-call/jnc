package jnc.provider;

@FunctionalInterface
interface ParameterPutter<T> {

    void doPut(CallContext context, int index, T obj);

}
