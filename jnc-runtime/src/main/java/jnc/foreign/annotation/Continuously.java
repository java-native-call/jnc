package jnc.foreign.annotation;

import java.lang.annotation.Documented;
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
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Continuously {

    long start() default 0;

    /**
     * native type mapped to, corresponding signed type if {@link #start()} is
     * negative, unsigned type if start is great or equals to zero
     */
    NativeType type() default NativeType.UINT32;

    /**
     * when converting this enum from native, the result is doesn't match any
     * value of this enum.
     */
    EnumMappingErrorAction onUnmappable() default EnumMappingErrorAction.NULL_WHEN_ZERO;

}
