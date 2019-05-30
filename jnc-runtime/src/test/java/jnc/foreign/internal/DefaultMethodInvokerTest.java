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

import java.io.IOException;
import java.lang.reflect.InvocationHandler;
import java.util.concurrent.atomic.AtomicInteger;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhanhb
 */
public class DefaultMethodInvokerTest {

    private static final Logger log = LoggerFactory.getLogger(DefaultMethodInvokerTest.class);

    /**
     * Test of getInstance method, of class DefaultMethodInvoker.
     */
    @Test
    public void testGetInstance() throws Throwable {
        AtomicInteger integer = new AtomicInteger();
        // the second parameter is not used of returning lambda
        // here use null to test it
        DefaultMethodInvoker.getInstance(TestInterface.class.getMethod("defaultMethod", AtomicInteger.class))
                .invoke(new TestInterfaceImpl(),
                        null, new Object[]{integer});
        assertThat(integer.get()).isEqualTo(1);

        InvocationHandler ih = DefaultMethodInvoker.getInstance(Throw.class.getMethod("defaultMethod"));
        assertThatThrownBy(() -> ih.invoke(new ThrowImpl(), null, new Object[]{}))
                .isExactlyInstanceOf(IOException.class);
    }

    private interface TestInterface {

        default void defaultMethod(AtomicInteger integer) {
            integer.getAndIncrement();
        }
    }

    private class TestInterfaceImpl implements TestInterface {

        @Override
        public final void defaultMethod(AtomicInteger integer) {
            throw new UnsupportedOperationException();
        }
    }

    private interface Throw {

        default void defaultMethod() throws IOException {
            throw new IOException();
        }
    }

    private class ThrowImpl implements Throw {

        @Override
        public void defaultMethod() {
            throw new StringIndexOutOfBoundsException();
        }
    }

}
