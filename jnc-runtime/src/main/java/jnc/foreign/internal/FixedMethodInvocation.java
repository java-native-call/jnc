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

import java.lang.reflect.Method;
import jnc.foreign.enums.CallingConvention;

final class FixedMethodInvocation implements MethodInvocation {

    private final CallingConvention convention;
    private final ParameterHandler<?>[] handlers;
    private final CifContainer container;
    private final Invoker<?> invoker;
    private final long function;

    FixedMethodInvocation(
            ParameterHandler<?>[] handlers,
            CallingConvention convention,
            Invoker<?> invoker,
            long function,
            InternalType retType,
            InternalType[] ptypes) {
        this.convention = convention;
        this.handlers = handlers;
        this.container = CifContainer.create(convention, retType, ptypes);
        this.invoker = invoker;
        this.function = function;
    }

    @Override
    public CallingConvention getCallingConvention() {
        return convention;
    }

    @Override
    public Object invoke(Object proxy, Method m, Object[] args) {
        @SuppressWarnings(value = "unchecked")
        ParameterHandler<Object>[] h = (ParameterHandler<Object>[]) handlers;
        int length = h.length;
        CallContext context = container.newCallContext();
        for (int i = 0; i < length; i++) {
            h[i].handle(context, i, args[i]);
        }
        return context.invoke(invoker, function);
    }

}
