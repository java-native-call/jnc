package jnc.foreign.internal;

import java.io.Closeable;

interface Library extends NativeObject, Closeable {

    @Override
    long address();

    long dlsym(String name) throws UnsatisfiedLinkError;

    @Override
    void close();

}
