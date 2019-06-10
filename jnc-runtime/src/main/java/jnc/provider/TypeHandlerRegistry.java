package jnc.provider;

import jnc.foreign.NativeType;
import jnc.foreign.Pointer;
import jnc.foreign.Struct;
import jnc.foreign.byref.ByReference;

final class TypeHandlerRegistry implements TypeHandlerFactory {

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

    private final ConcurrentWeakIdentityHashMap<Class<?>, InvokerHandlerInfo> exactReturnTypeMap = new ConcurrentWeakIdentityHashMap<>(16);
    private final ConcurrentWeakIdentityHashMap<Class<?>, InvokerHandlerInfo> inheritedReturnTypeMap = new ConcurrentWeakIdentityHashMap<>(16);
    private final ConcurrentWeakIdentityHashMap<Class<?>, ParameterHandlerInfo> exactParameterTypeMap = new ConcurrentWeakIdentityHashMap<>(16);
    private final ConcurrentWeakIdentityHashMap<Class<?>, ParameterHandlerInfo> inheritedParameterTypeMap = new ConcurrentWeakIdentityHashMap<>(16);

    TypeHandlerRegistry(TypeFactory typeFactory) {
        InternalType pointerType = typeFactory.findByNativeType(NativeType.POINTER);
        PointerHandler pointerHandler = new PointerHandler(pointerType);
        addExactReturnTypeHandler(Pointer.class, pointerHandler);
        addInheritedReturnTypeHandler(Enum.class, EnumHandlers.INSTANCE);
        PrimitiveConverter pc = new PrimitiveConverter();

        // parameter type should not be void, maybe user want to define a pointer type.
        addExactParameterTypeHandler(Void.class, pointerHandler);
        addPrimaryTypeHandler(void.class, NativeType.VOID, null, typeFactory, pc);
        addPrimaryTypeHandler(boolean.class, NativeType.UINT32, CallContext::putBoolean, typeFactory, pc);
        addPrimaryTypeHandler(byte.class, NativeType.SINT8, CallContext::putByte, typeFactory, pc);
        addPrimaryTypeHandler(char.class, NativeType.UINT16, CallContext::putChar, typeFactory, pc);
        addPrimaryTypeHandler(short.class, NativeType.SINT16, CallContext::putShort, typeFactory, pc);
        addPrimaryTypeHandler(int.class, NativeType.SINT32, CallContext::putInt, typeFactory, pc);
        addPrimaryTypeHandler(long.class, NativeType.SINT64, CallContext::putLong, typeFactory, pc);
        addPrimaryTypeHandler(float.class, NativeType.FLOAT, CallContext::putFloat, typeFactory, pc);
        addPrimaryTypeHandler(double.class, NativeType.DOUBLE, CallContext::putDouble, typeFactory, pc);

        addInheritedParameterTypeHandler(Struct.class, new StructHandler(pointerType));
        addInheritedParameterTypeHandler(Pointer.class, pointerHandler);
        addInheritedParameterTypeHandler(ByReference.class, new ByReferenceHandler(pointerType));
        addInheritedParameterTypeHandler(Enum.class, EnumHandlers.INSTANCE);

        addPrimitiveArrayParameterTypeHandler(byte[].class, Pointer::putBytes, Pointer::getBytes, Byte.BYTES, pointerType);
        addPrimitiveArrayParameterTypeHandler(char[].class, Pointer::putCharArray, Pointer::getCharArray, Character.BYTES, pointerType);
        addPrimitiveArrayParameterTypeHandler(short[].class, Pointer::putShortArray, Pointer::getShortArray, Short.BYTES, pointerType);
        addPrimitiveArrayParameterTypeHandler(int[].class, Pointer::putIntArray, Pointer::getIntArray, Integer.BYTES, pointerType);
        addPrimitiveArrayParameterTypeHandler(long[].class, Pointer::putLongArray, Pointer::getLongArray, Long.BYTES, pointerType);
        addPrimitiveArrayParameterTypeHandler(float[].class, Pointer::putFloatArray, Pointer::getFloatArray, Float.BYTES, pointerType);
        addPrimitiveArrayParameterTypeHandler(double[].class, Pointer::putDoubleArray, Pointer::getDoubleArray, Double.BYTES, pointerType);
        addPrimitiveArrayParameterTypeHandler(boolean[].class, TypeHandlerRegistry::putBooleanArray, TypeHandlerRegistry::getBooleanArray, Byte.BYTES, pointerType);
    }

    private <T> void addPrimitiveArrayParameterTypeHandler(
            Class<T> type, ArrayMemoryCopy<T> toNative, ArrayMemoryCopy<T> fromNative, int unit, InternalType pointerType) {
        addExactParameterTypeHandler(type, PrimitiveArrayHandler.of(toNative, fromNative, unit, pointerType));
    }

    private <T> void addPrimaryTypeHandler(
            Class<T> primitiveType, NativeType nativeType,
            ParameterPutter<T> parameterPutter,
            TypeFactory typeFactory, PrimitiveConverter pc) {
        Class<T> wrapType = Primitives.wrap(primitiveType);
        InternalType defaultType = typeFactory.findByNativeType(nativeType);
        InvokerHandlerInfo ihi = PrimitiveFromNativeInfo.of(pc, primitiveType, defaultType);

        addExactReturnTypeHandler(primitiveType, ihi);
        addExactReturnTypeHandler(wrapType, ihi);
        if (parameterPutter != null) {
            addExactParameterTypeHandler(primitiveType, defaultType, parameterPutter);
            addExactParameterTypeHandler(wrapType, defaultType, parameterPutter);
        }
    }

    private <T> void addExactReturnTypeHandler(Class<T> type, InvokerHandlerInfo info) {
        exactReturnTypeMap.putIfAbsent(type, info);
    }

    @SuppressWarnings("SameParameterValue")
    private <T> void addInheritedReturnTypeHandler(Class<T> type, InvokerHandlerInfo info) {
        inheritedReturnTypeMap.putIfAbsent(type, info);
    }

    private <T> void addExactParameterTypeHandler(Class<T> type, InternalType defaultType, ParameterPutter<T> putter) {
        addExactParameterTypeHandler(type, PrimitiveToNativeInfo.of(defaultType, putter));
    }

    private <T> void addExactParameterTypeHandler(Class<T> type, ParameterHandlerInfo info) {
        exactParameterTypeMap.putIfAbsent(type, info);
    }

    private <T> void addInheritedParameterTypeHandler(Class<T> type, ParameterHandlerInfo info) {
        inheritedParameterTypeMap.putIfAbsent(type, info);
    }

    @SuppressWarnings("unchecked")
    private <T> T extractFromMap(ConcurrentWeakIdentityHashMap<Class<?>, ?> exact,
            ConcurrentWeakIdentityHashMap<Class<?>, ?> inherited, Class<?> type) {
        {
            T result = (T) exact.get(type);
            if (result != null) {
                return result;
            }
        }
        // maybe type is an interface
        for (Class<?> klass = type; klass != null && klass != Object.class; klass = klass.getSuperclass()) {
            T result = (T) inherited.get(klass);
            if (result != null) {
                return result;
            }
        }
        for (Class<?> iface : type.getInterfaces()) {
            T result = (T) inherited.get(iface);
            if (result != null) {
                return result;
            }
        }
        throw new UnsupportedOperationException("no type handler for type '" + type.getName() + "'");
    }

    @Override
    public InvokerHandlerInfo findReturnTypeInfo(Class<?> returnType) {
        return extractFromMap(exactReturnTypeMap, inheritedReturnTypeMap, returnType);
    }

    @Override
    public ParameterHandlerInfo findParameterTypeInfo(Class<?> type) {
        return extractFromMap(exactParameterTypeMap, inheritedParameterTypeMap, type);
    }

}
