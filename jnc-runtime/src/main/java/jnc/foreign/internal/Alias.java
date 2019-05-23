package jnc.foreign.internal;

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

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return "Alias(" + name + ",target=" + delegate + ")";
    }

}
