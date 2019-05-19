package jnc.foreign.internal;

import jnc.foreign.NativeType;

enum BuiltinType implements SimpleType {

    VOID(NativeType.VOID, NativeMethods.TYPE_VOID, 0),
    FLOAT(NativeType.FLOAT, NativeMethods.TYPE_FLOAT, BuiltinType.MASK_SIGNED | BuiltinType.MASK_FLOAT),
    DOUBLE(NativeType.DOUBLE, NativeMethods.TYPE_DOUBLE, BuiltinType.MASK_SIGNED | BuiltinType.MASK_FLOAT),
    UINT8(NativeType.UINT8, NativeMethods.TYPE_UINT8, BuiltinType.MASK_INTEGER),
    SINT8(NativeType.SINT8, NativeMethods.TYPE_SINT8, BuiltinType.MASK_INTEGER | BuiltinType.MASK_SIGNED),
    UINT16(NativeType.UINT16, NativeMethods.TYPE_UINT16, BuiltinType.MASK_INTEGER),
    SINT16(NativeType.SINT16, NativeMethods.TYPE_SINT16, BuiltinType.MASK_INTEGER | BuiltinType.MASK_SIGNED),
    UINT32(NativeType.UINT32, NativeMethods.TYPE_UINT32, BuiltinType.MASK_INTEGER),
    SINT32(NativeType.SINT32, NativeMethods.TYPE_SINT32, BuiltinType.MASK_INTEGER | BuiltinType.MASK_SIGNED),
    UINT64(NativeType.UINT64, NativeMethods.TYPE_UINT64, BuiltinType.MASK_INTEGER),
    SINT64(NativeType.SINT64, NativeMethods.TYPE_SINT64, BuiltinType.MASK_INTEGER | BuiltinType.MASK_SIGNED),
    POINTER(NativeType.ADDRESS, NativeMethods.TYPE_POINTER, BuiltinType.MASK_INTEGER);

    private static final int MASK_SIGNED = 1;
    private static final int MASK_INTEGER = 2;
    private static final int MASK_FLOAT = 4;

    private final long address;
    private final NativeType nativeType;
    private final long typeInfo;
    private final int mask;

    BuiltinType(NativeType nativeType, int type, int mask) {
        NativeMethods nm = NativeMethods.getInstance();
        this.address = nm.findType(type);
        this.typeInfo = nm.getTypeInfo(address);
        this.mask = mask;
        this.nativeType = nativeType;
    }

    @Override
    public long address() {
        return address;
    }

    @Override
    public NativeType getNativeType() {
        return nativeType;
    }

    @Override
    public int size() {
        return BuiltinTypeHelper.size(typeInfo);
    }

    @Override
    public int alignment() {
        return BuiltinTypeHelper.alignment(typeInfo);
    }

    @Override
    public int type() {
        return BuiltinTypeHelper.type(typeInfo);
    }

    @Override
    public boolean isSigned() {
        return (mask & MASK_SIGNED) != 0;
    }

    public boolean isFloat() {
        return (mask & MASK_FLOAT) != 0;
    }

    public boolean isInteger() {
        return (mask & MASK_INTEGER) != 0;
    }

}
