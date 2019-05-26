/*
 * Copyright 2019 zhanhb.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package jnc.foreign.internal;

import java.lang.reflect.Method;
import javax.annotation.Nonnull;
import jnc.foreign.annotation.Continuously;
import jnc.foreign.annotation.In;
import jnc.foreign.annotation.Typedef;
import jnc.foreign.enums.EnumMappingErrorAction;
import jnc.foreign.enums.TypeAlias;
import jnc.foreign.typedef.size_t;
import jnc.foreign.typedef.uint8_t;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhanhb
 */
public class AnnotationUtilTest {

    private static final Logger log = LoggerFactory.getLogger(AnnotationUtilTest.class);

    /**
     * Test of getMethodAnnotation method, of class AnnotationUtil.
     */
    @Test
    public void testGetMethodAnnotation() throws NoSuchMethodException {
        log.info("getMethodAnnotation");
        Method method = Class1.class.getMethod("method");
        Nonnull nonnull = AnnotationUtil.getMethodAnnotation(method, Nonnull.class);
        assertNotNull("@Nonnull not found on " + method, nonnull);

        Typedef typedef = AnnotationUtil.getMethodAnnotation(method, Typedef.class);
        assertNotNull("@Typedef not found on " + method, typedef);
        assertEquals(TypeAlias.size_t, typedef.value());
    }

    /**
     * Test of getClassAnnotation method, of class AnnotationUtil.
     */
    @Test
    public void testGetClassAnnotation() {
        log.info("getClassAnnotation");
        EnumMappingErrorAction expect = EnumMappingErrorAction.REPORT_ALL;
        Continuously continuously = AnnotationUtil.getClassAnnotation(Enum1.class, Continuously.class);
        assertNotNull(continuously);
        EnumMappingErrorAction onUnmappable = continuously.onUnmappable();
        assertEquals(expect, onUnmappable);
    }

    /**
     * Test of getAnnotation method, of class AnnotationUtil.
     */
    @Test
    public void testGetAnnotation() throws NoSuchMethodException {
        log.info("getAnnotation");
        Method method = Class1.class.getMethod("method2", int.class);

        Typedef typedef = AnnotationUtil.getAnnotation(method.getParameterAnnotations()[0], Typedef.class);
        assertNotNull("@Typedef not found on parameter of " + method, typedef);
        assertEquals(TypeAlias.uint8_t, typedef.value());

        In in = AnnotationUtil.getAnnotation(method.getParameterAnnotations()[0], In.class);
        assertNotNull("@In not found on parameter of " + method, in);
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    private static class Class1 {

        @Nonnull
        @size_t
        public Object method() {
            return new Object();
        }

        public void method2(@uint8_t @In int parameter) {
        }

    }

    @Continuously(onUnmappable = EnumMappingErrorAction.REPORT_ALL)
    private enum Enum1 {
    }

}
