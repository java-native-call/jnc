package jnc.foreign.internal;

import jnc.foreign.NativeType;
import jnc.foreign.typedef.Typedef;

class BuiltinType implements SimpleType {

    private static final int MASK_SIGNED = 1;
    private static final int MASK_INTEGER = 2;
    private static final int MASK_FLOAT = 4;

    private static final NativeMethods nm = NativeMethods.getInstance();
    public static final BuiltinType VOID = new BuiltinType(NativeType.VOID, NativeMethods.TYPE_VOID, 0);
    public static final BuiltinType FLOAT = new BuiltinType(NativeType.FLOAT, NativeMethods.TYPE_FLOAT, MASK_SIGNED | MASK_FLOAT);
    public static final BuiltinType DOUBLE = new BuiltinType(NativeType.DOUBLE, NativeMethods.TYPE_DOUBLE, MASK_SIGNED | MASK_FLOAT);
    public static final BuiltinType UINT8 = new BuiltinType(NativeType.UINT8, NativeMethods.TYPE_UINT8, MASK_INTEGER);
    public static final BuiltinType SINT8 = new BuiltinType(NativeType.SINT8, NativeMethods.TYPE_SINT8, MASK_INTEGER | MASK_SIGNED);
    public static final BuiltinType UINT16 = new BuiltinType(NativeType.UINT16, NativeMethods.TYPE_UINT16, MASK_INTEGER);
    public static final BuiltinType SINT16 = new BuiltinType(NativeType.SINT16, NativeMethods.TYPE_SINT16, MASK_INTEGER | MASK_SIGNED);
    public static final BuiltinType UINT32 = new BuiltinType(NativeType.UINT32, NativeMethods.TYPE_UINT32, MASK_INTEGER);
    public static final BuiltinType SINT32 = new BuiltinType(NativeType.SINT32, NativeMethods.TYPE_SINT32, MASK_INTEGER | MASK_SIGNED);
    public static final BuiltinType UINT64 = new BuiltinType(NativeType.UINT64, NativeMethods.TYPE_UINT64, MASK_INTEGER);
    public static final BuiltinType SINT64 = new BuiltinType(NativeType.SINT64, NativeMethods.TYPE_SINT64, MASK_INTEGER | MASK_SIGNED);
    public static final BuiltinType POINTER = new BuiltinType(NativeType.ADDRESS, NativeMethods.TYPE_POINTER, MASK_INTEGER);

    static BuiltinType findByType(Class<?> type, /*nullable*/ Typedef alias) {
        return BuiltinTypeHelper.findByType(type, alias);
    }

    static BuiltinType findAlias(String name) {
        return BuiltinTypeHelper.findAlias(name);
    }

    static BuiltinType findByNativeType(NativeType nativeType) {
        return BuiltinTypeHelper.findByNativeType(nativeType);
    }

    static BuiltinType[] values() {
        return new BuiltinType[]{
            VOID, FLOAT, DOUBLE,
            UINT8, SINT8, UINT16, SINT16,
            UINT32, SINT32, UINT64, SINT64,
            POINTER
        };
    }

    private final long address;
    private final NativeType nativeType;
    private final long typeInfo;
    private final int mask;

    BuiltinType(NativeType nativeType, int type, int mask) {
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

    String name() {
        return nativeType.name();
    }

    @Override
    public String toString() {
        return name();
    }

}
