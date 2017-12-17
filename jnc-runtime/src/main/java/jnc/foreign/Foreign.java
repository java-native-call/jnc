package jnc.foreign;

import java.io.Closeable;

public interface Foreign extends Closeable {

    ForeignProvider provider();

    <T> T load(Class<T> interfaceClass, String libname, LoadOptions loadOptions);

    Platform getPlatform();

    Type findType(String alias);

    Type findType(NativeType nativeType);

    MemoryManager getMemoryManager();

    int getLastError();

}
