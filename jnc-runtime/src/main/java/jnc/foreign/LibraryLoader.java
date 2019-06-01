package jnc.foreign;

import java.util.Objects;
import javax.annotation.Nonnull;
import jnc.foreign.enums.CallingConvention;

@SuppressWarnings("WeakerAccess")
public class LibraryLoader<T> {

    @Nonnull
    public static <T> LibraryLoader<T> create(Class<T> cl) {
        Objects.requireNonNull(cl, "interface class");
        return new LibraryLoader<>(cl);
    }

    private final Class<T> interfaceClass;
    private final LoadOptionsBuilder loadOptionsBuilder = LoadOptions.builder();

    private LibraryLoader(Class<T> interfaceClass) {
        this.interfaceClass = interfaceClass;
    }

    @Nonnull
    public LibraryLoader<T> stdcall() {
        return convention(CallingConvention.STDCALL);
    }

    @Nonnull
    public LibraryLoader<T> convention(CallingConvention convention) {
        loadOptionsBuilder.convention(convention);
        return this;
    }

    @Nonnull
    public LibraryLoader<T> failImmediately() {
        loadOptionsBuilder.failImmediately();
        return this;
    }

    @Nonnull
    public LibraryLoader<T> failDeferred() {
        loadOptionsBuilder.failDeferred();
        return this;
    }

    @Nonnull
    public T load(String libname) {
        return Foreign.getDefault().load(interfaceClass, libname, loadOptionsBuilder.build());
    }

}
