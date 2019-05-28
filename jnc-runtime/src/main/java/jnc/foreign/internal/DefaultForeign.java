package jnc.foreign.internal;

import javax.annotation.Nonnull;
import jnc.foreign.FieldAccessor;
import jnc.foreign.Foreign;
import jnc.foreign.ForeignProvider;
import jnc.foreign.LoadOptions;
import jnc.foreign.MemoryManager;
import jnc.foreign.NativeType;
import jnc.foreign.Type;
import jnc.foreign.enums.TypeAlias;

enum DefaultForeign implements Foreign {

    INSTANCE;

    private final TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();

    @Nonnull
    @Override
    public ForeignProvider provider() {
        return DefaultForeignProvider.INSTANCE;
    }

    @Nonnull
    @Override
    public <T> T load(Class<T> interfaceClass, String libname, LoadOptions loadOptions) {
        return InvocationLibrary.create(interfaceClass, NativeLibrary.open(libname, 0), loadOptions, typeHandlerRegistry);
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
    public Type findType(TypeAlias alias) {
        return TypeHelper.findByAlias(alias);
    }

    @Nonnull
    @Override
    public Type findType(NativeType nativeType) {
        return TypeHelper.findByNativeType(nativeType);
    }

    @Nonnull
    @Override
    public <E extends Enum<E>> FieldAccessor<E> getEnumFieldAccessor(Class<E> type) {
        return EnumTypeHandler.getInstance(type).getFieldAccessor();
    }

    @Override
    public int getLastError() {
        return ThreadLocalError.get();
    }

}
