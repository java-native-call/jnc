package jnc.foreign.internal;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

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

    /*nullable*/
    public static <T extends Annotation> T getAnnotation(Method method, Class<T> type) {
        T anno = method.getAnnotation(type);
        if (anno != null) {
            return anno;
        }
        return getAnnotation0(method.getAnnotations(), type);
    }

    /*nullable*/
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

    /*nullable*/
    static <T extends Annotation> T getAnnotation(Annotation[] annotationse, Class<T> type) {
        type.getClass(); // null check
        for (Annotation annotation : annotationse) {
            if (type.isInstance(annotation)) {
                return type.cast(annotation);
            }
        }
        return getAnnotation0(annotationse, type);
    }

}
