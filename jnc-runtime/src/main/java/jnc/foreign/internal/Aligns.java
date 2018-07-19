package jnc.foreign.internal;

@SuppressWarnings("UtilityClassWithoutPrivateConstructor")
class Aligns {

    static int alignUp(int x, int align) {
        return x + align - 1 & -align;
    }

}
