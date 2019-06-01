package jnc.foreign;

import java.io.Closeable;
import javax.annotation.Nonnull;
import jnc.foreign.enums.TypeAlias;
import jnc.foreign.spi.ForeignProvider;

public interface Foreign extends Closeable {

    @Nonnull
    static Foreign getDefault() {
        return ForeignProvider.getDefault().getForeign();
    }

    @Nonnull
    <T> T load(Class<T> interfaceClass, String libname, LoadOptions loadOptions);

    /**
     * @throws UnsupportedOperationException if specified type is not supported
     * on current platform.
     */
    @Nonnull
    Type findType(TypeAlias alias) throws UnsupportedOperationException;

    @Nonnull
    Type findType(NativeType nativeType);

    @Nonnull
    <E extends Enum<E>> FieldAccessor<E> getEnumFieldAccessor(Class<E> type);

    @Nonnull
    MemoryManager getMemoryManager();

    int getLastError();

}
