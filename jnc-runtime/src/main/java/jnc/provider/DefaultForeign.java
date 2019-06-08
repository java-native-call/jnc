package jnc.provider;

import java.util.Objects;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import jnc.foreign.Foreign;
import jnc.foreign.LoadOptions;
import jnc.foreign.MemoryManager;
import jnc.foreign.NativeType;
import jnc.foreign.Type;
import jnc.foreign.enums.TypeAlias;
import jnc.foreign.support.TypeHandler;

@ParametersAreNonnullByDefault
enum DefaultForeign implements Foreign {

    INSTANCE;

    private final TypeFactory typeFactory;
    private final TypeHandlerFactory typeHandlerFactory;

    DefaultForeign() {
        TypeFactory tf;
        TypeHandlerFactory thf;
        try {
            tf = new TypeRegistry();
            thf = new TypeHandlerRegistry(tf);
        } catch (Throwable ex) {
            ProxyBuilder builder = ProxyBuilder.builder().orThrow(ex);
            tf = builder.newInstance(TypeFactory.class);
            thf = builder.newInstance(TypeHandlerFactory.class);
        }
        typeFactory = tf;
        typeHandlerFactory = thf;
    }

    TypeFactory getTypeFactory() {
        return typeFactory;
    }

    TypeHandlerFactory getTypeHandlerFactory() {
        return typeHandlerFactory;
    }

    @Nonnull
    @Override
    public <T> T load(Class<T> interfaceClass, @Nullable String libname, LoadOptions loadOptions) {
        Objects.requireNonNull(interfaceClass, "interfaceClass");
        Objects.requireNonNull(loadOptions, "loadOptions");
        try {
            return InvocationLibrary.create(interfaceClass, NativeLibrary.open(libname, 0),
                    loadOptions, typeFactory, typeHandlerFactory);
        } catch (Throwable t) {
            if (!loadOptions.isFailImmediately()) {
                return ProxyBuilder.builder().orThrow(t).newInstance(interfaceClass);
            }
            throw t;
        }
    }

    @Override
    public final void close() {
        throw new UnsupportedOperationException();
    }

    @Nonnull
    @Override
    public MemoryManager getMemoryManager() {
        return DefaultMemoryManager.INSTANCE;
    }

    @Nonnull
    @Override
    public Alias findType(TypeAlias alias) {
        return typeFactory.findByAlias(alias);
    }

    @Nonnull
    @Override
    public Type findType(NativeType nativeType) {
        return typeFactory.findByNativeType(nativeType);
    }

    @Nonnull
    @Override
    @SuppressWarnings("unchecked")
    public <T> TypeHandler<T> getTypeHandler(Class<T> type) {
        if (type.isEnum()) {
            return EnumTypeHandler.getInstance((Class) type);
        }
        throw new UnsupportedOperationException("no type handler present for type " + type);
    }

    @Override
    public int getLastError() {
        return ThreadLocalError.get();
    }

}
