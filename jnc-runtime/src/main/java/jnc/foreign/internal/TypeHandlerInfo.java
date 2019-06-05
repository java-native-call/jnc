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

import jnc.foreign.annotation.Typedef;

/**
 * @author zhanhb
 */
final class TypeHandlerInfo<T> {

    static <T> TypeHandlerInfo<T> typedefFirst(InternalType type, T handler) {
        return new TypeHandlerInfo<>(type, handler, true);
    }

    static <T> TypeHandlerInfo<T> always(InternalType type, T invoker) {
        return new TypeHandlerInfo<>(type, invoker, false);
    }

    private final InternalType type;
    private final T handler;
    private final boolean searchAnnotation;

    private TypeHandlerInfo(InternalType type, T handler, boolean searchAnnotation) {
        this.type = type;
        this.handler = handler;
        this.searchAnnotation = searchAnnotation;
    }

    T getHandler() {
        return handler;
    }

    InternalType getType(TypeFactory typeFactory, AnnotationContext ac) {
        if (searchAnnotation) {
            Typedef annotation = ac.getAnnotation(Typedef.class);
            if (annotation != null) {
                return typeFactory.findByAlias(annotation.value());
            }
        }
        return type;
    }

}
