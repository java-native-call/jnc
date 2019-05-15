package jnc.foreign.internal;

interface Aligns {

    static int alignUp(int x, int align) {
        return x + align - 1 & -align;
    }

}
