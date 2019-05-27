package jnc.foreign.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation only work on windows x86, ignored on other platform or
 * windows x64. Indicate this method is stdcall, cdecl otherwise.
 *
 * @author zhanhb
 */
@CallingConvention(jnc.foreign.enums.CallingConvention.STDCALL)
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD})
public @interface Stdcall {
}
