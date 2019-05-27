package jnc.foreign.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Objects;
import javax.annotation.Nullable;

@SuppressWarnings("UtilityClassWithoutPrivateConstructor")
public class AnnotationUtil {

    private static <T extends Annotation> T getAnnotation0(Annotation[] annotations, Class<T> type) {
        for (Annotation annotation : annotations) {
            T anno = annotation.annotationType().getAnnotation(type);
            if (anno != null) {
                return anno;
            }
        }
        return null;
    }

    @Nullable
    static <T extends Annotation> T getMethodAnnotation(Method method, Class<T> type) {
        T annotation = method.getAnnotation(type);
        if (annotation != null) {
            return annotation;
        }
        return getAnnotation0(method.getAnnotations(), type);
    }

    @Nullable
    @SuppressWarnings("NestedAssignment")
    public static <T extends Annotation> T getClassAnnotation(Class<?> klass, Class<T> type) {
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
        if (pkg != null) {
            anno = pkg.getAnnotation(type);
            if (anno != null) {
                return anno;
            }
            return getAnnotation0(pkg.getAnnotations(), type);
        }
        return null;
    }

    @Nullable
    static <T extends Annotation> T getAnnotation(Annotation[] annotations, Class<T> type) {
        Objects.requireNonNull(type, "type");
        for (Annotation annotation : annotations) {
            if (type.isInstance(annotation)) {
                return type.cast(annotation);
            }
        }
        return getAnnotation0(annotations, type);
    }

}
