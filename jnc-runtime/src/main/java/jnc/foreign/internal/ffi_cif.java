package jnc.foreign.internal;

import jnc.foreign.Struct;
import jnc.foreign.enums.CallingConvention;

class ffi_cif {

    private final CifStruct cifStruct;
    private final int[] offsets;
    private final int parameterSize;
    private final int parameterAlign;

    ffi_cif(CallingConvention callingConvention, InternalType resultType, InternalType... params) {
        int count = params.length;
        int[] offs = new int[count];
        ParameterTemplate parameterTemplate = new ParameterTemplate(params, offs);
        CifStruct struct = new CifStruct(params);
        struct.prepareInvoke(callingConvention, resultType);
        this.cifStruct = struct;
        this.offsets = offs;
        this.parameterSize = parameterTemplate.size();
        this.parameterAlign = parameterTemplate.alignment();
    }

    CallContext newCallContext() {
        return new CallContext(parameterSize, parameterAlign, offsets, cifStruct);
    }

    long getCifAddress() {
        return cifStruct.getCif().getMemory().address();
    }

    private static class ParameterTemplate extends Struct {

        ParameterTemplate(InternalType[] params, int[] offs) {
            for (int i = 0; i < params.length; i++) {
                InternalType param = params[i];
                Struct.Enclosing enclosing = padding(param.size(), param.alignment()).getEnclosing();
                assert enclosing != null;
                offs[i] = enclosing.getOffset();
            }
        }
    }

}
