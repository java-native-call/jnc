package jnc.foreign.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * indicate the value pass to the function is by value.
 *
 * For primitives types and wrapper types, parameters are always passed by
 * value, no need to annotation with this. Annotation @ByValue onto a struct
 * parameter will lead to pass the content of the structure to native.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.PARAMETER})
public @interface ByValue {
}
