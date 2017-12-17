package jnc.foreign.internal;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import jnc.foreign.NativeType;
import jnc.foreign.typedef.Typedef;

@SuppressWarnings("UtilityClassWithoutPrivateConstructor")
class BuiltinTypeHelper {

    private static final NativeMethods nm = NativeMethods.getInstance();
    private static final BuiltinType[] TYPES;
    private static final EnumMap<NativeType, BuiltinType> MAP;

    static {
        BuiltinType[] builtinTypes = BuiltinType.values();
        int maxType = 0;
        for (BuiltinType builtinType : builtinTypes) {
            maxType = Math.max(maxType, builtinType.type());
        }
        BuiltinType[] types = new BuiltinType[maxType + 1];
        for (BuiltinType type : builtinTypes) {
            int typeId = type.type();
            if (types[typeId] == null) {
                types[typeId] = type;
            } else {
                throw new AssertionError("duplicate type " + typeId);
            }
        }
        TYPES = types;
        EnumMap<NativeType, BuiltinType> map = new EnumMap<>(NativeType.class);
        for (BuiltinType builtinType : builtinTypes) {
            map.put(builtinType.getNativeType(), builtinType);
        }
        MAP = map;
    }

    static BuiltinType findByType(int type) {
        try {
            BuiltinType result = TYPES[type];
            if (result != null) {
                return result;
            }
        } catch (IndexOutOfBoundsException ex) {
        }
        throw new IllegalArgumentException("unsupport type " + type);
    }

    static BuiltinType findAlias(String name) {
        try {
            // throw NullPointerException if name is null
            return findByType(nm.findAlias(name));
        } catch (IllegalArgumentException ex) {
            throw new AssertionError(ex);
        } catch (UnsupportedOperationException ex) {
        }
        throw new IllegalArgumentException("unsupport alias " + name);
    }

    static BuiltinType findByNativeType(NativeType nativeType) {
        BuiltinType builtinType = MAP.get(nativeType);
        if (builtinType == null) {
            throw new IllegalArgumentException("unsupport native type " + nativeType);
        }
        return builtinType;
    }

    static int size(long typeInfo) {
        return (int) (typeInfo >>> 32);
    }

    static int alignment(long typeInfo) {
        return (int) (typeInfo >> 16) & 0xFF;
    }

    static int type(long typeInfo) {
        return (int) typeInfo & 0xFF;
    }

    private static BuiltinType findByType(Class<?> type) {
        return PrimitivesMap.getByType(type);
    }

    static BuiltinType findByType(Class<?> type, /*nullable*/ Typedef alias) {
        if (alias != null) {
            return findAlias(alias.value());
        }
        return findByType(type);
    }

    private static class PrimitivesMap {

        private static final Map<Class<?>, BuiltinType> MAP;

        static {
            Map<Class<?>, BuiltinType> hashMap = new HashMap<>(16);
            put(hashMap, void.class, BuiltinType.VOID);
            put(hashMap, boolean.class, BuiltinType.UINT8);
            put(hashMap, byte.class, BuiltinType.SINT8);
            put(hashMap, short.class, BuiltinType.SINT16);
            put(hashMap, char.class, BuiltinType.UINT16);
            put(hashMap, int.class, BuiltinType.SINT32);
            put(hashMap, long.class, BuiltinType.SINT64);
            put(hashMap, float.class, BuiltinType.FLOAT);
            put(hashMap, double.class, BuiltinType.DOUBLE);
            MAP = hashMap;
        }

        private static void put(Map<Class<?>, BuiltinType> map, Class<?> klass, BuiltinType builtinType) {
            map.put(klass, builtinType);
        }

        private static BuiltinType getByType(Class<?> type) {
            return MAP.get(Primitives.unwrap(type));
        }

    }

}
