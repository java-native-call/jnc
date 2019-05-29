package jnc.foreign.internal;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import jnc.foreign.NativeType;
import static jnc.foreign.NativeType.ADDRESS;
import static jnc.foreign.NativeType.DOUBLE;
import static jnc.foreign.NativeType.FLOAT;
import static jnc.foreign.NativeType.SINT16;
import static jnc.foreign.NativeType.SINT32;
import static jnc.foreign.NativeType.SINT64;
import static jnc.foreign.NativeType.SINT8;
import static jnc.foreign.NativeType.UINT16;
import static jnc.foreign.NativeType.UINT32;
import static jnc.foreign.NativeType.UINT64;
import static jnc.foreign.NativeType.UINT8;
import static jnc.foreign.NativeType.VOID;
import jnc.foreign.enums.TypeAlias;
import static jnc.foreign.internal.NativeMethods.TYPE_DOUBLE;
import static jnc.foreign.internal.NativeMethods.TYPE_FLOAT;
import static jnc.foreign.internal.NativeMethods.TYPE_POINTER;
import static jnc.foreign.internal.NativeMethods.TYPE_SINT16;
import static jnc.foreign.internal.NativeMethods.TYPE_SINT32;
import static jnc.foreign.internal.NativeMethods.TYPE_SINT64;
import static jnc.foreign.internal.NativeMethods.TYPE_SINT8;
import static jnc.foreign.internal.NativeMethods.TYPE_UINT16;
import static jnc.foreign.internal.NativeMethods.TYPE_UINT32;
import static jnc.foreign.internal.NativeMethods.TYPE_UINT64;
import static jnc.foreign.internal.NativeMethods.TYPE_UINT8;
import static jnc.foreign.internal.NativeMethods.TYPE_VOID;
import static jnc.foreign.internal.TypeInfo.MASK_FLOATING;
import static jnc.foreign.internal.TypeInfo.MASK_INTEGRAL;
import static jnc.foreign.internal.TypeInfo.MASK_SIGNED;

@SuppressWarnings("UtilityClassWithoutPrivateConstructor")
class TypeHelper {

    private static final EnumMap<NativeType, TypeInfo> MAP;
    private static final Map<Integer, TypeInfo> TYPE_INFOS;
    private static final HashMap<Class<?>, TypeInfo> PRIMITIVE_MAP;

    static final TypeInfo TYPE_INFO_POINTER;

    static {
        long[][] types = NativeMethods.getInstance().getTypes();
        Map<Integer, TypeInfo> typeInfos = new HashMap<>(types.length * 3 / 4);
        EnumMap<NativeType, TypeInfo> map = new EnumMap<>(NativeType.class);
        HashMap<Class<?>, TypeInfo> primitiveMap = new HashMap<>(16);

        add(types, VOID, void.class, TYPE_VOID, 0, typeInfos, map, primitiveMap);
        add(types, FLOAT, float.class, TYPE_FLOAT, MASK_SIGNED | MASK_FLOATING, typeInfos, map, primitiveMap);
        add(types, DOUBLE, double.class, TYPE_DOUBLE, MASK_SIGNED | MASK_FLOATING, typeInfos, map, primitiveMap);
        add(types, UINT8, null, TYPE_UINT8, MASK_INTEGRAL, typeInfos, map, primitiveMap);
        add(types, SINT8, byte.class, TYPE_SINT8, MASK_INTEGRAL | MASK_SIGNED, typeInfos, map, primitiveMap);
        add(types, UINT16, char.class, TYPE_UINT16, MASK_INTEGRAL, typeInfos, map, primitiveMap);
        add(types, SINT16, short.class, TYPE_SINT16, MASK_INTEGRAL | MASK_SIGNED, typeInfos, map, primitiveMap);
        add(types, UINT32, null, TYPE_UINT32, MASK_INTEGRAL, typeInfos, map, primitiveMap);
        add(types, SINT32, int.class, TYPE_SINT32, MASK_INTEGRAL | MASK_SIGNED, typeInfos, map, primitiveMap);
        add(types, UINT64, null, TYPE_UINT64, MASK_INTEGRAL, typeInfos, map, primitiveMap);
        add(types, SINT64, long.class, TYPE_SINT64, MASK_INTEGRAL | MASK_SIGNED, typeInfos, map, primitiveMap);
        add(types, ADDRESS, null, TYPE_POINTER, MASK_INTEGRAL, typeInfos, map, primitiveMap);

        MAP = map;
        TYPE_INFOS = typeInfos;
        PRIMITIVE_MAP = primitiveMap;

        TYPE_INFO_POINTER = map.get(NativeType.ADDRESS);
    }

    static Alias findByAlias(TypeAlias typeAlias) {
        Objects.requireNonNull(typeAlias, "type alias");
        return AliasMapHolder.find(typeAlias);
    }

    private static void add(long[][] types, NativeType nativeType,
            @Nullable Class<?> primaryClass, int type, int attr,
            Map<Integer, TypeInfo> typeInfos,
            Map<NativeType, TypeInfo> nativeTypeMap,
            Map<Class<?>, TypeInfo> primitiveMap) {
        long[] arr = types[type];
        long address = arr[0];
        long info = arr[1];

        TypeInfo typeInfo = new TypeInfo(address, info, nativeType, attr);
        nativeTypeMap.put(nativeType, typeInfo);
        typeInfos.put(type, typeInfo);
        if (primaryClass != null) {
            primitiveMap.put(primaryClass, typeInfo);
        }
    }

    private static TypeInfo findByType(int type) {
        TypeInfo ti = TYPE_INFOS.get(type);
        if (ti == null) {
            throw new IllegalArgumentException("unsupported type " + type);
        }
        return ti;
    }

    static InternalType findByNativeType(NativeType nativeType) {
        Objects.requireNonNull(nativeType, "native type");
        InternalType internalType = MAP.get(nativeType);
        if (internalType == null) {
            throw new IllegalArgumentException("unsupported native type " + nativeType);
        }
        return internalType;
    }

    static InternalType findByPrimaryType(Class<?> type) {
        return PRIMITIVE_MAP.get(Primitives.unwrap(type));
    }

    private static class AliasMapHolder {

        private static final Map<TypeAlias, Alias> MAP;

        static {
            HashMap<String, Integer> nativeAliasMap = new HashMap<>(50);
            NativeMethods.getInstance().initAlias(nativeAliasMap);
            EnumMap<TypeAlias, Alias> map = new EnumMap<>(TypeAlias.class);
            for (Map.Entry<String, Integer> entry : nativeAliasMap.entrySet()) {
                String key = entry.getKey();
                int type = entry.getValue();
                TypeAlias typeAlias = toTypeAlias(key);
                if (typeAlias != null) {
                    TypeInfo typeInfo = TypeHelper.findByType(type);
                    map.put(typeAlias, new Alias(typeAlias, typeInfo));
                }
            }
            MAP = map;
        }

        private static TypeAlias toTypeAlias(String name) {
            try {
                return TypeAlias.valueOf(name);
            } catch (IllegalArgumentException ex) {
                // ok might be int,long
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

}
