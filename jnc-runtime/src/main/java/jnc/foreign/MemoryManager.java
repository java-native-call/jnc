package jnc.foreign;

import java.nio.charset.Charset;
import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public interface MemoryManager {

    @Nonnull
    Pointer allocate(long size);

    @Nonnull
    Pointer allocateWithAlign(long size, long alignment);

    /**
     * Allocate exact size of memory to put the string and null terminator.
     *
     * @throws NullPointerException if {@code string} or {@code charset} is null
     * @throws OutOfMemoryError if unable to allocate memory with specified
     * string.
     */
    @Nonnull
    Pointer allocateString(String string, Charset charset);

}
