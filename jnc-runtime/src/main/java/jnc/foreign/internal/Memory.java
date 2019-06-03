package jnc.foreign.internal;

import java.nio.charset.Charset;
import java.util.Objects;
import javax.annotation.Nonnull;
import jnc.foreign.Pointer;
import jnc.foreign.Type;

abstract class Memory implements Pointer {

    private final MemoryAccessor ma;

    Memory(MemoryAccessor ma) {
        this.ma = Objects.requireNonNull(ma);
    }

    final MemoryAccessor getAccessor() {
        return ma;
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

    @Override
    public final void putString(int offset, @Nonnull String value, @Nonnull Charset charset) {
        Objects.requireNonNull(value);
        Objects.requireNonNull(charset);
        if (StringCoding.isNativeUTF16(charset)) {
            putString16(offset, value);
            return;
        }
        int terminatorLength = CharsetUtil.getTerminatorLength(charset);
        // throws UnsupportedOperationException if the charset doesn't support encode
        byte[] bytes = value.getBytes(charset);
        putStringImpl(offset, bytes, terminatorLength);
    }

    @Nonnull
    @Override
    public String getString(int offset, @Nonnull Charset charset) {
        Objects.requireNonNull(charset);
        if (StringCoding.isNativeUTF16(charset)) {
            return getString16(offset);
        }
        return getStringImpl(offset, charset);
    }

    abstract void putString16(int offset, @Nonnull String value);

    abstract String getString16(int offset);

    abstract void putStringImpl(int offset, byte[] bytes, int terminatorLength);

    abstract String getStringImpl(int offset, Charset charset);

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
