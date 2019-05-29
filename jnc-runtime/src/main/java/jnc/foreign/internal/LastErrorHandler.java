package jnc.foreign.internal;

@FunctionalInterface
interface LastErrorHandler {

    long METHOD_ID = ThreadLocalError.getMethodId();

    void handle(int error);

}
