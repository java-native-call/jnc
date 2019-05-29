package jnc.foreign.internal;

import java.lang.reflect.Array;
import javax.annotation.Nullable;
import jnc.foreign.Foreign;
import jnc.foreign.ForeignProvider;
import jnc.foreign.NativeType;
import jnc.foreign.Pointer;
import jnc.foreign.Struct;
import jnc.foreign.byref.ByReference;

final class TypeHandlerRegistry implements TypeHandlerFactory {

    private static void putByReference(CallContext context, int index, ByReference obj) {
        if (obj == null) {
            context.putLong(index, 0);
        } else {
            Foreign foreign = ForeignProvider.getDefault().getForeign();
            Pointer memory = AllocatedMemory.allocate(obj.componentType(foreign).size());
            obj.toNative(foreign, memory);
            context.onFinish(() -> obj.fromNative(foreign, memory)).putLong(index, memory.address());
        }
    }

    private static void putBooleanArray(Pointer memory, int offset, boolean[] array, int off, int len) {
        for (int i = off; i < len; i++) {
            memory.putByte(offset + i, (byte) (array[i] ? 1 : 0));
        }
    }

    private static void getBooleanArray(Pointer memory, int offset, boolean[] array, int off, int len) {
        for (int i = off; i < len; i++) {
            array[i] = memory.getByte(offset + i) != 0;
        }
    }

    @SuppressWarnings("NestedAssignment")
    private static <T> ParameterHandler<T> toParameterHandler(ArrayParameterHandler<T> toNative, ArrayParameterHandler<T> fromNative, int size) {
        return (CallContext context, int index, T array) -> {
            int len;
            if (array == null) {
                context.putLong(index, 0);
            } else if ((len = Array.getLength(array)) == 0) {
                context.putLong(index, EmptyMemoryHolder.NOMEMORY.address());
            } else {
                int offset = 0;
                int off = 0;
                Pointer memory = AllocatedMemory.allocate(len, size);
                toNative.handle(memory, offset, array, off, len);
                context.onFinish(() -> fromNative.handle(memory, offset, array, off, len)).putLong(index, memory.address());
            }
        };
    }
    private final TypeFactory typeFactory;

    private final ConcurrentWeakIdentityHashMap<Class<?>, TypeHandlerInfo<?>> exactReturnTypeMap = new ConcurrentWeakIdentityHashMap<>(16);
    private final ConcurrentWeakIdentityHashMap<Class<?>, TypeHandlerInfo<?>> inheritedReturnTypeMap = new ConcurrentWeakIdentityHashMap<>(16);
    private final ConcurrentWeakIdentityHashMap<Class<?>, TypeHandlerInfo<?>> exactParameterTypeMap = new ConcurrentWeakIdentityHashMap<>(16);
    private final ConcurrentWeakIdentityHashMap<Class<?>, TypeHandlerInfo<?>> inheritedParameterTypeMap = new ConcurrentWeakIdentityHashMap<>(16);

    TypeHandlerRegistry(TypeFactory typeFactory) {
        InternalType pointer = typeFactory.getPointerType();
        this.typeFactory = typeFactory;
        addExactReturnTypeHandler(Pointer.class, TypeHandlerInfo.always(pointer, Invokers::invokePointer));

        addPrimaryTypeHandler(void.class, NativeType.VOID, (context, index, __) -> context.putLong(index, 0), Invokers::invokeVoid);
        addPrimaryTypeHandler(boolean.class, NativeType.UINT8, CallContext::putBoolean, Invokers::invokeBoolean);
        addPrimaryTypeHandler(byte.class, NativeType.SINT8, CallContext::putByte, Invokers::invokeByte);
        addPrimaryTypeHandler(char.class, NativeType.UINT16, CallContext::putChar, Invokers::invokeChar);
        addPrimaryTypeHandler(short.class, NativeType.SINT16, CallContext::putShort, Invokers::invokeShort);
        addPrimaryTypeHandler(int.class, NativeType.SINT32, CallContext::putInt, Invokers::invokeInt);
        addPrimaryTypeHandler(long.class, NativeType.SINT64, CallContext::putLong, Invokers::invokeLong);
        addPrimaryTypeHandler(float.class, NativeType.FLOAT, CallContext::putFloat, Invokers::invokeFloat);
        addPrimaryTypeHandler(double.class, NativeType.DOUBLE, CallContext::putDouble, Invokers::invokeDouble);

        addInheritedParameterTypeHandler(Struct.class, TypeHandlerInfo.always(pointer, (context, index, obj) -> context.putLong(index, obj == null ? 0 : obj.getMemory().address())));
        addInheritedParameterTypeHandler(Pointer.class, TypeHandlerInfo.always(pointer, (context, index, obj) -> context.putLong(index, obj == null ? 0 : obj.address())));
        addInheritedParameterTypeHandler(ByReference.class, TypeHandlerInfo.always(pointer, TypeHandlerRegistry::putByReference));

        addPrimitiveArrayParameterTypeHandler(byte[].class, Pointer::putBytes, Pointer::getBytes, Byte.BYTES);
        addPrimitiveArrayParameterTypeHandler(char[].class, Pointer::putCharArray, Pointer::getCharArray, Character.BYTES);
        addPrimitiveArrayParameterTypeHandler(short[].class, Pointer::putShortArray, Pointer::getShortArray, Short.BYTES);
        addPrimitiveArrayParameterTypeHandler(int[].class, Pointer::putIntArray, Pointer::getIntArray, Integer.BYTES);
        addPrimitiveArrayParameterTypeHandler(long[].class, Pointer::putLongArray, Pointer::getLongArray, Long.BYTES);
        addPrimitiveArrayParameterTypeHandler(float[].class, Pointer::putFloatArray, Pointer::getFloatArray, Float.BYTES);
        addPrimitiveArrayParameterTypeHandler(double[].class, Pointer::putDoubleArray, Pointer::getDoubleArray, Double.BYTES);
        addPrimitiveArrayParameterTypeHandler(boolean[].class, TypeHandlerRegistry::putBooleanArray, TypeHandlerRegistry::getBooleanArray, Byte.BYTES);
    }

    private <T> void addPrimitiveArrayParameterTypeHandler(Class<T> primitiveArrayType, ArrayParameterHandler<T> toNative, ArrayParameterHandler<T> fromNative, int size) {
        ParameterHandler<T> parameterHandler = toParameterHandler(toNative, fromNative, size);
        addExactParameterTypeHandler(primitiveArrayType, TypeHandlerInfo.always(typeFactory.getPointerType(), parameterHandler));
    }

    private <T> void addPrimaryTypeHandler(Class<T> primitiveType, NativeType nativeType,
            ParameterHandler<T> parameterHandler, Invoker<T> invoker) {
        Class<T> wrapType = Primitives.wrap(primitiveType);
        InternalType defaultType = typeFactory.findByNativeType(nativeType);

        addExactReturnTypeHandler(primitiveType, defaultType, invoker);
        addExactReturnTypeHandler(wrapType, defaultType, invoker);
        if (primitiveType != void.class) {
            addExactParameterTypeHandler(primitiveType, defaultType, parameterHandler);
            addExactParameterTypeHandler(wrapType, defaultType, parameterHandler);
        } else {
            // parameter type should not be void, maybe user want to define a pointer type.
            addExactParameterTypeHandler(wrapType, typeFactory.getPointerType(), parameterHandler);
        }
    }

    private <T> void addExactReturnTypeHandler(Class<T> returnType, InternalType defaultType, Invoker<T> invoker) {
        addExactReturnTypeHandler(returnType, TypeHandlerInfo.typedefFirst(defaultType, invoker));
    }

    private <T> void addExactReturnTypeHandler(Class<T> returnType, TypeHandlerInfo<Invoker<T>> returnTypeHandlerInfo) {
        exactReturnTypeMap.putIfAbsent(returnType, returnTypeHandlerInfo);
    }

    @SuppressWarnings("unused")
    private <T> void addInheritedReturnTypeHandler(Class<T> returnType, InternalType defaultType, Invoker<T> invoker) {
        addInheritedReturnTypeHandler(returnType, TypeHandlerInfo.typedefFirst(defaultType, invoker));
    }

    private <T> void addInheritedReturnTypeHandler(Class<T> returnType, TypeHandlerInfo<Invoker<T>> returnTypeHandlerInfo) {
        inheritedReturnTypeMap.putIfAbsent(returnType, returnTypeHandlerInfo);
    }

    private <T> void addExactParameterTypeHandler(Class<T> parameterType, InternalType defaultType, ParameterHandler<T> parameterHandler) {
        addExactParameterTypeHandler(parameterType, TypeHandlerInfo.typedefFirst(defaultType, parameterHandler));
    }

    private <T> void addExactParameterTypeHandler(Class<T> parameterType, TypeHandlerInfo<ParameterHandler<T>> typeHandlerInfo) {
        exactParameterTypeMap.putIfAbsent(parameterType, typeHandlerInfo);
    }

    private <T> void addInheritedParameterTypeHandler(Class<T> parameterType, TypeHandlerInfo<ParameterHandler<T>> typeHandlerInfo) {
        inheritedParameterTypeMap.putIfAbsent(parameterType, typeHandlerInfo);
    }

    @Nullable
    @SuppressWarnings("unchecked")
    private <T> T extractFromMap(ConcurrentWeakIdentityHashMap<Class<?>, ?> exact,
            ConcurrentWeakIdentityHashMap<Class<?>, ?> inherited, Class<?> type) {
        T result = (T) exact.get(type);
        if (result != null) {
            return result;
        }
        // maybe type is an interface
        for (Class<?> klass = type; klass != null && klass != Object.class; klass = klass.getSuperclass()) {
            result = (T) inherited.get(klass);
            if (result != null) {
                return result;
            }
        }
        for (Class<?> iface : type.getInterfaces()) {
            result = (T) inherited.get(iface);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T> TypeHandlerInfo<Invoker<T>> findReturnTypeInfo(Class<T> returnType) {
        TypeHandlerInfo<Invoker<T>> typeHandlerInfo = extractFromMap(exactReturnTypeMap, inheritedReturnTypeMap, returnType);
        if (typeHandlerInfo != null) {
            return typeHandlerInfo;
        }
        EnumTypeHandler typeHandler = findEnumHandler(returnType);
        Invoker<T> invoker = typeHandler.getInvoker();
        TypeHandlerInfo<Invoker<T>> rthi = TypeHandlerInfo.typedefFirst(typeHandler.getDefaultType(), invoker);
        addExactReturnTypeHandler(returnType, rthi);
        return rthi;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private EnumTypeHandler findEnumHandler(Class type) {
        try {
            return EnumTypeHandler.getInstance(type);
        } catch (IllegalArgumentException ex) {
            throw new UnsupportedOperationException("no type handler for type '" + type.getName() + "'");
        }
    }

    @Override
    @SuppressWarnings({"rawtypes", "unchecked"})
    public <T> TypeHandlerInfo<ParameterHandler<T>> findParameterTypeInfo(Class<T> type) {
        TypeHandlerInfo<ParameterHandler<T>> typeHandlerInfo = extractFromMap(exactParameterTypeMap, inheritedParameterTypeMap, type);
        if (typeHandlerInfo != null) {
            return typeHandlerInfo;
        }
        EnumTypeHandler typeHandler = findEnumHandler(type);
        ParameterHandler<T> parameterHandler = typeHandler.getParameterHandler();
        InternalType internalType = typeHandler.getDefaultType();
        TypeHandlerInfo<ParameterHandler<T>> phi = TypeHandlerInfo.typedefFirst(internalType, parameterHandler);
        addExactParameterTypeHandler(type, phi);
        return phi;
    }

    private interface ArrayParameterHandler<T> {

        void handle(Pointer memory, int offset, T array, int off, int len);

    }

    private interface EmptyMemoryHolder {

        Pointer NOMEMORY = AllocatedMemory.allocate(0);

    }

}
