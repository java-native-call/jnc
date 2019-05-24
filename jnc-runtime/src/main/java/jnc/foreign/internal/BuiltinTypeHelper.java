package jnc.foreign.internal;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import jnc.foreign.NativeType;
import jnc.foreign.typedef.Typedef;

@SuppressWarnings("UtilityClassWithoutPrivateConstructor")
class BuiltinTypeHelper {

    static Alias findAlias(String name) {
        Objects.requireNonNull(name, "name");
        return AliasMapHolder.find(name);
    }

    static BuiltinType findByNativeType(NativeType nativeType) {
        Objects.requireNonNull(nativeType, "native type");
        BuiltinType builtinType = NativeMapHolder.MAP.get(nativeType);
        if (builtinType == null) {
            throw new IllegalArgumentException("unsupported native type " + nativeType);
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

    static BuiltinType findByPrimaryType(Class<?> type) {
        return PrimitivesMapHolder.getByType(type);
    }

    static Alias findByAlias(Typedef alias) {
        return findAlias(alias.value());
    }

    private static class AliasMapHolder {

        private static final Map<String, Alias> MAP;

        static {
            Map<Integer, BuiltinType> builtinTypes = EnumSet.allOf(BuiltinType.class)
                    .stream().collect(Collectors.toMap(BuiltinType::type, Function.identity()));
            HashMap<String, Integer> nativeAliasMap = new HashMap<>(50);
            NativeMethods.getInstance().initAlias(nativeAliasMap);
            MAP = nativeAliasMap.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey,
                            entry -> new Alias(entry.getKey(), builtinTypes.get(entry.getValue()))));
        }

        static Alias find(String name) {
            Alias alias = MAP.get(name);
            if (alias == null) {
                throw new IllegalArgumentException("unsupported alias " + name);
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

        EnumMap<NativeType, BuiltinType> MAP = EnumSet.allOf(BuiltinType.class).stream()
                .collect(Collectors.toMap(BuiltinType::getNativeType, Function.identity(),
                        (a, b) -> b, () -> new EnumMap<>(NativeType.class)));
    }

}
