package jnc.foreign.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Calling Convention searching order: method annotation, load options, class annotation,
 * enclosing class annotation.
 *
 * @author zhanhb
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface CallingConvention {

    jnc.foreign.enums.CallingConvention value();

}
