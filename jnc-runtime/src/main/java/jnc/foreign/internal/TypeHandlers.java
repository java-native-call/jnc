package jnc.foreign.internal;

import jnc.foreign.Pointer;
import jnc.foreign.Struct;
import jnc.foreign.annotation.Continuously;
import jnc.foreign.byref.ByReference;
import jnc.foreign.typedef.Typedef;

@SuppressWarnings({"UtilityClassWithoutPrivateConstructor", "NestedAssignment"})
class TypeHandlers {

    private static final ConcurrentWeakIdentityHashMap<Class<?>, InternalTypeHandler<?>> TYPE_HANDLERS = new ConcurrentWeakIdentityHashMap<>(40);

    static FFIType findReturnType(Class<?> returnType, Typedef annotation) {
        if (Pointer.class.isAssignableFrom(returnType)) {
            return BuiltinType.POINTER;
        } else if (annotation != null) {
            return BuiltinTypeHelper.findByAlias(annotation);
        } else {
            return findType(returnType);
        }
    }

    static FFIType findParameterType(Class<?> parameterType, Typedef annotation) {
        if (Struct.class.isAssignableFrom(parameterType) || Pointer.class.isAssignableFrom(parameterType)) {
            return BuiltinType.POINTER;
        } else if (parameterType.isArray() && parameterType.getComponentType().isPrimitive()) {
            return BuiltinType.POINTER;
        } else if (ByReference.class.isAssignableFrom(parameterType)) {
            return BuiltinType.POINTER;
        } else if (annotation != null) {
            return BuiltinTypeHelper.findByAlias(annotation);
        } else if (Void.class == parameterType) {
            return BuiltinType.POINTER;
        } else {
            return findType(parameterType);
        }
    }

    private static FFIType findType(Class<?> type) {
        return findByType(type).getBuiltinType();
    }

    static <T> ParameterHandler<T> getParameterHandler(Class<T> type) {
        if (Struct.class.isAssignableFrom(type)) {
            return StructTypeHandler.<T>getInstance().getParameterHandler();
        } else if (Pointer.class.isAssignableFrom(type)) {
            return PointerTypeHandler.<T>getInstance().getParameterHandler();
        } else if (type.isArray() && type.getComponentType().isPrimitive()) {
            return PrimitiveArrayParameterHandler.getInstance(type);
        } else if (ByReference.class.isAssignableFrom(type)) {
            return ByReferenceTypeHandler.<T>getInstance().getParameterHandler();
        } else {
            return findByType(type).getParameterHandler();
        }
    }

    static <T> Invoker<T> getInvoker(Class<T> returnType) {
        return findByType(returnType).getInvoker();
    }

    @SuppressWarnings("unchecked")
    static <T> InternalTypeHandler<T> findByType(Class<T> type) {
        return (InternalTypeHandler<T>) TYPE_HANDLERS.computeIfAbsent(type, TypeHandlers::findByType0);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> InternalTypeHandler<T> findByType0(Class<T> type) {
        if (type.isPrimitive() || Primitives.isWrapperType(type)) {
            return PrimaryTypeHandler.forType(type);
        } else if (type.isEnum()) {
            Continuously annotation = AnnotationUtil.getClassAnnotation(type, Continuously.class);
            return EnumTypeHandler.newInstance((Class) type, annotation);
        } else if (type == Pointer.class) {
            return PointerTypeHandler.getInstance();
        }
        throw new UnsupportedOperationException("no type handler for type " + type);
    }

}
