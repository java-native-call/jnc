package jnc.foreign.internal;

import jnc.foreign.NativeType;
import jnc.foreign.enums.TypeAlias;

final class Alias implements InternalType {

    private final TypeAlias typeAlias;
    private final TypeInfo typeInfo;

    Alias(TypeAlias typeAlias, TypeInfo typeInfo) {
        this.typeAlias = typeAlias;
        this.typeInfo = typeInfo;
    }

    @Override
    public NativeType nativeType() {
        return typeInfo.nativeType();
    }

    @Override
    public long address() {
        return typeInfo.address();
    }

    @Override
    public int size() {
        return typeInfo.size();
    }

    @Override
    public int alignment() {
        return typeInfo.alignment();
    }

    @Override
    public int type() {
        return typeInfo.type();
    }

    @Override
    public boolean isFloatingPoint() {
        return typeInfo.isFloatingPoint();
    }

    @Override
    public boolean isIntegral() {
        return typeInfo.isIntegral();
    }

    @Override
    public boolean isSigned() {
        return typeInfo.isSigned();
    }

    TypeAlias getTypeAlias() {
        return typeAlias;
    }

    @Override
    public String toString() {
        return "Alias(" + getTypeAlias() + "=" + nativeType() + ")";
    }

    @Deprecated
    @Override
    public void do_not_implement_this_for_its_used_internally() {
    }

}
