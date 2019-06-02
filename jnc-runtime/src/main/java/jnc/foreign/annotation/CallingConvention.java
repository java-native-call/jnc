package jnc.foreign.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Calling Convention searching order: method annotation, class annotation, load
 * options.
 *
 * @author zhanhb
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.ANNOTATION_TYPE)
public @interface CallingConvention {

    jnc.foreign.enums.CallingConvention value();

}
