package jnc.foreign.internal;

import javax.annotation.Nullable;

@FunctionalInterface
interface Invoker<T> {

    T invoke(long cif, long function, long base, @Nullable int[] offsets);

}
