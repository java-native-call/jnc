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
import jnc.foreign.annotation.In;
import jnc.foreign.annotation.Typedef;
import jnc.foreign.enums.TypeAlias;
import jnc.foreign.typedef.size_t;
import jnc.foreign.typedef.uint8_t;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhanhb
 */
public class AnnotationContextTest {

    private static final Logger log = LoggerFactory.getLogger(AnnotationContextTest.class);

    /**
     * Test of newContext method, of class AnnotationContext.
     */
    @Test
    public void testNewContext() throws NoSuchMethodException {
        log.info("newContext");
        Method method = Class1.class.getMethod("method");
        Typedef typedef = AnnotationContext.newContext(method)
                .getAnnotation(Typedef.class);
        assertNotNull("@Typedef not found on " + method, typedef);
        assertEquals(TypeAlias.size_t, typedef.value());
    }

    /**
     * Test of newMethodParameterContexts method, of class AnnotationContext.
     */
    @Test
    public void testNewMethodParameterContexts() throws NoSuchMethodException {
        log.info("newMethodParameterContexts");
        Method method = Class1.class.getMethod("method2", int.class);

        AnnotationContext ac = AnnotationContext.newMethodParameterContexts(method)[0];
        Typedef typedef = ac.getAnnotation(Typedef.class);
        assertNotNull("@Typedef not found on parameter of " + method, typedef);
        assertEquals(TypeAlias.uint8_t, typedef.value());

        assertTrue("@In not found on parameter of " + method, ac.isAnnotationPresent(In.class));
    }

    @SuppressWarnings({"WeakerAccess", "unused"})
    private static class Class1 {

        @size_t
        public Object method() {
            return null;
        }

        public void method2(@uint8_t @In int parameter) {
        }

    }

}
