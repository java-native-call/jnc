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

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.List;
import javax.annotation.Nullable;

/**
 * @author zhanhb
 */
interface AnnotationContext {

    static AnnotationContext newContext(AnnotatedElement element) {
        return new AnnotationContextImpl(element.getAnnotations());
    }

    static AnnotationContext[] newMethodParameterContexts(Method method) {
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        int length = parameterAnnotations.length;
        AnnotationContext[] annotationContexts = new AnnotationContext[length];
        for (int i = 0; i < length; ++i) {
            annotationContexts[i] = new AnnotationContextImpl(parameterAnnotations[i]);
        }
        return annotationContexts;
    }

    static AnnotationContext newMockContext(List<Class<? extends Annotation>> annotations, AnnotationContext previous) {
        return new CombineAnnotationContext(new AnnotationContextImpl(annotations.stream()
                .map(klass -> SimpleAnnotationBuilder.of(klass).build())
                .toArray(Annotation[]::new)), previous);
    }

    default boolean isAnnotationPresent(Class<? extends Annotation> annotationClass) {
        return getAnnotation(annotationClass) != null;
    }

    @Nullable
    <T extends Annotation> T getAnnotation(Class<T> annotationClass);

}
