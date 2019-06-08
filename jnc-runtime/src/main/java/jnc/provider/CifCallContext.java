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
package jnc.provider;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import javax.annotation.Nullable;
import jnc.foreign.Struct;

/**
 * @author zhanhb
 */
final class CifCallContext implements CallContext {

    private static final NativeAccessor NA = NativeLoader.getAccessor();
    private final AllocatedMemory parameter;
    private List<Runnable> onFinish;
    private final int[] offsets;
    private final InternalType[] params;
    // must keep a strong reference to cif,
    // Maybe cif is gced before call finish
    // VariadicMethodInvocation.invoke won't keep a reference to CifContainer
    private final Struct cif;

    CifCallContext(int parameterSize, InternalType[] params,
            @Nullable int[] offsets, Struct cif) {
        this.parameter = AllocatedMemory.allocate(1, parameterSize);
        this.offsets = offsets;
        this.params = params;
        this.cif = cif;
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

    private void finish() {
        List<Runnable> finish = this.onFinish;
        if (finish != null) {
            for (int i = finish.size() - 1; i >= 0; --i) {
                finish.get(i).run();
            }
        }
    }

    @Override
    public <T> T invoke(InvokeHandler<T> handler, long function) {
        long result = NA.invoke(cif.getMemory().address(), function, parameter.address(),
                offsets, ThreadLocalError.INSTANCE, LastErrorHandler.METHOD_ID);
        finish();
        return handler.handle(result);
    }

}
