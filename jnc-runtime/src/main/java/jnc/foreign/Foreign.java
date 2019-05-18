package jnc.foreign;

import java.io.Closeable;
import javax.annotation.Nonnull;

public interface Foreign extends Closeable {

    @Nonnull
    ForeignProvider provider();

    @Nonnull
    <T> T load(Class<T> interfaceClass, String libname, LoadOptions loadOptions);

    @Nonnull
    Platform getPlatform();

    @Nonnull
    Type findType(String alias) throws IllegalArgumentException;

    @Nonnull
    Type findType(NativeType nativeType) throws IllegalArgumentException;

    @Nonnull
    MemoryManager getMemoryManager();

    int getLastError();

}
