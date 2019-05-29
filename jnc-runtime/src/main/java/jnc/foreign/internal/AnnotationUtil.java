package jnc.foreign.internal;

import java.lang.annotation.Annotation;
import java.lang.annotation.ElementType;
import java.lang.annotation.Target;
import java.lang.reflect.AnnotatedElement;
import java.util.Arrays;
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
    public static <T extends Annotation> T getAnnotation(AnnotatedElement element, Class<T> type) {
        T annotation = element.getAnnotation(type);
        if (annotation != null) {
            return annotation;
        }
        Target target = type.getAnnotation(Target.class);
        if (target != null) {
            if (Arrays.asList(target.value()).contains(ElementType.ANNOTATION_TYPE)) {
                return getAnnotation0(element.getAnnotations(), type);
            }
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
