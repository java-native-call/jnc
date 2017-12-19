package jnc.foreign;

public enum Arch {

    UNKNOWN(0),
    I386(32),
    X86_64(64);

    private final int size;

    private Arch(int size) {
        this.size = size;
    }

    public int pointerSize() {
        return size;
    }

}
