package jnc.provider;

import jnc.foreign.NativeType;
import jnc.foreign.enums.TypeAlias;

final class Alias implements InternalType {

    private final TypeAlias typeAlias;
    private final InternalType delegate;

    Alias(TypeAlias typeAlias, BuiltinType delegate) {
        this.typeAlias = typeAlias;
        this.delegate = delegate;
    }

    @Override
    public NativeType nativeType() {
        return delegate.nativeType();
    }

    @Override
    public long address() {
        return delegate.address();
    }

    @Override
    public int size() {
        return delegate.size();
    }

    @Override
    public int alignment() {
        return delegate.alignment();
    }

    @Override
    public int type() {
        return delegate.type();
    }

    @Override
    public boolean isFloatingPoint() {
        return delegate.isFloatingPoint();
    }

    @Override
    public boolean isIntegral() {
        return delegate.isIntegral();
    }

    @Override
    public boolean isSigned() {
        return delegate.isSigned();
    }

    TypeAlias getTypeAlias() {
        return typeAlias;
    }

    @Override
    public String toString() {
        return getTypeAlias().name();
    }

    @Deprecated
    @Override
    public void do_not_implement_this_for_its_used_internally() {
    }

}
