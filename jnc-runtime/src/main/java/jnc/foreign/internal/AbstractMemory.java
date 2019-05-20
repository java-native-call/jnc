package jnc.foreign.internal;

import jnc.foreign.Pointer;
import jnc.foreign.Type;

abstract class AbstractMemory implements Pointer {

    @Override
    public void putBoolean(int offset, boolean value) {
        putInt(offset, BuiltinType.UINT8, value ? 1 : 0);
    }

    @Override
    public void putByte(int offset, byte value) {
        putInt(offset, BuiltinType.SINT8, value);
    }

    @Override
    public void putChar(int offset, char value) {
        putInt(offset, BuiltinType.UINT16, value);
    }

    @Override
    public void putShort(int offset, short value) {
        putInt(offset, BuiltinType.SINT16, value);
    }

    @Override
    public void putInt(int offset, int value) {
        putInt(offset, BuiltinType.SINT32, value);
    }

    @Override
    public void putLong(int offset, long value) {
        putLong(offset, BuiltinType.SINT64, value);
    }

    @Override
    public void putFloat(int offset, float value) {
        putFloat(offset, BuiltinType.FLOAT, value);
    }

    @Override
    public void putDouble(int offset, double value) {
        putDouble(offset, BuiltinType.DOUBLE, value);
    }

    @Override
    public void putAddress(int offset, long value) {
        putLong(offset, BuiltinType.POINTER, value);
    }

    @Override
    public boolean getBoolean(int offset) {
        return getBoolean(offset, BuiltinType.UINT8);
    }

    @Override
    public byte getByte(int offset) {
        return (byte) getInt(offset, BuiltinType.SINT8);
    }

    @Override
    public short getShort(int offset) {
        return (short) getInt(offset, BuiltinType.SINT16);
    }

    @Override
    public char getChar(int offset) {
        return (char) getInt(offset, BuiltinType.UINT16);
    }

    @Override
    public int getInt(int offset) {
        return getInt(offset, BuiltinType.SINT32);
    }

    @Override
    public long getLong(int offset) {
        return getLong(offset, BuiltinType.SINT64);
    }

    @Override
    public float getFloat(int offset) {
        return getFloat(offset, BuiltinType.FLOAT);
    }

    @Override
    public double getDouble(int offset) {
        return getDouble(offset, BuiltinType.DOUBLE);
    }

    @Override
    public long getAddress(int offset) {
        return getLong(offset, BuiltinType.POINTER);
    }

    private InternalType toInternalType(Type type) {
        try {
            return (InternalType) type;
        } catch (ClassCastException ex) {
            throw new IllegalArgumentException("unsupported type " + type);
        }
    }

    @Override
    public boolean getBoolean(int offset, Type nativeType) {
        return getBoolean(offset, toInternalType(nativeType));
    }

    @Override
    public void putBoolean(int offset, Type nativeType, boolean value) {
        putBoolean(offset, toInternalType(nativeType), value);
    }

    @Override
    public byte getByte(int offset, Type type) {
        return (byte) getInt(offset, toInternalType(type));
    }

    @Override
    public short getShort(int offset, Type type) {
        return (short) getInt(offset, toInternalType(type));
    }

    @Override
    public char getChar(int offset, Type type) {
        return (char) getInt(offset, toInternalType(type));
    }

    @Override
    public void putByte(int offset, Type type, byte value) {
        putInt(offset, toInternalType(type), offset);
    }

    @Override
    public void putShort(int offset, Type type, short value) {
        putInt(offset, toInternalType(type), offset);
    }

    @Override
    public void putChar(int offset, Type type, char value) {
        putInt(offset, toInternalType(type), offset);
    }

    @Override
    public int getInt(int offset, Type nativeType) {
        return getInt(offset, toInternalType(nativeType));
    }

    @Override
    public void putInt(int offset, Type nativeType, int value) {
        putInt(offset, toInternalType(nativeType), value);
    }

    @Override
    public long getLong(int offset, Type nativeType) {
        return getLong(offset, toInternalType(nativeType));
    }

    @Override
    public void putLong(int offset, Type nativeType, long value) {
        putLong(offset, toInternalType(nativeType), value);
    }

    @Override
    public float getFloat(int offset, Type nativeType) {
        return getFloat(offset, toInternalType(nativeType));
    }

    @Override
    public void putFloat(int offset, Type nativeType, float value) {
        putFloat(offset, toInternalType(nativeType), value);
    }

    @Override
    public double getDouble(int offset, Type nativeType) {
        return getDouble(offset, toInternalType(nativeType));
    }

    @Override
    public void putDouble(int offset, Type nativeType, double value) {
        putDouble(offset, toInternalType(nativeType), value);
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
