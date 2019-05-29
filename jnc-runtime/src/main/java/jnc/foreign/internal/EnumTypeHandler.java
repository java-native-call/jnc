package jnc.foreign.internal;

import java.text.MessageFormat;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;
import javax.annotation.Nullable;
import jnc.foreign.NativeType;
import static jnc.foreign.NativeType.SINT16;
import static jnc.foreign.NativeType.SINT32;
import static jnc.foreign.NativeType.SINT64;
import static jnc.foreign.NativeType.SINT8;
import static jnc.foreign.NativeType.UINT16;
import static jnc.foreign.NativeType.UINT32;
import static jnc.foreign.NativeType.UINT64;
import static jnc.foreign.NativeType.UINT8;
import jnc.foreign.Pointer;
import jnc.foreign.Type;
import jnc.foreign.annotation.Continuously;
import jnc.foreign.enums.EnumMappingErrorAction;
import jnc.foreign.exception.UnmappableNativeValueException;

class EnumTypeHandler<E extends Enum<E>> {

    private static final ConcurrentWeakIdentityHashMap<Class<? extends Enum<?>>, EnumTypeHandler<?>> cache
            = new ConcurrentWeakIdentityHashMap<>(32);

    private static final Map<NativeType, NativeType> signedMap;
    private static final Map<NativeType, NativeType> unsignedMap;

    // method annotationType is not implemented, got null if invoked
    // equals, hashCode, toString is same as class {@code Object} does
    private static final Continuously defaultContinuously = new ProxyBuilder()
            .useObjectMethods()
            .otherwise((proxy, method, args) -> method.getDefaultValue())
            .newInstance(Continuously.class);

    static {
        EnumMap<NativeType, NativeType> toSigned = new EnumMap<>(NativeType.class);
        EnumMap<NativeType, NativeType> toUnsigned = new EnumMap<>(NativeType.class);
        add(toSigned, toUnsigned, UINT8, SINT8);
        add(toSigned, toUnsigned, UINT8, SINT8);
        add(toSigned, toUnsigned, UINT16, SINT16);
        add(toSigned, toUnsigned, UINT32, SINT32);
        add(toSigned, toUnsigned, UINT64, SINT64);
        signedMap = toSigned;
        unsignedMap = toUnsigned;
    }

    private static void add(Map<NativeType, NativeType> forwarding,
            Map<NativeType, NativeType> reverse, NativeType key, NativeType value) {
        forwarding.put(key, value);
        reverse.put(value, key);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Enum<T>> EnumTypeHandler<T> getInstance(Class<T> type) {
        // Must do the cast, or will got COMPILATION ERROR: incomparable types
        //noinspection RedundantCast
        if (!type.isEnum() || type == (Class) Enum.class) {
            throw new IllegalArgumentException("Illegal type '" + type.getName() + "'");
        }
        return (EnumTypeHandler<T>) cache.computeIfAbsent(type, EnumTypeHandler::newInstance);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T extends Enum<T>> EnumTypeHandler<T> newInstance(Class<?> type) {
        @Nullable
        Continuously annotation = type.getAnnotation(Continuously.class);
        Continuously continuously = annotation != null ? annotation : defaultContinuously;
        long start = continuously.start();
        NativeType nativeType = continuously.type();
        EnumMappingErrorAction onUnmappable = continuously.onUnmappable();
        NativeType mapped = (start < 0 ? signedMap : unsignedMap).getOrDefault(nativeType, nativeType);
        InternalType internalType = TypeHelper.findByNativeType(mapped);
        if (!internalType.isIntegral()) {
            throw new IllegalArgumentException("Only integral type allowed on enum, but found "
                    + nativeType + " on " + type.getName());
        }
        T[] values = (T[]) type.getEnumConstants();
        int size = internalType.size();
        if (size < 8) {
            long max = 1L << (size << 3) - (internalType.isSigned() ? 1 : 0);
            if (start > max - values.length) {
                throw new IllegalArgumentException(
                        MessageFormat.format("start too large, start={0}, max={2}, {0}+{1}>{2}", start, values.length, max));
            }
        }
        return new EnumTypeHandler(values, type, internalType, start, onUnmappable);
    }

    private final E[] values;
    private final Class<E> type;
    private final InternalType defaultType;
    private final long start;
    private final EnumMappingErrorAction onUnmappable;
    private FieldAccessor fieldAccessor;

    private EnumTypeHandler(E[] values, Class<E> type, InternalType defaultType, long start,
            EnumMappingErrorAction onUnmappable) {
        this.values = values;
        this.type = type;
        this.defaultType = defaultType;
        this.start = start;
        this.onUnmappable = onUnmappable;
    }

    InternalType getDefaultType() {
        return defaultType;
    }

    ParameterHandler<E> getParameterHandler() {
        return (CallContext context, int index, E obj) -> context.putLong(index, obj != null ? start + obj.ordinal() : 0);
    }

    @Nullable
    private E mapLong(long v) {
        long index = v - start;
        if (0 <= index && index < values.length) {
            return values[(int) index];
        }
        if (onUnmappable == EnumMappingErrorAction.SET_TO_NULL
                || v == 0 && onUnmappable == EnumMappingErrorAction.NULL_WHEN_ZERO) {
            return null;
        }
        throw new UnmappableNativeValueException(type, v);
    }

    Invoker<E> getInvoker() {
        return (long cif, long function, long base, @Nullable int[] offsets) -> mapLong(Invokers.invokeLong(cif, function, base, offsets));
    }

    FieldAccessor getFieldAccessor() {
        FieldAccessor fa = this.fieldAccessor;
        if (fa == null) {
            fa = new FieldAccessor();
            this.fieldAccessor = fa;
        }
        return fa;
    }

    private class FieldAccessor implements jnc.foreign.FieldAccessor<E> {

        @Override
        public Type type() {
            return defaultType;
        }

        @Override
        public E get(Pointer memory, int offset) {
            return mapLong(memory.getLong(offset, defaultType));
        }

        @Override
        public void set(Pointer memory, int offset, E value) {
            memory.putLong(offset, defaultType, value != null ? start + value.ordinal() : 0);
        }
    }

}
