package jnc.foreign.abi;

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
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE, ElementType.TYPE, ElementType.METHOD})
@CallingConvertion(CallingMode.STDCALL)
public @interface Stdcall {
}
