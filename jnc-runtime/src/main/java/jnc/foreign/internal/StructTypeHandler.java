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

import jnc.foreign.NativeType;
import jnc.foreign.Struct;

/**
 *
 * @author zhanhb
 */
enum StructTypeHandler implements InternalTypeHandler<Struct> {
    INSTANCE;

    @SuppressWarnings("unchecked")
    static <T> InternalTypeHandler<T> getInstance() {
        return (InternalTypeHandler<T>) INSTANCE;
    }

    @Override
    public BuiltinType getBuiltinType() {
        return BuiltinType.POINTER;
    }

    @Override
    public Invoker<Struct> getInvoker() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public ParameterHandler<Struct> getParameterHandler() {
        return (context, index, obj)
                -> context.putLong(index, obj == null ? 0 : obj.getMemory().address());
    }

    @Override
    public NativeType nativeType() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
