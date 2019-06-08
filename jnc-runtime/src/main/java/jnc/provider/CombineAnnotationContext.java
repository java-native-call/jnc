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

import java.lang.annotation.Annotation;
import java.util.Objects;

/**
 *
 * @author zhanhb
 */
final class CombineAnnotationContext implements AnnotationContext {

    private final AnnotationContext b;
    private final AnnotationContext a;

    CombineAnnotationContext(AnnotationContext a, AnnotationContext b) {
        this.a = Objects.requireNonNull(a);
        this.b = Objects.requireNonNull(b);
    }

    @Override
    public <T extends Annotation> T getAnnotation(Class<T> annotationClass) {
        T annotation = a.getAnnotation(annotationClass);
        return annotation != null ? annotation : b.getAnnotation(annotationClass);
    }

}
