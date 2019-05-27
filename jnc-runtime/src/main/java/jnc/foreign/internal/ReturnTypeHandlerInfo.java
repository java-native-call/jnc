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
abstract class ReturnTypeHandlerInfo<T> {

    static <T> ReturnTypeHandlerInfo<T> acFirst(InternalType internalType, Invoker<T> invoker) {
        return new AnnotationContextFirst<>(internalType, invoker);
    }

    static <T> ReturnTypeHandlerInfo<T> always(InternalType internalType, Invoker<T> invoker) {
        return new Always<>(internalType, invoker);
    }

    private final InternalType internalType;
    private final Invoker<T> invoker;

    ReturnTypeHandlerInfo(InternalType internalType, Invoker<T> invoker) {
        this.internalType = internalType;
        this.invoker = invoker;
    }

    public abstract InternalType getInternalType(AnnotationContext ac);

    public final Invoker<T> getInvoker() {
        return invoker;
    }

    private InternalType getInternalType() {
        return internalType;
    }

    private static class Always<T> extends ReturnTypeHandlerInfo<T> {

        Always(InternalType internalType, Invoker<T> invoker) {
            super(internalType, invoker);
        }

        @Override
        public InternalType getInternalType(AnnotationContext ac) {
            return super.getInternalType();
        }

    }

    private static class AnnotationContextFirst<T> extends ReturnTypeHandlerInfo<T> {

        AnnotationContextFirst(InternalType internalType, Invoker<T> invoker) {
            super(internalType, invoker);
        }

        @Override
        public InternalType getInternalType(AnnotationContext ac) {
            Typedef annotation = ac.getAnnotation(Typedef.class);
            if (annotation != null) {
                return TypeHelper.findByAlias(annotation.value());
            }
            return super.getInternalType();
        }

    }
}
