package jnc.foreign.internal;

import javax.annotation.Nonnull;
import jnc.foreign.Foreign;
import jnc.foreign.ForeignProvider;
import jnc.foreign.LoadOptions;
import jnc.foreign.MemoryManager;
import jnc.foreign.NativeType;
import jnc.foreign.Platform;
import jnc.foreign.Type;
import jnc.foreign.TypeHandler;
import jnc.foreign.enums.TypeAlias;

class DefaultForeign implements Foreign {

    private final ForeignProvider provider;
    private final TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();

    DefaultForeign(ForeignProvider provider) {
        this.provider = provider;
    }

    @Nonnull
    @Override
    public <T> T load(Class<T> interfaceClass, String libname, LoadOptions loadOptions) {
        return InvocationLibrary.create(interfaceClass, Library.open(libname, 0), loadOptions, typeHandlerRegistry);
    }

    @Nonnull
    @Override
    public Platform getPlatform() {
        return DefaultPlatform.getInstance();
    }

    @Override
    public final void close() {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public MemoryManager getMemoryManager() {
        return NativeMemoryManager.getInstance();
    }

    @Nonnull
    @Override
    public Type findType(TypeAlias alias) {
        return TypeHelper.findByAlias(alias);
    }

    @Nonnull
    @Override
    public Type findType(NativeType nativeType) {
        return TypeHelper.findByNativeType(nativeType);
    }

    @Override
    public <T> TypeHandler<T> findTypeHandler(Class<T> type) throws UnsupportedOperationException {
        return typeHandlerRegistry.findHandler(type);
    }

    @Nonnull
    @Override
    public ForeignProvider provider() {
        return provider;
    }

    @Override
    public int getLastError() {
        return ThreadLocalError.get();
    }

}
