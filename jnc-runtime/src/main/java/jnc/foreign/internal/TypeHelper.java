package jnc.foreign.internal;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import jnc.foreign.NativeType;
import jnc.foreign.annotation.Typedef;
import jnc.foreign.enums.TypeAlias;

@SuppressWarnings("UtilityClassWithoutPrivateConstructor")
class TypeHelper {

    static Alias findByAlias(TypeAlias typeAlias) {
        Objects.requireNonNull(typeAlias, "type alias");
        return AliasMapHolder.find(typeAlias);
    }

    static InternalType findByNativeType(NativeType nativeType) {
        Objects.requireNonNull(nativeType, "native type");
        InternalType internalType = NativeMapHolder.MAP.get(nativeType);
        if (internalType == null) {
            throw new IllegalArgumentException("unsupported native type " + nativeType);
        }
        return internalType;
    }

    static BuiltinType findByPrimaryType(Class<?> type) {
        return PrimitivesMapHolder.getByType(type);
    }

    static TypeInfo getTypeInfo(int type) {
        return TypesHolder.get(type);
    }

    private static class TypesHolder {

        private static final TypeInfo[] TYPE_INFOS;

        static {
            long[][] types = NativeMethods.getInstance().getTypes();
            int len = types.length;
            TypeInfo[] typeInfos = new TypeInfo[len];
            for (int i = 0; i < len; ++i) {
                long[] arr = types[i];
                if (arr != null) {
                    long address = arr[0];
                    long info = arr[1];
                    typeInfos[i] = new TypeInfo(address, info);
                }
            }
            TYPE_INFOS = typeInfos;
        }

        private static TypeInfo get(int type) {
            try {
                TypeInfo ti = TYPE_INFOS[type];
                if (ti != null) {
                    return ti;
                }
            } catch (IndexOutOfBoundsException ignored) {
            }
            throw new IllegalArgumentException("unsupported type " + type);
        }

    }

    private static class AliasMapHolder {

        private static final Map<TypeAlias, Alias> MAP;

        static {
            Map<Integer, BuiltinType> builtinTypes = EnumSet.allOf(BuiltinType.class)
                    .stream().collect(Collectors.toMap(BuiltinType::type, Function.identity()));
            HashMap<String, Integer> nativeAliasMap = new HashMap<>(50);
            NativeMethods.getInstance().initAlias(nativeAliasMap);
            EnumMap<TypeAlias, Alias> map = new EnumMap<>(TypeAlias.class);
            for (Map.Entry<String, Integer> entry : nativeAliasMap.entrySet()) {
                String key = entry.getKey();
                Integer value = entry.getValue();
                TypeAlias typeAlias = toTypeAlias(key);
                if (typeAlias != null) {
                    map.put(typeAlias, new Alias(typeAlias, builtinTypes.get(value)));
                }
            }
            MAP = map;
        }

        private static TypeAlias toTypeAlias(String name) {
            try {
                return TypeAlias.valueOf(name);
            } catch (IllegalArgumentException ex) {
                // ok might be int,long,pointer
            }
            if (name.equals("int")) {
                return TypeAlias.cint;
            } else if (name.equals("long")) {
                return TypeAlias.clong;
            }
            return null;
        }

        static Alias find(TypeAlias name) {
            Alias alias = MAP.get(name);
            if (alias == null) {
                throw new UnsupportedOperationException("type " + name + " is not supported on current platform");
            }
            return alias;
        }
    }

    private static class PrimitivesMapHolder {

        private static final Map<Class<?>, BuiltinType> MAP;

        static {
            Map<Class<?>, BuiltinType> map = new HashMap<>(16);
            put(map, void.class, BuiltinType.VOID);
            put(map, boolean.class, BuiltinType.UINT8);
            put(map, byte.class, BuiltinType.SINT8);
            put(map, short.class, BuiltinType.SINT16);
            put(map, char.class, BuiltinType.UINT16);
            put(map, int.class, BuiltinType.SINT32);
            put(map, long.class, BuiltinType.SINT64);
            put(map, float.class, BuiltinType.FLOAT);
            put(map, double.class, BuiltinType.DOUBLE);
            MAP = map;
        }

        private static void put(Map<Class<?>, BuiltinType> map, Class<?> klass, BuiltinType builtinType) {
            map.put(klass, builtinType);
        }

        private static BuiltinType getByType(Class<?> type) {
            return MAP.get(Primitives.unwrap(type));
        }

    }

    private interface NativeMapHolder {

        EnumMap<NativeType, InternalType> MAP = EnumSet.allOf(BuiltinType.class).stream()
                .collect(Collectors.toMap(BuiltinType::getNativeType, Function.identity(),
                        (a, b) -> b, () -> new EnumMap<>(NativeType.class)));
    }

}
