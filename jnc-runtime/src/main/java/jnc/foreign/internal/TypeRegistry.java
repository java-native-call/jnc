package jnc.foreign.internal;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import jnc.foreign.NativeType;
import static jnc.foreign.NativeType.*;
import jnc.foreign.enums.TypeAlias;
import static jnc.foreign.internal.NativeAccessor.*;
import static jnc.foreign.internal.TypeInfo.MASK_FLOATING;
import static jnc.foreign.internal.TypeInfo.MASK_INTEGRAL;
import static jnc.foreign.internal.TypeInfo.MASK_SIGNED;

class TypeRegistry implements TypeFactory {

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

    private final EnumMap<NativeType, TypeInfo> NATIVE_TO_TYPE;
    private final HashMap<Class<?>, TypeInfo> PRIMITIVE_MAP;
    private final EnumMap<TypeAlias, Alias> ALIAS_MAP;

    TypeRegistry() {
        NativeAccessor accessor = NativeLoader.getAccessor();
        long[][] types = accessor.getTypes();
        Map<Integer, TypeInfo> typeInfos = new HashMap<>(types.length * 3 / 4);
        EnumMap<NativeType, TypeInfo> nativeToType = new EnumMap<>(NativeType.class);
        HashMap<Class<?>, TypeInfo> primitiveMap = new HashMap<>(16);

        add(types, VOID, void.class, TYPE_VOID, 0, typeInfos, nativeToType, primitiveMap);
        add(types, FLOAT, float.class, TYPE_FLOAT, MASK_SIGNED | MASK_FLOATING, typeInfos, nativeToType, primitiveMap);
        add(types, DOUBLE, double.class, TYPE_DOUBLE, MASK_SIGNED | MASK_FLOATING, typeInfos, nativeToType, primitiveMap);
        add(types, UINT8, null, TYPE_UINT8, MASK_INTEGRAL, typeInfos, nativeToType, primitiveMap);
        add(types, SINT8, byte.class, TYPE_SINT8, MASK_INTEGRAL | MASK_SIGNED, typeInfos, nativeToType, primitiveMap);
        add(types, UINT16, char.class, TYPE_UINT16, MASK_INTEGRAL, typeInfos, nativeToType, primitiveMap);
        add(types, SINT16, short.class, TYPE_SINT16, MASK_INTEGRAL | MASK_SIGNED, typeInfos, nativeToType, primitiveMap);
        add(types, UINT32, null, TYPE_UINT32, MASK_INTEGRAL, typeInfos, nativeToType, primitiveMap);
        add(types, SINT32, int.class, TYPE_SINT32, MASK_INTEGRAL | MASK_SIGNED, typeInfos, nativeToType, primitiveMap);
        add(types, UINT64, null, TYPE_UINT64, MASK_INTEGRAL, typeInfos, nativeToType, primitiveMap);
        add(types, SINT64, long.class, TYPE_SINT64, MASK_INTEGRAL | MASK_SIGNED, typeInfos, nativeToType, primitiveMap);
        add(types, POINTER, null, TYPE_POINTER, 0, typeInfos, nativeToType, primitiveMap);

        HashMap<String, Integer> aliasToId = new HashMap<>(50);
        accessor.initAlias(aliasToId);
        EnumMap<TypeAlias, Alias> aliasMap = new EnumMap<>(TypeAlias.class);
        for (Map.Entry<String, Integer> entry : aliasToId.entrySet()) {
            String key = entry.getKey();
            int type = entry.getValue();
            TypeAlias typeAlias = toTypeAlias(key);
            // if type alias is null, means native lib defined an alias,
            // but not exists in the enum, maybe native lib have higher version than java's
            if (typeAlias != null) {
                // if typeInfos.get(type) returns null, means method getTypes and initAlias not match
                TypeInfo typeInfo = Objects.requireNonNull(typeInfos.get(type), "native type and alias mismatch");
                aliasMap.put(typeAlias, new Alias(typeAlias, typeInfo));
            }
        }

        NATIVE_TO_TYPE = nativeToType;
        PRIMITIVE_MAP = primitiveMap;
        ALIAS_MAP = aliasMap;
    }

    @Override
    public Alias findByAlias(TypeAlias typeAlias) {
        Objects.requireNonNull(typeAlias, "type alias");
        Alias alias = ALIAS_MAP.get(typeAlias);
        if (alias == null) {
            throw new UnsupportedOperationException("type " + typeAlias + " is not supported on current platform");
        }
        return alias;
    }

    @Override
    public InternalType findByNativeType(NativeType nativeType) {
        Objects.requireNonNull(nativeType, "native type");
        InternalType internalType = NATIVE_TO_TYPE.get(nativeType);
        if (internalType == null) {
            throw new IllegalArgumentException("unsupported native type " + nativeType);
        }
        return internalType;
    }

    @Override
    public InternalType findByPrimaryType(Class<?> type) {
        return PRIMITIVE_MAP.get(Primitives.unwrap(type));
    }

}
