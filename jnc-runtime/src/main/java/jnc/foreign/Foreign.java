package jnc.foreign;

import java.io.Closeable;
import javax.annotation.Nonnull;
import jnc.foreign.enums.TypeAlias;

public interface Foreign extends Closeable {

    @Nonnull
    ForeignProvider provider();

    @Nonnull
    <T> T load(Class<T> interfaceClass, String libname, LoadOptions loadOptions);

    @Nonnull
    Platform getPlatform();

    @Nonnull
    Type findType(TypeAlias alias) throws IllegalArgumentException;

    @Nonnull
    Type findType(NativeType nativeType) throws IllegalArgumentException;

    <T> TypeHandler<T> findTypeHandler(Class<T> clazz) throws UnsupportedOperationException;

    @Nonnull
    MemoryManager getMemoryManager();

    int getLastError();

}
