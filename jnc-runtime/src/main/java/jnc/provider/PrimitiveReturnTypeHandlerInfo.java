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

import java.util.function.Function;
import jnc.foreign.NativeType;
import jnc.foreign.annotation.Typedef;

/**
 * @author zhanhb
 */
final class PrimitiveReturnTypeHandlerInfo<T> implements InvokerHandlerInfo {

    static <T> PrimitiveReturnTypeHandlerInfo<T> of(Class<T> klass, InternalType defaultType) {
        Function<NativeType, InvokeHandler<T>> functions
                = PrimitiveConverter.INSTANCE.getInvokerConvertors(Primitives.unwrap(klass));
        return new PrimitiveReturnTypeHandlerInfo<>(defaultType, functions);
    }

    private final InternalType defaultType;
    private final Function<NativeType, InvokeHandler<T>> functions;

    private PrimitiveReturnTypeHandlerInfo(InternalType defaultType,
            Function<NativeType, InvokeHandler<T>> functions) {
        this.defaultType = defaultType;
        this.functions = functions;
    }

    @Override
    public InternalType getType(Class<?> returnType, TypeFactory typeFactory, AnnotationContext ac) {
        Typedef annotation = ac.getAnnotation(Typedef.class);
        if (annotation != null) {
            return typeFactory.findByAlias(annotation.value());
        }
        return defaultType;
    }

    @Override
    public InvokeHandler<T> getHandler(Class<?> returnType, InternalType retType) {
        return functions.apply(retType.nativeType());
    }

}
