package jnc.foreign.internal;

import jnc.foreign.NativeType;

class Alias implements InternalType {

    private final String name;
    private final BuiltinType delegate;

    Alias(String name, BuiltinType delegate) {
        this.name = name;
        this.delegate = delegate;
    }

    @Override
    public long address() {
        return delegate.address();
    }

    @Override
    public NativeType getNativeType() {
        return delegate.getNativeType();
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
    public boolean isSigned() {
        return delegate.isSigned();
    }

    public boolean isFloatingPoint() {
        return delegate.isFloatingPoint();
    }

    public boolean isIntegral() {
        return delegate.isIntegral();
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Alias(" + getName() + ",target=" + delegate + ")";
    }

}
