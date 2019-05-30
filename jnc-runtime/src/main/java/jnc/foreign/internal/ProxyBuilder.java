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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;
import static java.util.Objects.requireNonNull;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * @author zhanhb
 */
@SuppressWarnings("WeakerAccess")
class ProxyBuilder {

    private static final Method OBJECT_TO_STRING;
    private static final Method OBJECT_EQUALS;
    private static final Method OBJECT_HASH_CODE;

    private static final InvocationHandler objectToString = (proxy, __, args) -> proxy.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(proxy));
    private static final InvocationHandler objectHashCode = (proxy, __, args) -> System.identityHashCode(proxy);
    private static final InvocationHandler objectEquals = (proxy, __, args) -> proxy == args[0];

    private static final InvocationHandler proxyToString = (proxy, __, args) -> proxy.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(Proxy.getInvocationHandler(proxy)));
    private static final InvocationHandler proxyHashCode = (proxy, __, args) -> System.identityHashCode(Proxy.getInvocationHandler(proxy));
    private static final InvocationHandler proxyEquals = (proxy, __, args) -> {
        Object another = args[0];
        return proxy == another || another != null
                && Proxy.isProxyClass(another.getClass())
                && Proxy.getInvocationHandler(proxy) == Proxy.getInvocationHandler(another);
    };

    static {
        try {
            OBJECT_EQUALS = Object.class.getMethod("equals", Object.class);
            OBJECT_HASH_CODE = Object.class.getMethod("hashCode");
            OBJECT_TO_STRING = Object.class.getMethod("toString");
        } catch (NoSuchMethodException ex) {
            throw new AssertionError(ex);
        }
    }

    public static ProxyBuilder builder() {
        return new ProxyBuilder();
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> RuntimeException throwUnchecked(Throwable throwable) throws T {
        throw (T) throwable;
    }

    private final Map<Method, InvocationHandler> map = new HashMap<>(4);
    private boolean useDefaultMethod;
    private Function<Method, InvocationHandler> otherwise;
    private Function<Method, ? extends Throwable> orThrow = method -> new AbstractMethodError(method.getName());

    private ProxyBuilder() {
    }

    public ProxyBuilder customize(Method method, InvocationHandler handler) {
        map.put(requireNonNull(method), requireNonNull(handler));
        return this;
    }

    public ProxyBuilder otherwise(Function<Method, InvocationHandler> otherwise) {
        this.otherwise = requireNonNull(otherwise);
        return this;
    }

    public ProxyBuilder otherwise(InvocationHandler otherwise) {
        requireNonNull(otherwise);
        return otherwise(__ -> otherwise);
    }

    public ProxyBuilder toStringWith(InvocationHandler handler) {
        return customize(OBJECT_TO_STRING, handler);
    }

    public ProxyBuilder equalsWith(InvocationHandler handler) {
        return customize(OBJECT_EQUALS, handler);
    }

    public ProxyBuilder hashCodeWith(InvocationHandler handler) {
        return customize(OBJECT_HASH_CODE, handler);
    }

    public ProxyBuilder useObjectToString() {
        return toStringWith(objectToString);
    }

    public ProxyBuilder useObjectHashCode() {
        return hashCodeWith(objectHashCode);
    }

    public ProxyBuilder useObjectEquals() {
        return equalsWith(objectEquals);
    }

    public ProxyBuilder useObjectMethods() {
        return useObjectEquals().useObjectHashCode().useObjectToString();
    }

    public ProxyBuilder useProxyToString() {
        return toStringWith(proxyToString);
    }

    public ProxyBuilder useProxyHashCode() {
        return hashCodeWith(proxyHashCode);
    }

    public ProxyBuilder useProxyEquals() {
        return equalsWith(proxyEquals);
    }

    public ProxyBuilder useProxyMethods() {
        return useProxyEquals().useProxyHashCode().useProxyToString();
    }

    public ProxyBuilder orThrow(Function<Method, ? extends Throwable> orThrow) {
        this.orThrow = requireNonNull(orThrow);
        return this;
    }

    @SuppressWarnings("ThrowableResultIgnored")
    public ProxyBuilder orThrow(Throwable orThrow) {
        requireNonNull(orThrow);
        this.orThrow = __ -> orThrow;
        return this;
    }

    public ProxyBuilder useDefaultMethod() {
        this.useDefaultMethod = true;
        return this;
    }

    public ProxyBuilder customDefaultMethod() {
        this.useDefaultMethod = false;
        return this;
    }

    public InvocationHandler toInvocationHandler() {
        final ConcurrentMap<Method, InvocationHandler> map = new ConcurrentHashMap<>(this.map);
        final boolean useDefaultMethod = this.useDefaultMethod;
        final Function<Method, InvocationHandler> otherwise = this.otherwise;
        final Function<Method, ? extends Throwable> orThrow = this.orThrow;
        return (proxy, method, args) -> map.computeIfAbsent(method, m -> {
            try {
                if (useDefaultMethod && m.isDefault()) {
                    return DefaultMethodInvoker.getInstance(m);
                }
                final InvocationHandler handler = otherwise != null ? otherwise.apply(m) : null;
                if (handler == null) {
                    throw orThrow.apply(m);
                }
                return handler;
            } catch (Throwable ex) {
                //noinspection RedundantTypeArguments
                throw ProxyBuilder.<RuntimeException>throwUnchecked(ex);
            }
        }).invoke(proxy, method, args);
    }

    public <T> T newInstance(Class<T> interfaceClass) {
        return interfaceClass.cast(Proxy.newProxyInstance(interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass}, toInvocationHandler()));
    }

}
