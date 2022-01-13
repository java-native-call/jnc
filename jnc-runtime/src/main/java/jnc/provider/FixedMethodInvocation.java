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

import jnc.foreign.enums.CallingConvention;

final class FixedMethodInvocation implements MethodInvocation {

    private final CallingConvention convention;
    private final ParameterPutter<?>[] putters;
    private final CifContainer container;
    private final RawConverter<?> rawConverter;
    private final long function;

    FixedMethodInvocation(
            ParameterPutter<?>[] putters,
            CallingConvention convention,
            RawConverter<?> rawConverter,
            long function,
            InternalType retType,
            InternalType[] ptypes) {
        this.convention = convention;
        this.putters = putters;
        this.container = CifContainer.create(convention, retType, ptypes);
        this.rawConverter = rawConverter;
        this.function = function;
    }

    @Override
    public CallingConvention getCallingConvention() {
        return convention;
    }

    @Override
    public Object invoke(Object proxy, Object[] args) {
        @SuppressWarnings(value = "unchecked")
        ParameterPutter<Object>[] h = (ParameterPutter<Object>[]) putters;
        int length = h.length;
        CallContext context = container.newCallContext();
        for (int i = 0; i < length; i++) {
            h[i].doPut(context, i, args[i]);
        }
        return context.invoke(rawConverter, function);
    }

}
