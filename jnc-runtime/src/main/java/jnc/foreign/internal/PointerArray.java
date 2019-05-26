package jnc.foreign.internal;

class PointerArray implements NativeObject {

    private final static int SHIFT;

    static {
        int size = BuiltinType.POINTER.size();
        if (Integer.bitCount(size) != 1) {
            throw new AssertionError("Illegal pointer size");
        }
        SHIFT = Integer.numberOfTrailingZeros(size);
    }

    static PointerArray allocate(NativeObject[] params) {
        int len = params.length;
        PointerArray array = new PointerArray(len);
        for (int i = 0; i < len; ++i) {
            array.set(i, params[i].address());
        }
        return array;
    }

    static void init(PointerArray array, long base, long[] offsets) {
        int len = offsets.length;
        for (int i = 0; i < len; ++i) {
            array.set(i, base + offsets[i]);
        }
    }

    static PointerArray wrap(Memory memory, int length) {
        return new PointerArray(memory, length);
    }

    private final Memory memory;
    private final int length;

    private PointerArray(int length) {
        this.memory = AllocatedMemory.allocate(length, 1 << SHIFT);
        this.length = length;
    }

    private PointerArray(Memory memory, int length) {
        this.memory = memory;
        this.length = length;
    }

    @Override
    public long address() {
        return memory.address();
    }

    public int length() {
        return length;
    }

    private int checkIndex(int index) {
        if (index < 0 || index >= length) {
            throw new IndexOutOfBoundsException();
        }
        return index << SHIFT;
    }

    public long get(int index) {
        return memory.getAddress(checkIndex(index));
    }

    public void set(int index, long value) {
        memory.putAddress(checkIndex(index), value);
    }

}
