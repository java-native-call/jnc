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

import jnc.foreign.annotation.Typedef;

/**
 *
 * @author zhanhb
 */
enum EnumHandlers implements InvokerHandlerInfo, ParameterHandlerInfo {

    INSTANCE;

    @SuppressWarnings("unchecked")
    private EnumTypeHandler<?> forHandler(Class<?> type) {
        return EnumTypeHandler.getInstance((Class) type);
    }

    @Override
    public InternalType getType(Class<?> returnType, TypeFactory typeFactory, AnnotationContext ac) {
        Typedef annotation = ac.getAnnotation(Typedef.class);
        if (annotation != null) {
            return typeFactory.findByAlias(annotation.value());
        }
        return forHandler(returnType).getDefaultType();
    }

    @Override
    public RawConverter<?> getRawConverter(Class<?> returnType, InternalType retType) {
        return forHandler(returnType);
    }

    @Override
    public ParameterPutter<?> getPutter(Class<?> type) {
        return forHandler(type);
    }

}
