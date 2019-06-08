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

import java.lang.reflect.Array;
import jnc.foreign.NativeType;
import jnc.foreign.Pointer;

/**
 *
 * @author zhanhb
 */
final class PrimitiveArrayHandler<T> implements ParameterHandlerInfo, ParameterPutter<T> {

    static <T> PrimitiveArrayHandler<T> of(ArrayMemoryCopy<T> toNative, ArrayMemoryCopy<T> fromNative, int unit) {
        return new PrimitiveArrayHandler<>(toNative, fromNative, unit);
    }

    private final ArrayMemoryCopy<T> toNative;
    private final ArrayMemoryCopy<T> fromNative;
    private final int unit;

    private PrimitiveArrayHandler(ArrayMemoryCopy<T> toNative, ArrayMemoryCopy<T> fromNative, int unit) {
        this.toNative = toNative;
        this.fromNative = fromNative;
        this.unit = unit;
    }

    @Override
    public ParameterPutter<?> getPutter(Class<?> type) {
        return this;
    }

    @Override
    public InternalType getType(Class<?> type, TypeFactory typeFactory, AnnotationContext ac) {
        return typeFactory.findByNativeType(NativeType.POINTER);
    }

    @Override
    @SuppressWarnings("NestedAssignment")
    public void doPut(CallContext context, int index, T array) {
        int len;
        if (array == null) {
            context.putLong(index, 0);
        } else if ((len = Array.getLength(array)) == 0) {
            context.putLong(index, EmptyMemoryHolder.NOMEMORY.address());
        } else {
            int offset = 0;
            int off = 0;
            Pointer memory = AllocatedMemory.allocate(len, unit);
            toNative.handle(memory, offset, array, off, len);
            context.onFinish(() -> fromNative.handle(memory, offset, array, off, len)).putLong(index, memory.address());
        }
    }

}
