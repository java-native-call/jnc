package jnc.foreign.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

class CallContext implements NativeObject {

    private static final NativeMethods nm = NativeMethods.getInstance();

    private final PointerArray types;
    private final PointerArray values;
    private List<Runnable> onFinish;

    CallContext(int parameterSize, int parameterAlign, long base, long[] offsets, PointerArray types) {
        int count = offsets.length;
        DirectMemory p = AllocatedMemory.allocate(parameterSize, parameterAlign);
        PointerArray v = PointerArray.wrap(p, count);
        PointerArray.init(v, p.address() + base, offsets);
        this.types = types;
        this.values = v;
    }

    @Override
    public long address() {
        return values.address();
    }

    public void putBoolean(int i, boolean value) {
        putInt(i, value ? 1 : 0);
    }

    public void putByte(int i, byte value) {
        putInt(i, value);
    }

    public void putChar(int i, char value) {
        putInt(i, value);
    }

    public void putShort(int i, short value) {
        putInt(i, value);
    }

    public void putInt(int i, int value) {
        nm.putInt(values.get(i), types.get(i), value);
    }

    public void putLong(int i, long value) {
        nm.putLong(values.get(i), types.get(i), value);
    }

    public void putFloat(int i, float value) {
        nm.putFloat(values.get(i), types.get(i), value);
    }

    public void putDouble(int i, double value) {
        nm.putDouble(values.get(i), types.get(i), value);
    }

    CallContext onFinish(Runnable r) {
        Objects.requireNonNull(r);
        List<Runnable> finish = this.onFinish;
        if (finish == null) {
            finish = new ArrayList<>(values.length());
            this.onFinish = finish;
        }
        finish.add(r);
        return this;
    }

    void finish() {
        List<Runnable> finish = this.onFinish;
        if (finish != null) {
            Collections.reverse(finish);
            for (Runnable runnable : finish) {
                runnable.run();
            }
        }
    }

}
