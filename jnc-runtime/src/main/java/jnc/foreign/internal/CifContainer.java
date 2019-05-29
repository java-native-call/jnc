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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import jnc.foreign.Struct;
import jnc.foreign.enums.CallingConvention;

/**
 * @author zhanhb
 */
final class CifContainer extends Struct {

    private static final NativeMethods nm = NativeMethods.getInstance();

    private static final long CIF_INFO = nm.getCifInfo();
    private static final int SIZE_OF_FFI_CIF = (int) CIF_INFO;
    private static final int ALIGN_OF_FFI_CIF = (int) (CIF_INFO >> 32);
    private static final Address[] ARGUMENT_EMPTY = {};

    static CifContainer create(CallingConvention callingConvention, InternalType resultType, InternalType... params) {
        return new CifContainer(params).prepareInvoke(callingConvention, resultType);
    }

    private static int convention(CallingConvention callingConvention) {
        if (callingConvention == CallingConvention.STDCALL) {
            return NativeMethods.CONVENTION_STDCALL;
        }
        return NativeMethods.CONVENTION_DEFAULT;
    }

    private final InternalType[] params;
    private final Struct cif;
    private final int[] offsets;
    private final int parameterSize;

    CifContainer(InternalType... params) {
        int length = params.length;
        Address[] atypes;
        if (length != 0) {
            atypes = array(new Address[length]);
        } else {
            atypes = ARGUMENT_EMPTY;
        }
        // cif must be constructed after atypes,
        // and value setter of atypes must after all parameter constructed
        this.cif = padding(SIZE_OF_FFI_CIF, ALIGN_OF_FFI_CIF);

        for (int i = 0; i < length; ++i) {
            atypes[i].set(params[i].address());
        }

        // TODO struct layout is expected here.
        int[] offsets = new int[length];
        ParameterTemplate parameterTemplate = new ParameterTemplate(params, offsets);
        this.params = params;
        this.offsets = offsets;
        this.parameterSize = parameterTemplate.size();
    }

    CifContainer prepareInvoke(CallingConvention callingConvention, InternalType resultType) {
        nm.prepareInvoke(cif.getMemory().address(), convention(callingConvention), params.length, resultType.address(), getMemory().address());
        return this;
    }

    CallContext newCallContext() {
        return new CifCallContext();
    }

    long getCifAddress() {
        return cif.getMemory().address();
    }

    private static class ParameterTemplate extends Struct {

        ParameterTemplate(InternalType[] params, int[] offsets) {
            for (int i = 0; i < params.length; i++) {
                InternalType param = params[i];
                Struct.Enclosing enclosing = padding(param.size(), param.alignment()).getEnclosing();
                assert enclosing != null;
                offsets[i] = enclosing.getOffset();
            }
        }
    }

    private final class CifCallContext implements CallContext {

        private final AllocatedMemory parameter;
        private List<Runnable> onFinish;

        CifCallContext() {
            this.parameter = AllocatedMemory.allocate(1, parameterSize);
        }

        @Override
        public void putInt(int i, int value) {
            parameter.putInt(offsets[i], params[i], value);
        }

        @Override
        public void putLong(int i, long value) {
            parameter.putLong(offsets[i], params[i], value);
        }

        @Override
        public void putFloat(int i, float value) {
            parameter.putFloat(offsets[i], params[i], value);
        }

        @Override
        public void putDouble(int i, double value) {
            parameter.putDouble(offsets[i], params[i], value);
        }

        @Override
        public CifCallContext onFinish(Runnable r) {
            Objects.requireNonNull(r);
            List<Runnable> finish = this.onFinish;
            if (finish == null) {
                finish = new ArrayList<>(offsets.length);
                this.onFinish = finish;
            }
            finish.add(r);
            return this;
        }

        public void finish() {
            List<Runnable> finish = this.onFinish;
            if (finish != null) {
                Collections.reverse(finish);
                for (Runnable runnable : finish) {
                    runnable.run();
                }
            }
        }

        @Override
        public <T> T invoke(Invoker<T> invoker, long function) {
            T result = invoker.invoke(CifContainer.this.getCifAddress(), function, parameter.address(), CifContainer.this.offsets);
            finish();
            return result;
        }
    }

}
