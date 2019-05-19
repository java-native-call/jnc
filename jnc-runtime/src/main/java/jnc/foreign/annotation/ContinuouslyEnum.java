package jnc.foreign.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jnc.foreign.NativeType;

/**
 * Indicate how to convert this enum to native or from native. Enums without
 * this annotation will be processed as if it has an annotation all default
 * values in this annotation.
 *
 * @author zhanhb
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ContinuouslyEnum {

    /**
     * native type to mapped with
     */
    NativeType type() default NativeType.UINT32;

    int start() default 0;

    /**
     * when converting this enum from native, the result is zero, this
     * configuration is ignored if this enum has already a mapping value to
     * zero.
     */
    EnumMappingErrorAction onZero() default EnumMappingErrorAction.SET_TO_NULL;

    /**
     * when converting this enum from native, the result is neither mapping to
     * any value, nor is zero.
     */
    EnumMappingErrorAction onUnmappable() default EnumMappingErrorAction.REPORT;

}
