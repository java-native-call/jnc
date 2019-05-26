package jnc.foreign.typedef.bsd;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import jnc.foreign.annotation.Typedef;
import jnc.foreign.enums.TypeAlias;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.PARAMETER})
@Typedef(TypeAlias.segsz_t)
public @interface segsz_t {
}
