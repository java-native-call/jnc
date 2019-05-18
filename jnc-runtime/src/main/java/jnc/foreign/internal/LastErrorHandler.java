package jnc.foreign.internal;

@FunctionalInterface
interface LastErrorHandler {

    void handle(int error);

}
