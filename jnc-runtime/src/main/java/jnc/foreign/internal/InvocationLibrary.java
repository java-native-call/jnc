package jnc.foreign.internal;

import java.lang.reflect.Method;
import jnc.foreign.LoadOptions;
import jnc.foreign.enums.CallingConvention;

final class InvocationLibrary<T> {

    static <T> T create(Class<T> interfaceClass, Library library, LoadOptions loadOptions,
            TypeFactory typeFactory, TypeHandlerFactory typeHandlerFactory) {
        return new InvocationLibrary<>(interfaceClass, library, loadOptions, typeFactory, typeHandlerFactory).create();
    }

    private final Class<T> interfaceClass;
    private final Library library;
    private final CallingConvention classConvention;
    private final TypeHandlerFactory typeHandlerFactory;
    private final TypeFactory typeFactory;

    @VisibleForTesting
    InvocationLibrary(Class<T> interfaceClass, Library library, LoadOptions options,
            TypeFactory typeFactory, TypeHandlerFactory typeHandlerFactory) {
        this.interfaceClass = interfaceClass;
        jnc.foreign.annotation.CallingConvention classConventionAnnotation
                = AnnotationContext.newContext(interfaceClass)
                        .getAnnotation(jnc.foreign.annotation.CallingConvention.class);
        this.library = library;
        this.classConvention = classConventionAnnotation != null ? classConventionAnnotation.value() : options.getCallingConvention();
        this.typeFactory = typeFactory;
        this.typeHandlerFactory = typeHandlerFactory;
    }

    private T create() {
        return ProxyBuilder.builder().otherwise(this::find).newInstance(interfaceClass);
    }

    @VisibleForTesting
    MethodInvocation find(Method method) {
        AnnotationContext ac = AnnotationContext.newContext(method);
        jnc.foreign.annotation.CallingConvention methodConvention = ac.getAnnotation(jnc.foreign.annotation.CallingConvention.class);
        CallingConvention convention = methodConvention != null ? methodConvention.value() : classConvention;
        Entry entry = ac.getAnnotation(Entry.class);
        String name;
        if (entry != null) {
            name = entry.value();
        } else {
            name = method.getName();
        }

        long function = library.dlsym(name);
        InvokerHandlerInfo returnTypeInfo = typeHandlerFactory.findReturnTypeInfo(method.getReturnType());
        InternalType retType = returnTypeInfo.getType(method.getReturnType(), typeFactory, ac);
        InvokeHandler<?> handler = returnTypeInfo.getHandler(method.getReturnType(), retType);
        Class<?>[] parameterTypes = method.getParameterTypes();

        int len;
        Class<?> variadicType;
        AnnotationContext[] mpacs = AnnotationContext.newMethodParameterContexts(method);
        if (method.isVarArgs()) {
            len = parameterTypes.length - 1;
            if (len == 0) {
                throw new IllegalStateException("method " + method + " is variadic, at least one fixed argument is required.");
            }
            variadicType = parameterTypes[len].getComponentType();
        } else {
            len = parameterTypes.length;
            variadicType = null;
        }
        InternalType[] ptypes = new InternalType[len];
        @SuppressWarnings("rawtypes")
        ParameterHandler<?>[] handlers = new ParameterHandler[len];
        for (int i = 0; i < len; ++i) {
            Class<?> type = parameterTypes[i];
            ParameterHandlerInfo<?> info = typeHandlerFactory.findParameterTypeInfo(type);
            ptypes[i] = info.getType(typeFactory, mpacs[i]);
            handlers[i] = info.getHandler();
        }
        if (method.isVarArgs()) {
            return new VariadicMethodInvocation(handlers, convention, handler, function, retType, ptypes, variadicType, mpacs[len], typeFactory, typeHandlerFactory);
        }
        return new FixedMethodInvocation(handlers, convention, handler, function, retType, ptypes);
    }

}
