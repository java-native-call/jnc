package jnc.foreign.internal;

@FunctionalInterface
interface Invoker<T> {

    T invoke(long cif, long function, long avalues);

}
