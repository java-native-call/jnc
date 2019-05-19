package jnc.foreign.internal;

import java.lang.reflect.InvocationTargetException;
import jnc.foreign.NativeType;
import jnc.foreign.Pointer;
import jnc.foreign.annotation.ContinuouslyEnum;
import jnc.foreign.annotation.EnumMappingErrorAction;

class EnumTypeHandler<E extends Enum<E>> implements InternalTypeHandler<E> {

    static <T extends Enum<T>> EnumTypeHandler<T> newInstance(Class<T> type, ContinuouslyEnum annotation) {
        NativeType nativeType = NativeType.UINT32;
        int start = 0;
        EnumMappingErrorAction onZero = EnumMappingErrorAction.SET_TO_NULL;
        EnumMappingErrorAction onUnmappable = EnumMappingErrorAction.REPORT;
        if (annotation != null) {
            nativeType = annotation.type();
            start = annotation.start();
            onZero = annotation.onZero();
            onUnmappable = annotation.onUnmappable();
        }
        BuiltinType builtinType = BuiltinTypeHelper.findByNativeType(nativeType);
        return new EnumTypeHandler<>(type, builtinType, start, onZero, onUnmappable);
    }

    private final E[] values;
    private final Class<E> type;
    private final BuiltinType builtinType;
    private final int start;
    private final int end;
    private final EnumMappingErrorAction onZero;
    private final EnumMappingErrorAction onUnmappable;

    @SuppressWarnings("unchecked")
    private EnumTypeHandler(Class<E> type, BuiltinType builtinType, int start,
            EnumMappingErrorAction onZero, EnumMappingErrorAction onUnmappable) {
        if (builtinType != BuiltinType.SINT32 && builtinType != BuiltinType.UINT32) {
            throw new UnsupportedOperationException("Not supported yet.");
        }
        try {
            values = (E[]) type.getMethod("values").invoke(null);
        } catch (NoSuchMethodException | IllegalAccessException | IllegalArgumentException ex) {
            throw new AssertionError("class '" + type + "' is not an enum");
        } catch (InvocationTargetException ex) {
            Throwable cause = ex.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw new AssertionError(cause);
        }
        this.type = type;
        this.builtinType = builtinType;
        this.start = start;
        end = start + values.length;
        this.onZero = onZero;
        this.onUnmappable = onUnmappable;
    }

    @Deprecated
    @Override
    public BuiltinType getBuiltinType() {
        return builtinType;
    }

    @Override
    public NativeType nativeType() {
        return builtinType.getNativeType();
    }

    @Override
    public E get(Pointer memory, int offset) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void set(Pointer memory, int offset, E e) {
        memory.putInt(offset, builtinType, e != null ? start + e.ordinal() : 0);
    }

    @Override
    public Invoker getInvoker() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ParameterHandler<E> getParameterHandler() {
        return (CallContext context, int index, E obj) -> {
            context.putInt(index, obj != null ? start + obj.ordinal() : 0);
        };
    }

}
