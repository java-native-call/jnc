package jnc.foreign.internal;

import jnc.foreign.enums.TypeAlias;

class Alias implements InternalType {

    private final TypeAlias typeAlias;
    private final BuiltinType delegate;

    Alias(TypeAlias typeAlias, BuiltinType delegate) {
        this.typeAlias = typeAlias;
        this.delegate = delegate;
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

    boolean isSigned() {
        return delegate.isSigned();
    }

    public TypeAlias getTypeAlias() {
        return typeAlias;
    }

    @Override
    public String toString() {
        return "Alias(" + typeAlias + ",target=" + delegate + ")";
    }

}
