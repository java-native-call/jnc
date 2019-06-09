package jnc.provider;

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import javax.annotation.Nullable;
import jnc.foreign.NativeType;
import static jnc.foreign.NativeType.*;
import jnc.foreign.enums.TypeAlias;
import static jnc.provider.BuiltinType.MASK_FLOATING;
import static jnc.provider.BuiltinType.MASK_INTEGRAL;
import static jnc.provider.BuiltinType.MASK_SIGNED;
import static jnc.provider.NativeAccessor.*;

final class TypeRegistry implements TypeFactory {

    private static void add(long[][] types, NativeType nativeType,
            @Nullable Class<?> primaryClass, int type, int attr,
            Map<Integer, BuiltinType> builtinTypes,
            Map<NativeType, BuiltinType> nativeTypeMap,
            Map<Class<?>, BuiltinType> primitiveMap) {
        long[] arr = types[type];
        long address = arr[0];
        long info = arr[1];

        BuiltinType builtinType = new BuiltinType(address, info, nativeType, attr);
        nativeTypeMap.put(nativeType, builtinType);
        builtinTypes.put(type, builtinType);
        if (primaryClass != null) {
            primitiveMap.put(primaryClass, builtinType);
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

    private final Map<NativeType, BuiltinType> NATIVE_TO_TYPE;
    private final Map<Class<?>, BuiltinType> PRIMITIVE_MAP;
    private final Map<TypeAlias, Alias> ALIAS_MAP;

    TypeRegistry() {
        NativeAccessor accessor = NativeLoader.getAccessor();
        long[][] types = accessor.getTypes();
        Map<Integer, BuiltinType> builtinTypes = new HashMap<>(types.length * 3 / 4);
        EnumMap<NativeType, BuiltinType> nativeToType = new EnumMap<>(NativeType.class);
        HashMap<Class<?>, BuiltinType> primitiveMap = new HashMap<>(16);

        add(types, VOID, void.class, TYPE_VOID, 0, builtinTypes, nativeToType, primitiveMap);
        add(types, FLOAT, float.class, TYPE_FLOAT, MASK_SIGNED | MASK_FLOATING, builtinTypes, nativeToType, primitiveMap);
        add(types, DOUBLE, double.class, TYPE_DOUBLE, MASK_SIGNED | MASK_FLOATING, builtinTypes, nativeToType, primitiveMap);
        add(types, UINT8, null, TYPE_UINT8, MASK_INTEGRAL, builtinTypes, nativeToType, primitiveMap);
        add(types, SINT8, byte.class, TYPE_SINT8, MASK_INTEGRAL | MASK_SIGNED, builtinTypes, nativeToType, primitiveMap);
        add(types, UINT16, char.class, TYPE_UINT16, MASK_INTEGRAL, builtinTypes, nativeToType, primitiveMap);
        add(types, SINT16, short.class, TYPE_SINT16, MASK_INTEGRAL | MASK_SIGNED, builtinTypes, nativeToType, primitiveMap);
        add(types, UINT32, null, TYPE_UINT32, MASK_INTEGRAL, builtinTypes, nativeToType, primitiveMap);
        add(types, SINT32, int.class, TYPE_SINT32, MASK_INTEGRAL | MASK_SIGNED, builtinTypes, nativeToType, primitiveMap);
        add(types, UINT64, null, TYPE_UINT64, MASK_INTEGRAL, builtinTypes, nativeToType, primitiveMap);
        add(types, SINT64, long.class, TYPE_SINT64, MASK_INTEGRAL | MASK_SIGNED, builtinTypes, nativeToType, primitiveMap);
        add(types, POINTER, null, TYPE_POINTER, 0, builtinTypes, nativeToType, primitiveMap);

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
                // if builtinTypes.get(type) returns null, means method getTypes and initAlias not match
                BuiltinType builtinType = Objects.requireNonNull(builtinTypes.get(type), "native type and alias mismatch");
                aliasMap.put(typeAlias, new Alias(typeAlias, builtinType));
            }
        }

        NATIVE_TO_TYPE = Collections.unmodifiableMap(nativeToType);
        PRIMITIVE_MAP = Collections.unmodifiableMap(primitiveMap);
        ALIAS_MAP = Collections.unmodifiableMap(aliasMap);
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
