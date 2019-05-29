package jnc.foreign.internal;

import java.util.Objects;
import javax.annotation.Nullable;
import jnc.foreign.Pointer;
import jnc.foreign.Type;

abstract class Memory implements Pointer {

    private final MemoryAccessor ma;

    Memory(MemoryAccessor ma) {
        this.ma = Objects.requireNonNull(ma);
    }

    final MemoryAccessor getMemoryAccessor() {
        return ma;
    }

    @Override
    public final void putAddress(int offset, long value) {
        putLong(offset, TypeHelper.TYPE_INFO_POINTER, value);
    }

    @Override
    public final long getAddress(int offset) {
        return getLong(offset, TypeHelper.TYPE_INFO_POINTER);
    }

    private InternalType toInternalType(Type type) {
        try {
            return (InternalType) type;
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException("unsupported type " + type);
        }
    }

    @Override
    public final boolean getBoolean(int offset, Type nativeType) {
        return getBoolean(offset, toInternalType(nativeType));
    }

    @Override
    public final void putBoolean(int offset, Type nativeType, boolean value) {
        putBoolean(offset, toInternalType(nativeType), value);
    }

    @Override
    public final byte getByte(int offset, Type type) {
        return (byte) getInt(offset, toInternalType(type));
    }

    @Override
    public final short getShort(int offset, Type type) {
        return (short) getInt(offset, toInternalType(type));
    }

    @Override
    public final int getInt(int offset, Type nativeType) {
        return getInt(offset, toInternalType(nativeType));
    }

    @Override
    public final void putInt(int offset, Type nativeType, int value) {
        putInt(offset, toInternalType(nativeType), value);
    }

    @Override
    public final long getLong(int offset, Type nativeType) {
        return getLong(offset, toInternalType(nativeType));
    }

    @Override
    public final void putLong(int offset, Type nativeType, long value) {
        putLong(offset, toInternalType(nativeType), value);
    }

    @Override
    public final float getFloat(int offset, Type nativeType) {
        return getFloat(offset, toInternalType(nativeType));
    }

    @Override
    public final void putFloat(int offset, Type nativeType, float value) {
        putFloat(offset, toInternalType(nativeType), value);
    }

    @Override
    public final double getDouble(int offset, Type nativeType) {
        return getDouble(offset, toInternalType(nativeType));
    }

    @Override
    public final void putDouble(int offset, Type nativeType, double value) {
        putDouble(offset, toInternalType(nativeType), value);
    }

    @Nullable
    @Override
    public final Pointer getPointer(int offset) {
        return UnboundedDirectMemory.of(getAddress(offset));
    }

    @Override
    public final void putPointer(int offset, @Nullable Pointer pointer) {
        putAddress(offset, pointer != null ? pointer.address() : 0);
    }

    abstract void putBoolean(int offset, InternalType internalType, boolean value);

    abstract void putInt(int offset, InternalType internalType, int value);

    abstract void putLong(int offset, InternalType internalType, long value);

    abstract void putFloat(int offset, InternalType internalType, float value);

    abstract void putDouble(int offset, InternalType internalType, double value);

    abstract boolean getBoolean(int offset, InternalType internalType);

    abstract int getInt(int offset, InternalType internalType);

    abstract long getLong(int offset, InternalType internalType);

    abstract float getFloat(int offset, InternalType internalType);

    abstract double getDouble(int offset, InternalType internalType);

}
