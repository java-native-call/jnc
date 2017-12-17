package jnc.foreign.internal;

interface Invoker {

    Object invoke(long cif, long function, long avalues);

}
