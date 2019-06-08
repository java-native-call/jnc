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

import jnc.foreign.Foreign;
import jnc.foreign.NativeType;
import jnc.foreign.Pointer;
import jnc.foreign.byref.ByReference;
import jnc.foreign.spi.ForeignProvider;

/**
 *
 * @author zhanhb
 */
enum ByReferenceHandler implements ParameterHandlerInfo, ParameterPutter<ByReference> {

    INSTANCE;

    @Override
    public void doPut(CallContext context, int index, ByReference obj) {
        if (obj == null) {
            context.putLong(index, 0);
        } else {
            Foreign foreign = ForeignProvider.getDefault().getForeign();
            Pointer memory = AllocatedMemory.allocate(obj.componentType(foreign).size());
            obj.toNative(foreign, memory);
            context.onFinish(() -> obj.fromNative(foreign, memory)).putLong(index, memory.address());
        }
    }

    @Override
    public ParameterPutter<ByReference> getPutter(Class<?> type) {
        return this;
    }

    @Override
    public InternalType getType(Class<?> type, TypeFactory typeFactory, AnnotationContext ac) {
        return typeFactory.findByNativeType(NativeType.POINTER);
    }

}
