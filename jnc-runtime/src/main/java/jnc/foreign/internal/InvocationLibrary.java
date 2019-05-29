package jnc.foreign.internal;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import jnc.foreign.LoadOptions;
import jnc.foreign.enums.CallingConvention;

class InvocationLibrary<T> {

    static <T> T create(Class<T> interfaceClass, Library library, LoadOptions loadOptions,
            TypeHandlerRegistry typeHandlerRegistry) {
        return new InvocationLibrary<>(interfaceClass, library, loadOptions, typeHandlerRegistry).create();
    }

    private final Class<T> interfaceClass;
    private final Library library;
    private final CallingConvention classConvention;
    private final TypeHandlerRegistry typeHandlerRegistry;

    // visible for test
    InvocationLibrary(Class<T> interfaceClass, Library library, LoadOptions options, TypeHandlerRegistry typeHandlerRegistry) {
        this.interfaceClass = interfaceClass;
        jnc.foreign.annotation.CallingConvention classConventionAnnotation
                = AnnotationContext.newContext(interfaceClass)
                        .getAnnotation(jnc.foreign.annotation.CallingConvention.class);
        this.library = library;
        this.classConvention = classConventionAnnotation != null ? classConventionAnnotation.value() : options.getCallingConvention();
        this.typeHandlerRegistry = typeHandlerRegistry;
    }

    private T create() {
        return ProxyBuilder.builder().useProxyMethods().useDefaultMethod()
                .otherwise(this::find).newInstance(interfaceClass);
    }

    // visible for test
    MethodInvocation find(Method method) {
        if (method.isVarArgs()) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        String name = method.getName();
        long function = library.dlsym(name);
        AnnotationContext ac = AnnotationContext.newContext(method);
        TypeHandlerInfo<? extends Invoker<?>> returnTypeInfo = typeHandlerRegistry.findReturnTypeInfo(method.getReturnType());
        InternalType retType = returnTypeInfo.getType(ac);
        Invoker<?> invoker = returnTypeInfo.getHandler();
        Class<?>[] parameterTypes = method.getParameterTypes();
        AnnotationContext[] mpacs = AnnotationContext.newMethodParameterContexts(method);
        int len = parameterTypes.length;
        InternalType[] ptypes = new InternalType[len];
        @SuppressWarnings("rawtypes")
        ParameterHandler<?>[] handlers = new ParameterHandler[len];
        for (int i = 0; i < len; ++i) {
            Class<?> type = parameterTypes[i];
            TypeHandlerInfo<? extends ParameterHandler<?>> typeHandlerInfo = typeHandlerRegistry.findParameterTypeInfo(type);
            ptypes[i] = typeHandlerInfo.getType(mpacs[i]);
            handlers[i] = typeHandlerInfo.getHandler();
        }
        jnc.foreign.annotation.CallingConvention methodConvention = ac.getAnnotation(jnc.foreign.annotation.CallingConvention.class);
        return new MethodInvocation(handlers, methodConvention != null ? methodConvention.value() : classConvention, invoker, function, retType, ptypes);
    }

    // visible for test
    @SuppressWarnings("PackageVisibleInnerClass")
    static class MethodInvocation implements InvocationHandler {

        private final CallingConvention callingConvention;
        private final ParameterHandler<?>[] handlers;
        private final CifContainer container;
        private final Invoker<?> invoker;
        private final long function;

        MethodInvocation(ParameterHandler<?>[] handlers, CallingConvention callingConvention, Invoker<?> invoker, long function, InternalType retType, InternalType[] ptypes) {
            this.callingConvention = callingConvention;
            this.handlers = handlers;
            this.container = CifContainer.create(callingConvention, retType, ptypes);
            this.invoker = invoker;
            this.function = function;
        }

        CallingConvention getCallingConvention() {
            return callingConvention;
        }

        @Override
        public Object invoke(Object proxy, Method m, Object[] args) {
            @SuppressWarnings("unchecked")
            ParameterHandler<Object>[] h = (ParameterHandler<Object>[]) handlers;
            int length = h.length;
            if (length != 0) {
                CallContext context = container.newCallContext();
                for (int i = 0; i < length; i++) {
                    h[i].handle(context, i, args[i]);
                }
                return context.invoke(invoker, function);
            } else {
                return invoker.invoke(container.getCifAddress(), function, 0, null);
            }
        }
    }

}
