package jnc.foreign.byref;

import jnc.foreign.Foreign;
import jnc.foreign.Pointer;
import jnc.foreign.Type;

@SuppressWarnings("ClassMayBeInterface")
public abstract class ByReference {

    public abstract void fromNative(Foreign foreign, Pointer pointer);

    public abstract void toNative(Foreign foreign, Pointer pointer);

    public abstract Type componentType(Foreign foreign);

}
