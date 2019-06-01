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
abstract class TypeHandlerInfo<T> {

    static <T> TypeHandlerInfo<T> typedefFirst(InternalType type, T handler) {
        return new TypedefFirst<>(type, handler);
    }

    static <T> TypeHandlerInfo<T> always(InternalType type, T invoker) {
        return new Always<>(type, invoker);
    }

    private final InternalType type;
    private final T handler;

    TypeHandlerInfo(InternalType type, T handler) {
        this.type = type;
        this.handler = handler;
    }

    InternalType getType() {
        return type;
    }

    public abstract InternalType getType(TypeFactory typeFactory, AnnotationContext ac);

    public T getHandler() {
        return handler;
    }

    private static final class Always<T> extends TypeHandlerInfo<T> {

        private Always(InternalType type, T handler) {
            super(type, handler);
        }

        @Override
        public InternalType getType(TypeFactory typeFactory, AnnotationContext ac) {
            return super.getType();
        }

    }

    private static final class TypedefFirst<T> extends TypeHandlerInfo<T> {

        private TypedefFirst(InternalType type, T handler) {
            super(type, handler);
        }

        @Override
        public InternalType getType(TypeFactory typeFactory, AnnotationContext ac) {
            Typedef annotation = ac.getAnnotation(Typedef.class);
            if (annotation != null) {
                return typeFactory.findByAlias(annotation.value());
            }
            return super.getType();
        }

    }

}
