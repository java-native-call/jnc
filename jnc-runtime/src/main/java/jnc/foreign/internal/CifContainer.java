/*
 * Copyright 2019 zhanhb.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jnc.foreign.internal;

import jnc.foreign.Struct;
import jnc.foreign.enums.CallingConvention;

/**
 * @author zhanhb
 */
final class CifContainer extends Struct {

    private static final NativeAccessor NA = NativeLoader.getAccessor();

    private static final long CIF_INFO = NA.getCifInfo();
    private static final int SIZE_OF_FFI_CIF = (int) CIF_INFO;
    private static final int ALIGN_OF_FFI_CIF = (int) (CIF_INFO >> 32);
    private static final InternalType[] TYPE_EMPTY = {};
    private static final Address[] ADDRESS_EMPTY = {};

    private static int convention(CallingConvention callingConvention) {
        if (callingConvention == CallingConvention.STDCALL) {
            return NativeAccessor.CONVENTION_STDCALL;
        }
        return NativeAccessor.CONVENTION_DEFAULT;
    }

    static CifContainer create(
            CallingConvention convention,
            InternalType resultType, InternalType... params) {
        return new CifContainer(params).prepareInvoke(convention, resultType);
    }

    static CifContainer createVariadic(
            CallingConvention convention, int fixedArgs,
            InternalType resultType, InternalType[] params) {
        return new CifContainer(params).prepareInvokeVariadic(convention, fixedArgs, resultType);
    }

    private final InternalType[] params;
    private final Struct cif;
    private final int[] offsets;
    private final int parameterSize;

    private CifContainer(InternalType[] params) {
        int length = params.length;
        Address[] atypes;
        if (length != 0) {
            atypes = array(new Address[length]);
        } else {
            atypes = ADDRESS_EMPTY;
        }
        // cif must be constructed after atypes,
        // and value setter of atypes must after all parameter constructed
        this.cif = padding(SIZE_OF_FFI_CIF, ALIGN_OF_FFI_CIF);

        for (int i = 0; i < length; ++i) {
            atypes[i].set(params[i].address());
        }

        if (length != 0) {
            int[] offsets = new int[length];
            LayoutBuilder builder = LayoutBuilder.withoutPack(LayoutBuilder.Type.STRUCT);
            for (int i = 0; i < length; ++i) {
                InternalType param = params[i];
                offsets[i] = builder.addField(param.size(), param.alignment());
            }
            this.params = params;
            this.offsets = offsets;
            this.parameterSize = builder.size();
        } else {
            this.params = TYPE_EMPTY;
            this.offsets = null;
            this.parameterSize = 0;
        }
    }

    private CifContainer prepareInvoke(CallingConvention convention, InternalType resultType) {
        NA.prepareInvoke(cif.getMemory().address(), convention(convention),
                params.length, resultType.address(), getMemory().address());
        return this;
    }

    private CifContainer prepareInvokeVariadic(CallingConvention convention,
            int fixedArgs, InternalType resultType) {
        NA.prepareInvokeVariadic(cif.getMemory().address(), convention(convention),
                fixedArgs, params.length, resultType.address(), getMemory().address());
        return this;
    }

    CallContext newCallContext() {
        return new CifCallContext(parameterSize, params, offsets, cif);
    }

}
