package jnc.foreign.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import jnc.foreign.LoadOptions;
import jnc.foreign.enums.CallingConvention;

interface InvocationLibrary {

    static <T> T create(Class<T> interfaceClass, Library library, LoadOptions loadOptions,
            TypeHandlerRegistry typeHandlerRegistry) {
        ClassAnnotationContext cac = new ClassAnnotationContext(interfaceClass);
        jnc.foreign.annotation.CallingConvention classConventionAnnotation = cac.getAnnotation(jnc.foreign.annotation.CallingConvention.class);
        final CallingConvention classConvention = classConventionAnnotation != null ? classConventionAnnotation.value() : loadOptions.getCallingConvention();
        return new ProxyBuilder()
                .useProxyMethods()
                .useDefaultMethod()
                .otherwise(method -> {
                    if (method.isVarArgs()) {
                        throw new UnsupportedOperationException("Not supported yet.");
                    }
                    String name = method.getName();
                    long function = library.dlsym(name);
                    MethodAnnotationContext mac = new MethodAnnotationContext(method);
                    ReturnTypeHandlerInfo<?> returnTypeInfo = typeHandlerRegistry.findReturnTypeInfo(method.getReturnType());
                    InternalType retType = returnTypeInfo.getInternalType(mac);
                    Invoker<?> invoker = returnTypeInfo.getInvoker();
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    Annotation[][] annotations = method.getParameterAnnotations();
                    int len = parameterTypes.length;
                    InternalType[] ptypes = new InternalType[len];
                    @SuppressWarnings("rawtypes")
                    ParameterHandler<?>[] handlers = new ParameterHandler[len];
                    for (int i = 0; i < len; ++i) {
                        Class<?> type = parameterTypes[i];
                        MethodParameterAnnotationContext mpac = new MethodParameterAnnotationContext(annotations[i]);
                        ParameterHandlerInfo<?> handlerInfo = typeHandlerRegistry.findParameterTypeInfo(type);
                        ptypes[i] = handlerInfo.getType(mpac);
                        handlers[i] = handlerInfo.getHandler();
                    }
                    jnc.foreign.annotation.CallingConvention methodConvention = mac.getAnnotation(jnc.foreign.annotation.CallingConvention.class);
                    ffi_cif cif = new ffi_cif(methodConvention != null ? methodConvention.value() : classConvention, retType, ptypes);
                    return (Object proxy, Method m, Object[] args) -> {
                        @SuppressWarnings("unchecked")
                        ParameterHandler<Object>[] h = (ParameterHandler<Object>[]) handlers;
                        int length = h.length;
                        if (length != 0) {
                            CallContext context = cif.newCallContext();
                            for (int i = 0; i < length; i++) {
                                h[i].handle(context, i, args[i]);
                            }
                            Object result = invoker.invoke(cif.address(), function, context.address());
                            context.finish();
                            return result;
                        } else {
                            return invoker.invoke(cif.address(), function, EmptyMemoryHolder.NOMEMORY.address());
                        }
                    };
                }).toInstance(interfaceClass);

    }

}
