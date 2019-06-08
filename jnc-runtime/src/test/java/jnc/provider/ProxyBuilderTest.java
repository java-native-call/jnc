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

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import java.util.Iterator;
import java.util.function.Consumer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * @author zhanhb
 */
public class ProxyBuilderTest {

    private static Serializable createSerializable(InvocationHandler ih) {
        Class<Serializable> c = Serializable.class;
        return c.cast(Proxy.newProxyInstance(c.getClassLoader(),
                new Class<?>[]{c}, ih));
    }

    @Test
    public void testEquals() {
        // toString must be implemented for junit require this to display error message if failed
        ProxyBuilder builder = ProxyBuilder.empty().objectEquals().proxyToString();
        {
            InvocationHandler handler = builder.toInvocationHandler();
            Serializable serializable = createSerializable(handler);
            assertEquals(serializable, serializable);
            assertNotEquals(serializable, createSerializable(handler));
        }

        {
            InvocationHandler handler = builder.equalsWithProxySame().toInvocationHandler();
            Serializable instance1 = createSerializable(handler);
            Serializable instance2 = createSerializable(handler);
            assertNotSame(instance1, instance2);
            assertEquals(instance1, instance2);
        }
    }

    @Test
    public void testToStringAndHashCode() {
        InterfaceHasObjectMethods instance = ProxyBuilder.identifier()
                .newInstance(InterfaceHasObjectMethods.class);
        int idHash = System.identityHashCode(instance);
        assertThat(instance.toString()).contains(Long.toHexString(idHash));
        assertThat(instance.hashCode()).isEqualTo(idHash);
    }

    @Test
    public void testHashCode() {
        // toString must be implemented for junit require this to display error message if failed
        ProxyBuilder builder = ProxyBuilder.empty().objectHashCode().proxyToString();
        {
            Serializable serializable = builder.newInstance(Serializable.class);
            assertEquals(System.identityHashCode(serializable), serializable.hashCode());
        }

        {
            Serializable serializable = builder.proxyHashCode().newInstance(Serializable.class);
            assertEquals(System.identityHashCode(Proxy.getInvocationHandler(serializable)), serializable.hashCode());
        }
    }

    @Test
    public void testToString() {
        ProxyBuilder builder = ProxyBuilder.empty().objectToString();
        {
            Serializable serializable = builder.newInstance(Serializable.class);
            assertThat(serializable.toString())
                    .contains(Long.toHexString(System.identityHashCode(serializable)))
                    .contains(serializable.getClass().getName());
        }

        {
            Serializable serializable = builder.proxyToString().newInstance(Serializable.class);
            assertThat(serializable.toString())
                    .doesNotContain(Long.toHexString(System.identityHashCode(serializable)))
                    .contains(serializable.getClass().getName());
        }
    }

    private <T> void testDefaultMethodOf(
            Class<T> interfaceClass, Consumer<T> consumer,
            Class<? extends Throwable> exceptionClass) {
        ProxyBuilder builder = ProxyBuilder.empty().objectToString();
        {
            T t = builder.defaultMethods().newInstance(interfaceClass);
            assertThatThrownBy(() -> consumer.accept(t))
                    .isExactlyInstanceOf(exceptionClass);
        }
        {
            T t = builder.defaultMethods(false).newInstance(interfaceClass);
            assertThatThrownBy(() -> consumer.accept(t))
                    .isExactlyInstanceOf(AbstractMethodError.class);
        }
    }

    // TODO, failed on jdk8
    //  failed on jdk9 if run with --illegal-access=deny
    @SuppressWarnings("rawtypes")
    // @Test
    public void testDefaultMethod() {
        testDefaultMethodOf(Iterator.class, Iterator::remove, UnsupportedOperationException.class);
    }

    @Test
    public void testDefaultMethodInCurrentModule() {
        testDefaultMethodOf(MyInterface.class, MyInterface::defaultMethod, StringIndexOutOfBoundsException.class);
    }

    @Test
    public void testThrow() {
        // toString must be implemented for junit require this to display error message if failed
        ProxyBuilder builder = ProxyBuilder.empty().proxyToString();
        {
            Closeable closeable = builder.newInstance(Closeable.class);
            assertThatThrownBy(closeable::close).isInstanceOf(AbstractMethodError.class).hasMessage("close");
        }

        {
            Closeable closeable = builder
                    .orThrow(method -> new UnsupportedOperationException(method.getName()))
                    .newInstance(Closeable.class);
            assertThatThrownBy(closeable::close).isInstanceOf(UnsupportedOperationException.class).hasMessage("close");
        }

        {
            Closeable closeable = builder
                    .orThrow(method -> new IOException(method.getName()))
                    .newInstance(Closeable.class);
            assertThatThrownBy(closeable::close).isInstanceOf(IOException.class).hasMessage("close");
        }

        {
            Closeable closeable = builder
                    .orThrow(method -> new Exception(method.getName()))
                    .newInstance(Closeable.class);
            assertThatThrownBy(closeable::close).isInstanceOf(UndeclaredThrowableException.class)
                    .hasCauseExactlyInstanceOf(Exception.class);
        }
    }

    private interface InterfaceHasObjectMethods {

        @Override
        String toString();

        @Override
        boolean equals(Object obj);
    }

    private interface MyInterface {

        default void defaultMethod() {
            throw new StringIndexOutOfBoundsException();
        }
    }

}
