package jnc.foreign.internal;

import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;
import jnc.foreign.Pointer;

@SuppressWarnings("UtilityClassWithoutPrivateConstructor")
class PrimitiveArrayParameterHandler {

    private static final Map<Class<?>, ParameterHandler<?>> MAP;

    static {
        Map<Class<?>, ParameterHandler<?>> map = new HashMap<>(16);
        put(map, byte[].class, Pointer::putBytes, Pointer::getBytes, Byte.BYTES);
        put(map, char[].class, Pointer::putCharArray, Pointer::getCharArray, Character.BYTES);
        put(map, short[].class, Pointer::putShortArray, Pointer::getShortArray, Short.BYTES);
        put(map, int[].class, Pointer::putIntArray, Pointer::getIntArray, Integer.BYTES);
        put(map, long[].class, Pointer::putLongArray, Pointer::getLongArray, Long.BYTES);
        put(map, float[].class, Pointer::putFloatArray, Pointer::getFloatArray, Float.BYTES);
        put(map, double[].class, Pointer::putDoubleArray, Pointer::getDoubleArray, Double.BYTES);
        MAP = map;
    }

    @SuppressWarnings("NestedAssignment")
    private static <T> void put(Map<Class<?>, ParameterHandler<?>> map,
            Class<T> type, ArrayParameterHandler<T> toNative, ArrayParameterHandler<T> fromNative, int size) {
        map.put(type, (CallContext context, int index, T array) -> {
            int len;
            if (array == null || (len = Array.getLength(array)) == 0) {
                context.putLong(index, 0);
            } else {
                int offset = 0;
                int off = 0;
                Pointer memory = AllocatedMemory.allocate(len, size);
                toNative.handle(memory, offset, array, off, len);
                context.onFinish(() -> fromNative.handle(memory, offset, array, off, len)).putLong(index, memory.address());
            }
        });
    }

    static <T> ParameterHandler<T> getInstance(Class<T> type) {
        @SuppressWarnings("unchecked")
        ParameterHandler<T> handler = (ParameterHandler<T>) MAP.get(type);
        if (handler != null) {
            return handler;
        }
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private interface ArrayParameterHandler<T> {

        void handle(Pointer memory, int offset, T array, int off, int len);

    }

}
