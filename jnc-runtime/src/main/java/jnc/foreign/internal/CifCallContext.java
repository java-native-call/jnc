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
import javax.annotation.Nullable;

/**
 * @author zhanhb
 */
final class CifCallContext implements CallContext {

    private final AllocatedMemory parameter;
    private List<Runnable> onFinish;
    private final int[] offsets;
    private final InternalType[] params;
    private final long cifAddress;

    CifCallContext(int parameterSize, InternalType[] params,
            @Nullable int[] offsets, long cifAddress) {
        this.parameter = AllocatedMemory.allocate(1, parameterSize);
        this.offsets = offsets;
        this.params = params;
        this.cifAddress = cifAddress;
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
        T result = invoker.invoke(cifAddress, function, parameter.address(), offsets);
        finish();
        return result;
    }

}
