package jnc.foreign.internal;

import java.util.EnumSet;
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
import jnc.foreign.annotation.Continuously;
import jnc.foreign.enums.EnumMappingErrorAction;
import jnc.foreign.exception.UnmappableNativeValueException;

class EnumTypeHandler<E extends Enum<E>> implements InternalTypeHandler<E> {

    private static final EnumSet<NativeType> ALLOWED_NATIVE_TYPES = EnumSet.of(
            UINT8, UINT16, UINT32, UINT64,
            SINT8, SINT16, SINT32, SINT64
    );

    static <T extends Enum<T>> EnumTypeHandler<T> newInstance(Class<T> type, AnnotationContext ac) {
        NativeType nativeType = NativeType.UINT32;
        int start = 0;
        EnumMappingErrorAction onUnmappable = EnumMappingErrorAction.NULL_WHEN_ZERO;
        Continuously annotation = ac.getAnnotation(Continuously.class);
        if (annotation != null) {
            nativeType = annotation.type();
            start = annotation.start();
            onUnmappable = annotation.onUnmappable();
        }
        if (!ALLOWED_NATIVE_TYPES.contains(nativeType)) {
            throw new IllegalStateException("Only integral type allowed on enum, but found "
                    + nativeType + " on " + type.getName());
        }
        InternalType internalType = TypeHelper.findByNativeType(nativeType);
        return new EnumTypeHandler<>(type, nativeType, internalType, start, onUnmappable);
    }

    private final E[] values;
    private final Class<E> type;
    private final NativeType nativeType;
    private final InternalType defaultType;
    private final int start;
    private final EnumMappingErrorAction onUnmappable;
    private FieldAccessor fieldAccessor;

    private EnumTypeHandler(Class<E> type, NativeType nativeType, InternalType defaultType, int start,
            EnumMappingErrorAction onUnmappable) {
        this.values = type.getEnumConstants();
        this.type = type;
        this.nativeType = nativeType;
        this.defaultType = defaultType;
        this.start = start;
        this.onUnmappable = onUnmappable;
    }

    @Override
    public InternalType getDefaultType() {
        return defaultType;
    }

    @Override
    public NativeType nativeType() {
        return nativeType;
    }

    @Override
    public ParameterHandler<E> getParameterHandler() {
        return (CallContext context, int index, E obj) -> {
            context.putInt(index, obj != null ? start + obj.ordinal() : 0);
        };
    }

    @Nullable
    private E mapInt(int intVal) {
        int index = intVal - start;
        if (0 <= index && index < values.length) {
            return values[index];
        }
        if (onUnmappable == EnumMappingErrorAction.SET_TO_NULL
                || intVal == 0 && onUnmappable == EnumMappingErrorAction.NULL_WHEN_ZERO) {
            return null;
        }
        throw new UnmappableNativeValueException(type, intVal);
    }

    @Override
    public Invoker<E> getInvoker() {
        return (long cif, long function, long avalues) -> mapInt(Invokers.invokeInt(cif, function, avalues));
    }

    @Override
    public FieldAccessor getFieldAccessor() {
        FieldAccessor fa = this.fieldAccessor;
        if (fa == null) {
            fa = new FieldAccessor();
            this.fieldAccessor = fa;
        }
        return fa;
    }

    private class FieldAccessor implements jnc.foreign.FieldAccessor<E> {

        @Override
        public E get(Pointer memory, int offset) {
            return mapInt(memory.getInt(offset, defaultType));
        }

        @Override
        public void set(Pointer memory, int offset, E value) {
            memory.putInt(offset, defaultType, value != null ? start + value.ordinal() : 0);
        }
    }

}
