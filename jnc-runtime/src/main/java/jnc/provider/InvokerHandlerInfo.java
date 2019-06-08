package jnc.provider;

interface InvokerHandlerInfo {

    InternalType getType(Class<?> returnType, TypeFactory typeFactory, AnnotationContext ac);

    RawConverter<?> getRawConverter(Class<?> returnType, InternalType retType);

}
