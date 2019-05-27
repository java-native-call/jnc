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
abstract class ParameterHandlerInfo<T> {

    static <T> ParameterHandlerInfo<T> acFirst(InternalType internalType, ParameterHandler<T> invoker) {
        return new AnnotationContextFirst<>(internalType, invoker);
    }

    static <T> ParameterHandlerInfo<T> always(InternalType internalType, ParameterHandler<T> invoker) {
        return new Always<>(internalType, invoker);
    }

    private final InternalType type;
    private final ParameterHandler<T> handler;

    ParameterHandlerInfo(InternalType type, ParameterHandler<T> handler) {
        this.type = type;
        this.handler = handler;
    }

    InternalType getType() {
        return type;
    }

    public abstract InternalType getType(AnnotationContext ac);

    public ParameterHandler<T> getHandler() {
        return handler;
    }

    private static class Always<T> extends ParameterHandlerInfo<T> {

        private Always(InternalType type, ParameterHandler<T> handler) {
            super(type, handler);
        }

        @Override
        public InternalType getType(AnnotationContext ac) {
            return super.getType();
        }

    }

    private static class AnnotationContextFirst<T> extends ParameterHandlerInfo<T> {

        private AnnotationContextFirst(InternalType type, ParameterHandler<T> handler) {
            super(type, handler);
        }

        @Override
        public InternalType getType(AnnotationContext ac) {
            Typedef annotation = ac.getAnnotation(Typedef.class);
            if (annotation != null) {
                return TypeHelper.findByAlias(annotation.value());
            }
            return super.getType();
        }

    }

}
