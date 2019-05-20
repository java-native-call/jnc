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
        return LazyAlias.find(name);
    }

    static BuiltinType findByNativeType(NativeType nativeType) {
        BuiltinType builtinType = LazyNative.NATIVE_MAP.get(nativeType);
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
        return PrimitivesMap.getByType(type);
    }

    static Alias findByAlias(Typedef alias) {
        return findAlias(alias.value());
    }

    private interface LazyNative {

        EnumMap<NativeType, BuiltinType> NATIVE_MAP = EnumSet.allOf(BuiltinType.class).stream()
                .collect(Collectors.toMap(BuiltinType::getNativeType, Function.identity(),
                        (a, b) -> b, () -> new EnumMap<>(NativeType.class)));
    }

    private static class LazyAlias {

        private static final Map<String, Alias> ALIAS_MAP;

        static {
            Map<Integer, BuiltinType> builtinTypes = EnumSet.allOf(BuiltinType.class)
                    .stream().collect(Collectors.toMap(BuiltinType::type, Function.identity()));
            HashMap<String, Integer> nativeAliasMap = new HashMap<>(50);
            NativeMethods.getInstance().initAlias(nativeAliasMap);
            ALIAS_MAP = nativeAliasMap.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey,
                            entry -> new Alias(entry.getKey(), builtinTypes.get(entry.getValue()))));
        }

        static Alias find(String name) {
            Alias alias = ALIAS_MAP.get(name);
            if (alias == null) {
                throw new IllegalArgumentException("unsupported alias " + name);
            }
            return alias;
        }
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
