package jnc.foreign.internal;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

class CallContext {

    private static final NativeMethods nm = NativeMethods.getInstance();

    private final CifStruct cifStruct;
    private final AllocatedMemory parameter;
    private List<Runnable> onFinish;
    private final int[] offsets;

    CallContext(int parameterSize, int parameterAlign, int[] offsets, CifStruct cifStruct) {
        this.cifStruct = cifStruct;
        this.parameter = AllocatedMemory.allocate(1, parameterSize);
        this.offsets = offsets;
    }

    public long parameterBaseAddress() {
        return parameter.address();
    }

    @SuppressWarnings("ReturnOfCollectionOrArrayField")
    public int[] offsets() {
        return offsets;
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
        nm.putInt(parameter.address() + offsets[i], cifStruct.get(i), value);
    }

    public void putLong(int i, long value) {
        nm.putLong(parameter.address() + offsets[i], cifStruct.get(i), value);
    }

    public void putFloat(int i, float value) {
        nm.putFloat(parameter.address() + offsets[i], cifStruct.get(i), value);
    }

    public void putDouble(int i, double value) {
        nm.putDouble(parameter.address() + offsets[i], cifStruct.get(i), value);
    }

    CallContext onFinish(Runnable r) {
        Objects.requireNonNull(r);
        List<Runnable> finish = this.onFinish;
        if (finish == null) {
            finish = new ArrayList<>(offsets.length);
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
