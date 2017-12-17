package jnc.foreign.internal;

import jnc.foreign.Foreign;
import jnc.foreign.Pointer;
import jnc.foreign.Struct;
import jnc.foreign.byref.ByReference;
import jnc.foreign.typedef.Typedef;

@SuppressWarnings({"UtilityClassWithoutPrivateConstructor", "NestedAssignment"})
class TypeHandlers {

    static FFIType findReturnType(Class<?> returnType, Typedef annotation) {
        if (returnType.isPrimitive() || Primitives.isWrapperType(returnType)) {
            return BuiltinType.findByType(returnType, annotation);
        }
        return findType(returnType, annotation);
    }

    static FFIType findParameterType(Class<?> parameterType, Typedef annotation) {
        if (parameterType.isPrimitive() || Primitives.isWrapperType(parameterType)) {
            if (annotation == null && Void.class == parameterType) {
                return BuiltinType.POINTER;
            }
            return BuiltinType.findByType(parameterType, annotation);
        }
        return findType(parameterType, annotation);
    }

    private static FFIType findType(Class<?> type, /*nullable*/ Typedef aliasA) {
        if (Struct.class.isAssignableFrom(type) || Pointer.class.isAssignableFrom(type)) {
            return BuiltinType.POINTER;
        }
        if (Struct[].class.isAssignableFrom(type)) {
            return BuiltinType.POINTER;
        }
        if (type.isArray() && type.getComponentType().isPrimitive()) {
            return BuiltinType.POINTER;
        }
        if (ByReference.class.isAssignableFrom(type)) {
            return BuiltinType.POINTER;
        }
        throw new UnsupportedOperationException("unsupported type " + type);
    }

    @SuppressWarnings("unchecked")
    static <T> ParameterHandler<T> forParameterHandler(Class<T> type) {
        if (type.isPrimitive() || Primitives.isWrapperType(type)) {
            return (ParameterHandler<T>) ParameterHandlers.forHandler(type);
        } else if (Struct.class.isAssignableFrom(type)) {
            return (ParameterHandler<T>) (ParameterHandler<Struct>) (context, index, obj)
                    -> context.putLong(index, obj == null ? 0 : obj.getMemory().address());
        } else if (Pointer.class.isAssignableFrom(type)) {
            return (ParameterHandler<T>) (ParameterHandler<Pointer>) (context, index, obj)
                    -> context.putLong(index, obj != null ? obj.address() : 0);
        }
        if (type.isArray() && type.getComponentType().isPrimitive()) {
            return PrimitiveArrayParameterHandler.getInstance(type);
        }
        if (ByReference.class.isAssignableFrom(type)) {
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
        if (Struct[].class.isAssignableFrom(type)) {
            return (ParameterHandler<T>) StructArrayHandler.getInstance();
        }
        throw new UnsupportedOperationException("unsupported type " + type);
    }

    static Invoker forInvoker(Class<?> returnType) {
        Invoker invoker = Invokers.forInvoker(returnType);
        if (invoker != null) {
            return invoker;
        }
        throw new UnsupportedOperationException("unsupported return type " + returnType);
    }

}
