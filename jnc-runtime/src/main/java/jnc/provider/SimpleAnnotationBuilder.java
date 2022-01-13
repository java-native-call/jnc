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
import java.lang.reflect.Method;
import javax.annotation.Nullable;

/**
 *
 * @author zhanhb
 */
@Nullable
final class SimpleAnnotationBuilder<T extends Annotation> {

    private static final Method ANNOTATION_TYPE;

    static {
        try {
            ANNOTATION_TYPE = Annotation.class.getMethod("annotationType");
        } catch (NoSuchMethodException | SecurityException ex) {
            throw new AssertionError(ex);
        }
    }

    public static <T extends Annotation> SimpleAnnotationBuilder<T> of(Class<T> annotationClass) {
        if (!annotationClass.isAnnotation()) {
            throw new IllegalArgumentException("must be an annotation");
        }
        return new SimpleAnnotationBuilder<>(annotationClass);
    }

    private final Class<T> annotationClass;

    private SimpleAnnotationBuilder(Class<T> annotationClass) {
        this.annotationClass = annotationClass;
    }

    T build() {
        final Class<T> ac = annotationClass;
        return ProxyBuilder.identifier()
                .customize(ANNOTATION_TYPE, (obj, args) -> ac)
                .otherwise(method -> (obj, args) -> method.getDefaultValue())
                .newInstance(ac);
    }

}
