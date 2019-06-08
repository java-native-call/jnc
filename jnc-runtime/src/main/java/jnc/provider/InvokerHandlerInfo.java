package jnc.provider;

interface InvokerHandlerInfo {

    InternalType getType(Class<?> returnType, TypeFactory typeFactory, AnnotationContext ac);

    InvokeHandler<?> getHandler(Class<?> returnType, InternalType retType);

}
