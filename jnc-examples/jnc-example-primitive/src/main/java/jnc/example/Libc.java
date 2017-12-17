package jnc.example;

import jnc.foreign.LibraryLoader;
import jnc.foreign.Platform;
import jnc.foreign.typedef.size_t;
import jnc.foreign.typedef.uintptr_t;

public interface Libc {

    Libc INSTANCE = LibraryLoader.create(Libc.class).load(Platform.getNativePlatform().getLibcName());

    @uintptr_t
    long memcpy(@uintptr_t long dst, @uintptr_t long src, @size_t long n);

}
