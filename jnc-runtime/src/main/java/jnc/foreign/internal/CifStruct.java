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
 *
 * @author zhanhb
 */
final class CifStruct extends Struct {

    private static final NativeMethods nm = NativeMethods.getInstance();

    private static final long CIF_INFO = nm.getCifInfo();
    private static final int SIZE_OF_FFI_CIF = (int) CIF_INFO;
    private static final int ALIGN_OF_FFI_CIF = (int) (CIF_INFO >> 32);
    private static final Address[] EMPTY = {};

    private static int convention(CallingConvention callingConvention) {
        if (callingConvention == CallingConvention.STDCALL) {
            return NativeMethods.CONVENTION_STDCALL;
        }
        return NativeMethods.CONVENTION_DEFAULT;
    }

    private final Address[] atypes;
    private final Struct cif;

    CifStruct(InternalType... params) {
        int length = params.length;
        if (length > 0) {
            atypes = array(new Address[length]);
        } else {
            atypes = EMPTY;
        }
        cif = padding(SIZE_OF_FFI_CIF, ALIGN_OF_FFI_CIF);
        for (int i = 0; i < length; ++i) {
            atypes[i].set(params[i].address());
        }
    }

    public Struct getCif() {
        return cif;
    }

    void prepareInvoke(CallingConvention callingConvention, InternalType resultType) {
        nm.prepareInvoke(cif.getMemory().address(), convention(callingConvention), atypes.length, resultType.address(), getMemory().address());
    }

    long get(int i) {
        return atypes[i].get();
    }

    void putInt(Pointer parameter, int i, int value) {
    }

}
