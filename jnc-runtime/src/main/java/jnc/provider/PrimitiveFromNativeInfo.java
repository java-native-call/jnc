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
final class PrimitiveFromNativeInfo<T> implements InvokerHandlerInfo {

    static <T> PrimitiveFromNativeInfo<T> of(
            PrimitiveConverter pc, Class<T> returnType, InternalType defaultType) {
        Function<NativeType, RawConverter<T>> converters
                = pc.getConverters(Primitives.unwrap(returnType));
        return new PrimitiveFromNativeInfo<>(defaultType, converters);
    }

    private final InternalType defaultType;
    private final Function<NativeType, RawConverter<T>> converters;

    private PrimitiveFromNativeInfo(InternalType defaultType,
            Function<NativeType, RawConverter<T>> converters) {
        this.defaultType = defaultType;
        this.converters = converters;
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
    public RawConverter<T> getRawConverter(Class<?> returnType, InternalType retType) {
        return converters.apply(retType.nativeType());
    }

}
