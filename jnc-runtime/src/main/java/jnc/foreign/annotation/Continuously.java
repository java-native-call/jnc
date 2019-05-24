package jnc.foreign.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jnc.foreign.NativeType;
import jnc.foreign.enums.EnumMappingErrorAction;

/**
 * Indicate how to convert this enum to native or from native. Enums without
 * this annotation will be processed as if it has an annotation all default
 * values in this annotation.
 *
 * @author zhanhb
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Continuously {

    /**
     * native type to mapped with
     */
    NativeType type() default NativeType.UINT32;

    int start() default 0;

    /**
     * when converting this enum from native, the result is doesn't match any
     * value of this enum.
     */
    EnumMappingErrorAction onUnmappable() default EnumMappingErrorAction.NULL_WHEN_ZERO;

}
