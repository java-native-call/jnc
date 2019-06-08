package jnc.provider;

@FunctionalInterface
interface LastErrorHandler {

    long METHOD_ID = ThreadLocalError.getMethodId();

    void handle(int error);

}
