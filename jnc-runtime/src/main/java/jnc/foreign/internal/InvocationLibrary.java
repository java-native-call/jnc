package jnc.foreign.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import jnc.foreign.LoadOptions;
import jnc.foreign.enums.CallingConvention;

@SuppressWarnings("UtilityClassWithoutPrivateConstructor")
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
    InvocationLibrary(Class<T> interfaceClass, Library library, LoadOptions loadOptions, TypeHandlerRegistry typeHandlerRegistry) {
        this.interfaceClass = interfaceClass;
        AnnotatedElementContext aec = new AnnotatedElementContext(interfaceClass);
        jnc.foreign.annotation.CallingConvention classConventionAnnotation = aec.getAnnotation(jnc.foreign.annotation.CallingConvention.class);
        CallingConvention optionConvention = loadOptions.getCallingConvention();
        this.library = library;
        this.classConvention = optionConvention != null ? optionConvention : classConventionAnnotation != null ? classConventionAnnotation.value() : CallingConvention.DEFAULT;
        this.typeHandlerRegistry = typeHandlerRegistry;
    }

    private T create() {
        return new ProxyBuilder().useProxyMethods().useDefaultMethod()
                .otherwise(this::find).newInstance(interfaceClass);
    }

    // visible for test
    MethodInvocation find(Method method) {
        if (method.isVarArgs()) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        String name = method.getName();
        long function = library.dlsym(name);
        AnnotatedElementContext mac = new AnnotatedElementContext(method);
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
        public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
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
