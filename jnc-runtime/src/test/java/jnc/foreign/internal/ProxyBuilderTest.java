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

import java.io.Closeable;
import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 *
 * @author zhanhb
 */
public class ProxyBuilderTest {

    @Test
    public void testEquals() {
        // toString must be implemented for junit require this to display error message if failed
        ProxyBuilder builder = new ProxyBuilder().useObjectEquals().useProxyToString();
        InvocationHandler handler = builder.toInvocationHandler();
        Serializable instance1 = ProxyBuilder.newInstance(Serializable.class, handler);
        Serializable instance2 = ProxyBuilder.newInstance(Serializable.class, handler);
        assertNotEquals(instance1, instance2);

        handler = builder.useProxyEquals().toInvocationHandler();
        instance1 = ProxyBuilder.newInstance(Serializable.class, handler);
        instance2 = ProxyBuilder.newInstance(Serializable.class, handler);
        assertNotSame(instance1, instance2);
        assertEquals(instance1, instance2);
    }

    @Test
    public void testHashCode() {
        // toString must be implemented for junit require this to display error message if failed
        ProxyBuilder builder = new ProxyBuilder().useObjectHashCode().useProxyToString();
        Serializable serializable = builder.toInstance(Serializable.class);
        assertEquals(System.identityHashCode(serializable), serializable.hashCode());

        serializable = builder.useProxyHashCode().toInstance(Serializable.class);
        assertEquals(System.identityHashCode(Proxy.getInvocationHandler(serializable)), serializable.hashCode());
    }

    @Test
    public void testToString() {
        ProxyBuilder builder = new ProxyBuilder().useObjectToString();

        Serializable serializable = builder.toInstance(Serializable.class);
        assertThat(serializable.toString())
                .contains(Long.toHexString(System.identityHashCode(serializable)))
                .contains(serializable.getClass().getName());

        serializable = builder.useProxyToString().toInstance(Serializable.class);
        assertThat(serializable.toString())
                .doesNotContain(Long.toHexString(System.identityHashCode(serializable)))
                .contains(serializable.getClass().getName());
    }

    @Test
    public void testThrow() {
        // toString must be implemented for junit require this to display error message if failed
        ProxyBuilder builder = new ProxyBuilder().useProxyToString();
        Closeable closeable = builder.toInstance(Closeable.class);
        assertThatThrownBy(closeable::close).isInstanceOf(AbstractMethodError.class).hasMessage("close");

        closeable = builder.orThrow(method -> new UnsupportedOperationException(method.getName())).toInstance(Closeable.class);
        assertThatThrownBy(closeable::close).isInstanceOf(UnsupportedOperationException.class).hasMessage("close");

        closeable = builder.orThrow(method -> new IOException(method.getName())).toInstance(Closeable.class);
        assertThatThrownBy(closeable::close).isInstanceOf(IOException.class).hasMessage("close");

        closeable = builder.orThrow(method -> new Exception(method.getName())).toInstance(Closeable.class);
        assertThatThrownBy(closeable::close).isInstanceOf(UndeclaredThrowableException.class)
                .hasCauseExactlyInstanceOf(Exception.class);
    }

}
