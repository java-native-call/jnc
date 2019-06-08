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
package jnc.provider;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import jnc.foreign.NativeType;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.AfterClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhanhb
 */
@RunWith(Parameterized.class)
public class PrimitiveConverterTest {

    private static final Logger log = LoggerFactory.getLogger(PrimitiveConverterTest.class);

    private static final Set<InvokeHandler<?>> handlers
            = Collections.newSetFromMap(new IdentityHashMap<>(44));

    @Parameterized.Parameters(name = "{index} {0} {1}")
    public static List<Object[]> data() {
        List<Object[]> list = new ArrayList<>(8 * (NativeType.values().length - 1));
        for (Class<?> primitiveType : Primitives.allPrimitiveTypes()) {
            for (NativeType type : NativeType.values()) {
                if (type == NativeType.POINTER) {
                    continue;
                }
                list.add(new Object[]{primitiveType, type});
            }
        }
        return list;
    }

    @AfterClass
    public static void tearDownClass() {
        log.info("total function number: {}", handlers.size());
    }

    private final Class<?> klass;
    private final NativeType type;

    public PrimitiveConverterTest(Class<?> klass, NativeType type) {
        this.klass = klass;
        this.type = type;
    }

    /**
     * Test of getInvokerConvertor method, of class PrimitiveConverter.
     */
    @Test
    public void testGetInvokerConvertor() {
        InvokeHandler<?> handler = PrimitiveConverter.INSTANCE.getInvokerConvertors(klass).apply(type);
        assertThat(handler)
                .describedAs("class=%s,native=%s", klass, type)
                .isNotNull();
        Object result = handler.handle(0);
        if (klass == void.class) {
            assertThat(result).isNull();
        } else {
            assertThat(result).isInstanceOf(Primitives.wrap(klass));
        }
        handlers.add(handler);
    }

}
