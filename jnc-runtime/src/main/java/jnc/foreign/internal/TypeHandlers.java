package jnc.foreign.internal;

import jnc.foreign.Foreign;
import jnc.foreign.NativeType;
import jnc.foreign.Pointer;
import jnc.foreign.Struct;
import jnc.foreign.annotation.ContinuouslyEnum;
import jnc.foreign.byref.ByReference;
import jnc.foreign.typedef.Typedef;

@SuppressWarnings({"UtilityClassWithoutPrivateConstructor", "NestedAssignment"})
class TypeHandlers {

    private static final ConcurrentWeakIdentityHashMap<Class<?>, InternalTypeHandler<?>> TYPE_HANDLERS = new ConcurrentWeakIdentityHashMap<>(40);

    static FFIType findReturnType(Class<?> returnType, Typedef annotation) {
        if (annotation != null) {
            return BuiltinTypeHelper.findAlias(annotation.value());
        } else {
            return findType(returnType);
        }
    }

    static FFIType findParameterType(Class<?> parameterType, Typedef annotation) {
        if (annotation != null) {
            return BuiltinTypeHelper.findAlias(annotation.value());
        } else if (Void.class == parameterType) {
            return BuiltinType.POINTER;
        } else {
            return findType(parameterType);
        }
    }

    private static FFIType findType(Class<?> type) {
        if (type.isPrimitive() || Primitives.isWrapperType(type)) {
            return BuiltinTypeHelper.findByType(type);
        } else if (type.isEnum()) {
            return findByType(type).getBuiltinType();
        } else if (Struct.class.isAssignableFrom(type) || Pointer.class.isAssignableFrom(type)) {
            return BuiltinType.POINTER;
        } else if (type.isArray() && type.getComponentType().isPrimitive()) {
            return BuiltinType.POINTER;
        } else if (ByReference.class.isAssignableFrom(type)) {
            return BuiltinType.POINTER;
        }
        throw new UnsupportedOperationException("unsupported type " + type);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    static <T> ParameterHandler<T> getParameterHandler(Class<T> type) {
        if (type.isPrimitive() || Primitives.isWrapperType(type)) {
            return PrimaryTypeHandler.forType(type).getParameterHandler();
        } else if (type.isEnum()) {
            return findByType(type).getParameterHandler();
        } else if (Struct.class.isAssignableFrom(type)) {
            return (ParameterHandler<T>) (ParameterHandler<Struct>) (context, index, obj)
                    -> context.putLong(index, obj == null ? 0 : obj.getMemory().address());
        } else if (Pointer.class.isAssignableFrom(type)) {
            return (ParameterHandler<T>) PointerTypeHandler.INSTANCE.getParameterHandler();
        } else if (type.isArray() && type.getComponentType().isPrimitive()) {
            return PrimitiveArrayParameterHandler.getInstance(type);
        } else if (ByReference.class.isAssignableFrom(type)) {
            return (ParameterHandler<T>) (ParameterHandler<ByReference>) (context, index, obj) -> {
                if (obj == null) {
                    context.putLong(index, 0);
                } else {
                    Foreign foreign = DefaultForeignProvider.getInstance().getForeign();
                    Pointer memory = AllocatedMemory.allocate(obj.componentType(foreign).size());
                    obj.toNative(foreign, memory);
                    context.onFinish(() -> obj.fromNative(foreign, memory)).putLong(index, memory.address());
                }
            };
        }
        throw new UnsupportedOperationException("unsupported type " + type);
    }

    static Invoker getInvoker(Class<?> returnType) {
        if (returnType.isPrimitive() || Primitives.isWrapperType(returnType)) {
            return PrimaryTypeHandler.forType(returnType).getInvoker();
        } else if (returnType == Pointer.class) {
            return PointerTypeHandler.INSTANCE.getInvoker();
        }
        throw new UnsupportedOperationException("unsupported return type " + returnType);
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
            ContinuouslyEnum annotation = AnnotationUtil.getAnnotation(type, ContinuouslyEnum.class);
            return EnumTypeHandler.newInstance((Class) type, annotation);
        }
        throw new UnsupportedOperationException("no type handler for type " + type);
    }

}
