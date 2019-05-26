package jnc.foreign.internal;

import jnc.foreign.abi.CallingMode;

class ffi_cif implements NativeObject {

    private static final NativeMethods nm = NativeMethods.getInstance();
    private static final int SIZE_OF_FFI_CIF = nm.sizeof_ffi_cif();

    private static int convention(CallingMode callingMode) {
        if (callingMode == CallingMode.STDCALL) {
            return NativeMethods.CONVENTION_STDCALL;
        }
        return NativeMethods.CONVENTION_DEFAULT;
    }

    private final Memory ffi_cif;
    private final PointerArray argumentTypes;
    private final long base;
    private final long[] offsets;
    private final int parameterSize;
    private final int parameterAlign;

    ffi_cif(CallingMode callingMode, InternalType resultType, InternalType... params) {
        int count = params.length;
        int size = 0;
        int pointerSize = TypeHelper.TYPE_POINTER.size();
        int alignment = pointerSize;
        long[] offs = new long[count];
        for (int i = 0; i < count; ++i) {
            InternalType param = params[i];
            int align = Math.max(param.alignment(), pointerSize);
            alignment = Math.max(align, alignment);
            size = Aligns.alignUp(size, align);
            offs[i] = size;
            size += param.size();
        }
        size = Aligns.alignUp(size, alignment);
        Memory cif = AllocatedMemory.allocate(1, SIZE_OF_FFI_CIF);
        PointerArray atypes = PointerArray.allocate(params);
        int offset = Aligns.alignUp(count * pointerSize, alignment);
        nm.prepareInvoke(cif.address(), convention(callingMode), count, resultType.address(), atypes.address());
        this.offsets = offs;
        this.ffi_cif = cif;
        this.argumentTypes = atypes;
        this.parameterSize = offset + size;
        this.parameterAlign = alignment;
        this.base = offset;
    }

    CallContext newCallContext() {
        return new CallContext(parameterSize, parameterAlign, base, offsets, argumentTypes);
    }

    @Override
    public long address() {
        return ffi_cif.address();
    }

}
