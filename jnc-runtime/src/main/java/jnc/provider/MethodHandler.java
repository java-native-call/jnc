package jnc.provider;

@FunctionalInterface
interface MethodHandler {

    Object invoke(Object obj, Object[] args) throws Throwable;

}
