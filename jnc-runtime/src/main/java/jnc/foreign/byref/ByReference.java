package jnc.foreign.byref;

import jnc.foreign.Foreign;
import jnc.foreign.Pointer;
import jnc.foreign.Type;

public interface ByReference {

    void fromNative(Foreign foreign, Pointer pointer);

    void toNative(Foreign foreign, Pointer pointer);

    Type componentType(Foreign foreign);

}
