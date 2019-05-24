package jnc.foreign.internal;

import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import javax.annotation.Nonnull;
import jnc.foreign.Foreign;
import jnc.foreign.ForeignProvider;
import jnc.foreign.LoadOptions;
import jnc.foreign.MemoryManager;
import jnc.foreign.NativeType;
import jnc.foreign.Platform;
import jnc.foreign.Type;
import jnc.foreign.TypeHandler;

class ForeignImpl implements Foreign {

    private final ForeignProvider provider;

    ForeignImpl(ForeignProvider provider) {
        this.provider = provider;
    }

    @Nonnull
    @Override
    public <T> T load(Class<T> interfaceClass, String libname, LoadOptions loadOptions) {
        InvocationLibrary library = new InvocationLibrary(interfaceClass, NativeLibrary.open(libname, 0), loadOptions);
        return interfaceClass.cast(Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                (Object proxy, Method method, Object[] args)
                -> library.findMethodInvoker(method).invoke(proxy, method, args)));
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
    public Type findType(String alias) {
        return TypeHelper.findAlias(alias);
    }

    @Nonnull
    @Override
    public Type findType(NativeType nativeType) {
        return TypeHelper.findByNativeType(nativeType);
    }

    @Override
    public <T> TypeHandler<T> findTypeHandler(Class<T> clazz) throws UnsupportedOperationException {
        return TypeHandlers.findByType(clazz);
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
