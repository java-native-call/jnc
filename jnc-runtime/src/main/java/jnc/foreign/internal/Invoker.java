package jnc.foreign.internal;

@FunctionalInterface
interface Invoker {

    Object invoke(long cif, long function, long avalues);

}
