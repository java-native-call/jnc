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
        CallingConvention optionConvention = loadOptions.getCallingConvention();
        final CallingConvention classConvention = optionConvention != null ? optionConvention : classConventionAnnotation != null ? classConventionAnnotation.value() : CallingConvention.DEFAULT;
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
                    TypeHandlerInfo<? extends Invoker<?>> returnTypeInfo = typeHandlerRegistry.findReturnTypeInfo(method.getReturnType());
                    InternalType retType = returnTypeInfo.getType(mac);
                    Invoker<?> invoker = returnTypeInfo.getHandler();
                    Class<?>[] parameterTypes = method.getParameterTypes();
                    Annotation[][] annotations = method.getParameterAnnotations();
                    int len = parameterTypes.length;
                    InternalType[] ptypes = new InternalType[len];
                    @SuppressWarnings("rawtypes")
                    ParameterHandler<?>[] handlers = new ParameterHandler[len];
                    for (int i = 0; i < len; ++i) {
                        Class<?> type = parameterTypes[i];
                        MethodParameterAnnotationContext mpac = new MethodParameterAnnotationContext(annotations[i]);
                        TypeHandlerInfo<? extends ParameterHandler<?>> typeHandlerInfo = typeHandlerRegistry.findParameterTypeInfo(type);
                        ptypes[i] = typeHandlerInfo.getType(mpac);
                        handlers[i] = typeHandlerInfo.getHandler();
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
