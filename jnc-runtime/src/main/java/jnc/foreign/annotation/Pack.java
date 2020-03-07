package jnc.foreign.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation on a struct class will lead this struct to be packed as this value
 *
 * <pre>
 * #pragma pack(push, 2)
 * struct Sample {
 *     int x;
 *     char d;
 * };
 * #pragma pack(pop)
 * sizeof(Sample) == 6
 * </pre>
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Pack {

    /**
     * ignore if it's zero, when inherit class doesn't need a pack.
     *
     * @return the pack value of this structure
     */
    int value();

}
