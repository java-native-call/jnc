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

import jnc.foreign.NativeType;
import jnc.foreign.Pointer;

/**
 *
 * @author zhanhb
 */
enum PointerHandlerInfo implements InvokerHandlerInfo {

    INSTANCE;

    @Override
    public InternalType getType(Class<?> returnType, TypeFactory typeFactory, AnnotationContext ac) {
        return typeFactory.findByNativeType(NativeType.POINTER);
    }

    @Override
    public InvokeHandler<Pointer> getHandler(Class<?> returnType, InternalType retType) {
        return UnboundedDirectMemory::of;
    }

}