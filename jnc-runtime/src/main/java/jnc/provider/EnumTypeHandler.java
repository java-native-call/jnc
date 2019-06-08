package jnc.provider;

import java.util.EnumMap;
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
import jnc.foreign.exception.InvalidAnnotationException;
import jnc.foreign.exception.UnmappableNativeValueException;
import jnc.foreign.support.TypeHandler;

final class EnumTypeHandler<E extends Enum<E>> implements TypeHandler<E>, InvokeHandler<E> {

    private static final ConcurrentWeakIdentityHashMap<Class<? extends Enum<?>>, EnumTypeHandler<?>> cache
            = new ConcurrentWeakIdentityHashMap<>(32);

    private static final Map<NativeType, NativeType> TO_SIGNED_TYPE;
    private static final Map<NativeType, NativeType> TO_UNSIGNED_TYPE;

    // method annotationType is not implemented, got null if invoked
    // equals, hashCode, toString is same as class {@code Object} does
    private static final Continuously defaultContinuously
            = SimpleAnnotationBuilder.of(Continuously.class).build();

    static {
        EnumMap<NativeType, NativeType> toSigned = new EnumMap<>(NativeType.class);
        EnumMap<NativeType, NativeType> toUnsigned = new EnumMap<>(NativeType.class);
        add(toSigned, toUnsigned, UINT8, SINT8);
        add(toSigned, toUnsigned, UINT16, SINT16);
        add(toSigned, toUnsigned, UINT32, SINT32);
        add(toSigned, toUnsigned, UINT64, SINT64);
        TO_SIGNED_TYPE = toSigned;
        TO_UNSIGNED_TYPE = toUnsigned;
    }

    private static void add(Map<NativeType, NativeType> forward,
            Map<NativeType, NativeType> backward, NativeType key, NativeType value) {
        forward.put(key, value);
        backward.put(value, key);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public static <T extends Enum<T>> EnumTypeHandler<T> getInstance(Class<T> type) {
        // isEnum will check if the class is enum and not java.lang.Enum itself
        if (!type.isEnum()) {
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
        NativeType mapped = (start < 0 ? TO_SIGNED_TYPE : TO_UNSIGNED_TYPE).getOrDefault(nativeType, nativeType);
        InternalType internalType = DefaultForeign.INSTANCE.getTypeFactory().findByNativeType(mapped);
        if (!internalType.isIntegral()) {
            throw new InvalidAnnotationException(annotation, type, "Only integral type allowed");
        }
        T[] values = (T[]) type.getEnumConstants();
        int size = internalType.size();
        if (size < 8) {
            long max = 1L << (size << 3) - (internalType.isSigned() ? 1 : 0);
            if (start > max - values.length) {
                throw new InvalidAnnotationException(annotation, type, "start too large");
            }
        }
        return new EnumTypeHandler(values, type, internalType, start, onUnmappable);
    }

    private final E[] values;
    private final Class<E> type;
    private final InternalType defaultType;
    private final long start;
    private final EnumMappingErrorAction onUnmappable;

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

    @Override
    public E handle(long result) {
        return mapLong(result);
    }

    @Override
    public E get(Pointer memory, int offset) {
        return mapLong(memory.getLong(offset, defaultType));
    }

    @Override
    public void set(Pointer memory, int offset, @Nullable E value) {
        memory.putLong(offset, defaultType, value != null ? start + value.ordinal() : 0);
    }

    @Deprecated
    @Override
    public Type type() {
        return getDefaultType();
    }

}
