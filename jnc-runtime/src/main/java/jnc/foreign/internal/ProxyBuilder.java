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
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * @author zhanhb
 */
@SuppressWarnings({"WeakerAccess", "unused"})
class ProxyBuilder {

    private static final Method OBJECT_TO_STRING;
    private static final Method OBJECT_EQUALS;
    private static final Method OBJECT_HASH_CODE;

    private static final InvocationHandler objectToString = (proxy, method, args) -> proxy.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(proxy));
    private static final InvocationHandler objectHashCode = (proxy, method, args) -> System.identityHashCode(proxy);
    private static final InvocationHandler objectEquals = (proxy, method, args) -> proxy == args[0];

    private static final InvocationHandler proxyToString = (proxy, method, args) -> proxy.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(Proxy.getInvocationHandler(proxy)));
    private static final InvocationHandler proxyHashCode = (proxy, method, args) -> System.identityHashCode(Proxy.getInvocationHandler(proxy));
    private static final InvocationHandler proxyEquals = (proxy, method, args) -> {
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

    // visible for test
    static <T> T newInstance(Class<T> klass, InvocationHandler ih) {
        Objects.requireNonNull(klass);
        Objects.requireNonNull(ih);
        return klass.cast(Proxy.newProxyInstance(klass.getClassLoader(), new Class<?>[]{klass}, ih));
    }

    private final Map<Method, InvocationHandler> map = new HashMap<>(4);
    private boolean useDefaultMethod;
    private Function<Method, InvocationHandler> otherwise;
    private Function<Method, ? extends Throwable> orThrow = method -> new AbstractMethodError(method.getName());

    public ProxyBuilder customize(Method method, InvocationHandler handler) {
        map.put(Objects.requireNonNull(method), Objects.requireNonNull(handler));
        return this;
    }

    public ProxyBuilder otherwise(Function<Method, InvocationHandler> otherwise) {
        this.otherwise = Objects.requireNonNull(otherwise);
        return this;
    }

    public ProxyBuilder otherwise(InvocationHandler otherwise) {
        Objects.requireNonNull(otherwise);
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
        this.orThrow = Objects.requireNonNull(orThrow);
        return this;
    }

    public ProxyBuilder useDefaultMethod() {
        if (!DefaultMethodInvoker.isAvailable()) {
            throw new IllegalStateException("default method invoker is not available");
        }
        this.useDefaultMethod = true;
        return this;
    }

    public InvocationHandler toInvocationHandler() {
        return new InvocationHandlerImpl(map, useDefaultMethod, otherwise, orThrow);
    }

    public <T> T newInstance(Class<T> klass) {
        return newInstance(klass, toInvocationHandler());
    }

    private static class InvocationHandlerImpl implements InvocationHandler {

        @SuppressWarnings("unchecked")
        private static <T extends Throwable> RuntimeException throwUnchecked(Throwable throwable) throws T {
            throw (T) throwable;
        }

        private final ConcurrentMap<Method, InvocationHandler> map;
        private final boolean useDefaultMethod;
        private final Function<Method, InvocationHandler> otherwise;
        private final Function<Method, ? extends Throwable> orThrow;

        InvocationHandlerImpl(Map<Method, InvocationHandler> map, boolean useDefaultMethod, Function<Method, InvocationHandler> otherwise, Function<Method, ? extends Throwable> orThrow) {
            this.map = new ConcurrentHashMap<>(map);
            this.useDefaultMethod = useDefaultMethod;
            this.otherwise = otherwise;
            this.orThrow = orThrow;
        }

        @SuppressWarnings("RedundantTypeArguments")
        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            return map.computeIfAbsent(method, m -> {
                if (useDefaultMethod && m.isDefault()) {
                    return DefaultMethodInvoker.getInstance(m);
                }
                final InvocationHandler handler;
                if (otherwise != null) {
                    handler = otherwise.apply(m);
                } else {
                    handler = null;
                }
                if (handler == null) {
                    throw InvocationHandlerImpl.<RuntimeException>throwUnchecked(orThrow.apply(m));
                }
                return handler;
            }).invoke(proxy, method, args);
        }
    }

}
