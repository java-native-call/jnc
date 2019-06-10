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

import jnc.foreign.Struct;

/**
 *
 * @author zhanhb
 */
final class StructHandler implements ParameterHandlerInfo, ParameterPutter<Struct> {

    private final InternalType pointerType;

    StructHandler(InternalType pointerType) {
        this.pointerType = pointerType;
    }

    @Override
    public ParameterPutter<Struct> getPutter(Class<?> type) {
        return this;
    }

    @Override
    public InternalType getType(Class<?> type, TypeFactory typeFactory, AnnotationContext ac) {
        return pointerType;
    }

    @Override
    public void doPut(CallContext context, int index, Struct obj) {
        context.putLong(index, obj == null ? 0 : obj.getMemory().address());
    }

}
