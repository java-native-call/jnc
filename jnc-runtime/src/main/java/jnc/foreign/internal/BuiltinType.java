package jnc.foreign.internal;

import jnc.foreign.NativeType;

enum BuiltinType implements InternalType {

    VOID(NativeType.VOID, NativeMethods.TYPE_VOID, 0),
    FLOAT(NativeType.FLOAT, NativeMethods.TYPE_FLOAT, BuiltinType.MASK_SIGNED | BuiltinType.MASK_FLOATING),
    DOUBLE(NativeType.DOUBLE, NativeMethods.TYPE_DOUBLE, BuiltinType.MASK_SIGNED | BuiltinType.MASK_FLOATING),
    UINT8(NativeType.UINT8, NativeMethods.TYPE_UINT8, BuiltinType.MASK_INTEGRAL),
    SINT8(NativeType.SINT8, NativeMethods.TYPE_SINT8, BuiltinType.MASK_INTEGRAL | BuiltinType.MASK_SIGNED),
    UINT16(NativeType.UINT16, NativeMethods.TYPE_UINT16, BuiltinType.MASK_INTEGRAL),
    SINT16(NativeType.SINT16, NativeMethods.TYPE_SINT16, BuiltinType.MASK_INTEGRAL | BuiltinType.MASK_SIGNED),
    UINT32(NativeType.UINT32, NativeMethods.TYPE_UINT32, BuiltinType.MASK_INTEGRAL),
    SINT32(NativeType.SINT32, NativeMethods.TYPE_SINT32, BuiltinType.MASK_INTEGRAL | BuiltinType.MASK_SIGNED),
    UINT64(NativeType.UINT64, NativeMethods.TYPE_UINT64, BuiltinType.MASK_INTEGRAL),
    SINT64(NativeType.SINT64, NativeMethods.TYPE_SINT64, BuiltinType.MASK_INTEGRAL | BuiltinType.MASK_SIGNED),
    POINTER(NativeType.ADDRESS, NativeMethods.TYPE_POINTER, BuiltinType.MASK_INTEGRAL);

    private static final int MASK_SIGNED = 1;
    private static final int MASK_INTEGRAL = 2;
    private static final int MASK_FLOATING = 4;

    private final NativeType nativeType;
    private final int type;
    private final int mask;

    BuiltinType(NativeType nativeType, int type, int mask) {
        this.type = type;
        this.mask = mask;
        this.nativeType = nativeType;
    }

    @Override
    public long address() {
        return TypeHelper.getTypeInfo(type).address();
    }

    public NativeType getNativeType() {
        return nativeType;
    }

    @Override
    public int size() {
        return TypeHelper.getTypeInfo(type).size();
    }

    @Override
    public int alignment() {
        return TypeHelper.getTypeInfo(type).alignment();
    }

    @Override
    public int type() {
        return type;
    }

    public boolean isSigned() {
        return (mask & MASK_SIGNED) != 0;
    }

    public boolean isFloatingPoint() {
        return (mask & MASK_FLOATING) != 0;
    }

    public boolean isIntegral() {
        return (mask & MASK_INTEGRAL) != 0;
    }

}
