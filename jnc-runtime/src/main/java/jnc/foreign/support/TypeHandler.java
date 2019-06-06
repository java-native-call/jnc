package jnc.foreign.support;

import jnc.foreign.Pointer;
import jnc.foreign.Type;

public interface TypeHandler<T> {

    Type type();

    T get(Pointer memory, int offset);

    void set(Pointer memory, int offset, T value);

}
