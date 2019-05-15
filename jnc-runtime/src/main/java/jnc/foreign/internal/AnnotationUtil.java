package jnc.foreign.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Objects;
import javax.annotation.Nullable;

@SuppressWarnings("UtilityClassWithoutPrivateConstructor")
public class AnnotationUtil {

    private static <T extends Annotation> T getAnnotation0(Annotation[] annotationse, Class<T> type) {
        for (Annotation annotation : annotationse) {
            T anno = annotation.annotationType().getAnnotation(type);
            if (anno != null) {
                return anno;
            }
        }
        return null;
    }

    @Nullable
    static <T extends Annotation> T getAnnotation(Method method, Class<T> type) {
        T annotation = method.getAnnotation(type);
        if (annotation != null) {
            return annotation;
        }
        return getAnnotation0(method.getAnnotations(), type);
    }

    @Nullable
    @SuppressWarnings("NestedAssignment")
    public static <T extends Annotation> T getAnnotation(Class<?> klass, Class<T> type) {
        T anno;
        for (Class<?> tmp = klass;
                tmp != null;
                tmp = tmp.getEnclosingClass()) {
            if ((anno = tmp.getAnnotation(type)) != null
                    || (anno = getAnnotation0(tmp.getAnnotations(), type)) != null) {
                return anno;
            }
        }
        Package pkg = klass.getPackage();
        return pkg != null ? pkg.getAnnotation(type) : null;
    }

    @Nullable
    static <T extends Annotation> T getAnnotation(Annotation[] annotationse, Class<T> type) {
        Objects.requireNonNull(type, "type");
        for (Annotation annotation : annotationse) {
            if (type.isInstance(annotation)) {
                return type.cast(annotation);
            }
        }
        return getAnnotation0(annotationse, type);
    }

}
