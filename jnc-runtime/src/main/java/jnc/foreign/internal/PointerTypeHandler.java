package jnc.foreign.internal;

import jnc.foreign.NativeType;
import jnc.foreign.Pointer;

enum PointerTypeHandler implements InternalTypeHandler<Pointer> {

    INSTANCE;

    @SuppressWarnings("unchecked")
    public static <T> InternalTypeHandler<T> getInstance() {
        return (InternalTypeHandler<T>) INSTANCE;
    }

    @Override
    public Invoker<Pointer> getInvoker() {
        return (long cif, long function, long avalues)
                -> DirectMemory.of(NativeMethods.getInstance().invokeLong(cif, function, avalues, ThreadLocalError.getInstance(), LastErrorHandler.METHOD_ID));
    }

    @Override
    public ParameterHandler<Pointer> getParameterHandler() {
        return (context, index, obj) -> context.putLong(index, obj != null ? obj.address() : 0);
    }

    @Override
    public NativeType nativeType() {
        return NativeType.ADDRESS;
    }

    @Override
    public BuiltinType getBuiltinType() {
        return BuiltinType.POINTER;
    }

    @Override
    public Pointer get(Pointer memory, int offset) {
        return memory.getPointer(offset);
    }

    @Override
    public void set(Pointer memory, int offset, Pointer value) {
        memory.putPointer(offset, value);
    }

}
