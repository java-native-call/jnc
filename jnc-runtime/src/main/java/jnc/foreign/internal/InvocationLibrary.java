package jnc.foreign.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import jnc.foreign.LoadOptions;
import jnc.foreign.Pointer;
import jnc.foreign.abi.CallingConvertion;
import jnc.foreign.abi.CallingMode;
import jnc.foreign.typedef.Typedef;

class InvocationLibrary {

    private final NativeLibrary library;
    private final CallingMode callingMode;
    private final ConcurrentMap<Method, InvocationHandler> map = new ConcurrentHashMap<>(4);

    InvocationLibrary(Class<?> interfaceClass, NativeLibrary nativeLibrary, LoadOptions loadOptions) {
        this.library = nativeLibrary;
        CallingConvertion callingConvertion = AnnotationUtil.getAnnotation(interfaceClass, CallingConvertion.class);
        this.callingMode = callingConvertion != null ? callingConvertion.value() : loadOptions.getCallingMode();
    }

    public InvocationHandler findMethodInvoker(Method method) {
        InvocationHandler handler = map.get(method);
        if (handler != null) {
            return handler;
        }
        if (method.getDeclaringClass() == Object.class) {
            handler = (proxy, m, args) -> {
                InvocationHandler ih = Proxy.getInvocationHandler(proxy);
                switch (m.getName()) {
                    case "equals":
                        Object another = args[0];
                        return proxy == another
                                || another != null
                                && Proxy.isProxyClass(another.getClass())
                                && ih == Proxy.getInvocationHandler(another);
                    case "hashCode":
                        return System.identityHashCode(ih);
                    case "toString":
                        return proxy.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(ih));
                }
                throw new AssertionError();
            };
        } else if (method.isDefault()) {
            if (DefaultMethodInvoker.isAvailiable()) {
                handler = DefaultMethodInvoker.getInstance(method);
            } else {
                throw new UnsupportedOperationException("Default method");
            }
        } else if (method.isVarArgs()) {
            throw new UnsupportedOperationException("Not supported yet.");
        } else {
            String name = method.getName();
            long function = library.dlsym(name);
            FFIType retType = TypeHandlers.findReturnType(method.getReturnType(), AnnotationUtil.getAnnotation(method, Typedef.class));
            Class<?>[] parameterTypes = method.getParameterTypes();
            Annotation[][] annotationses = method.getParameterAnnotations();
            int len = parameterTypes.length;
            FFIType[] ptypes = new FFIType[len];
            Invoker invoker = TypeHandlers.forInvoker(method.getReturnType());
            @SuppressWarnings("rawtypes")
            ParameterHandler<?>[] handlers = new ParameterHandler[len];
            for (int i = 0; i < len; ++i) {
                Class<?> type = parameterTypes[i];
                Typedef aliasA = AnnotationUtil.getAnnotation(annotationses[i], Typedef.class);
                ptypes[i] = TypeHandlers.findParameterType(type, aliasA);
                handlers[i] = TypeHandlers.forParameterHandler(type);
            }
            CallingConvertion annotation = AnnotationUtil.getAnnotation(method, CallingConvertion.class);
            ffi_cif cif = new ffi_cif(annotation != null ? annotation.value() : callingMode, retType, ptypes);
            handler = (Object proxy, Method m, Object[] args) -> {
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
                    return invoker.invoke(cif.address(), function, NoParameter.NOMEMORY.address());
                }
            };
        }
        InvocationHandler ih = map.putIfAbsent(method, handler);
        return ih != null ? ih : handler;
    }

    private interface NoParameter {

        Pointer NOMEMORY = AllocatedMemory.allocate(0);

    }

}
