package jnc.foreign.internal;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import jnc.foreign.Foreign;
import jnc.foreign.ForeignProvider;
import jnc.foreign.LoadOptions;
import jnc.foreign.MemoryManager;
import jnc.foreign.NativeType;
import jnc.foreign.Platform;
import jnc.foreign.Type;

class ForeignImpl implements Foreign {

    private final ForeignProvider provider;

    ForeignImpl(ForeignProvider provider) {
        this.provider = provider;
    }

    @Override
    public <T> T load(Class<T> interfaceClass, String libname, LoadOptions loadOptions) {
        InvocationLibrary library = new InvocationLibrary(interfaceClass, NativeLibrary.open(libname, 0), loadOptions);
        return interfaceClass.cast(Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                (Object proxy, Method method, Object[] args)
                -> library.findMethodInvoker(method).invoke(proxy, method, args)));
    }

    @Override
    public Platform getPlatform() {
        return DefaultPlatform.getInstance();
    }

    @Override
    @SuppressWarnings("FinalMethod")
    public final void close() throws IOException {
        throw new UnsupportedOperationException();
    }

    @Override
    public MemoryManager getMemoryManager() {
        return NativeMemoryManager.getInstance();
    }

    @Override
    public Type findType(String alias) {
        return BuiltinType.findAlias(alias);
    }

    @Override
    public Type findType(NativeType nativeType) {
        return BuiltinType.findByNativeType(nativeType);
    }

    @Override
    public ForeignProvider provider() {
        return provider;
    }

    @Override
    public int getLastError() {
        return ThreadLocalError.get();
    }

}
