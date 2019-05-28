package jnc.foreign.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.function.Function;
import jnc.foreign.LoadOptions;
import jnc.foreign.enums.CallingConvention;

@SuppressWarnings("UtilityClassWithoutPrivateConstructor")
class InvocationLibrary {

    static <T> T create(Class<T> interfaceClass, Library library, LoadOptions loadOptions,
            TypeHandlerRegistry typeHandlerRegistry) {
        ClassAnnotationContext cac = new ClassAnnotationContext(interfaceClass);
        jnc.foreign.annotation.CallingConvention classConventionAnnotation = cac.getAnnotation(jnc.foreign.annotation.CallingConvention.class);
        CallingConvention optionConvention = loadOptions.getCallingConvention();
        final CallingConvention classConvention = optionConvention != null ? optionConvention : classConventionAnnotation != null ? classConventionAnnotation.value() : CallingConvention.DEFAULT;
        return new ProxyBuilder().useProxyMethods().useDefaultMethod()
                .otherwise(new Otherwise(library, typeHandlerRegistry, classConvention))
                .newInstance(interfaceClass);
    }

    private static class Otherwise implements Function<Method, InvocationHandler> {

        private final Library library;
        private final TypeHandlerRegistry typeHandlerRegistry;
        private final CallingConvention classConvention;

        Otherwise(Library library, TypeHandlerRegistry typeHandlerRegistry, CallingConvention classConvention) {
            this.library = library;
            this.typeHandlerRegistry = typeHandlerRegistry;
            this.classConvention = classConvention;
        }

        @Override
        public InvocationHandler apply(Method method) {
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
            return new InvocationHandlerImpl(handlers, cif, invoker, function);
        }

    }

    private static class InvocationHandlerImpl implements InvocationHandler {

        private final ParameterHandler<?>[] handlers;
        private final ffi_cif cif;
        private final Invoker<?> invoker;
        private final long function;

        InvocationHandlerImpl(ParameterHandler<?>[] handlers, ffi_cif cif, Invoker<?> invoker, long function) {
            this.handlers = handlers;
            this.cif = cif;
            this.invoker = invoker;
            this.function = function;
        }

        @Override
        public Object invoke(Object proxy, Method m, Object[] args) throws Throwable {
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
        }
    }

}
