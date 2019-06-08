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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import static java.util.Objects.requireNonNull;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 * @author zhanhb
 */
@SuppressWarnings("WeakerAccess")
final class ProxyBuilder {

    private static final Method OBJECT_TO_STRING;
    private static final Method OBJECT_EQUALS;
    private static final Method OBJECT_HASH_CODE;

    private static final InvocationHandler objectToString = (proxy, __, args) -> proxy.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(proxy));
    private static final InvocationHandler objectHashCode = (proxy, __, args) -> System.identityHashCode(proxy);
    private static final InvocationHandler objectEquals = (proxy, __, args) -> proxy == args[0];

    private static final InvocationHandler proxyToString = (proxy, __, args) -> proxy.getClass().getName() + "@" + Integer.toHexString(System.identityHashCode(Proxy.getInvocationHandler(proxy)));
    private static final InvocationHandler proxyHashCode = (proxy, __, args) -> System.identityHashCode(Proxy.getInvocationHandler(proxy));
    private static final InvocationHandler proxySame = (proxy, __, args) -> {
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

    public static ProxyBuilder empty() {
        return new ProxyBuilder();
    }

    public static ProxyBuilder builder() {
        return empty().proxyMethods().defaultMethods();
    }

    public static ProxyBuilder identifier() {
        return empty().identifierMethods().defaultMethods();
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> RuntimeException throwUnchecked(Throwable throwable) throws T {
        throw (T) throwable;
    }

    private final Map<MethodKey, InvocationHandler> map = new HashMap<>(4);
    private boolean useDefaultMethod;
    private Function<Method, InvocationHandler> otherwise;
    private Function<Method, ? extends Throwable> orThrow = method -> new AbstractMethodError(method.getName());

    private ProxyBuilder() {
    }

    public ProxyBuilder customize(Method method, InvocationHandler handler) {
        map.put(MethodKey.of(requireNonNull(method)), requireNonNull(handler));
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

    public ProxyBuilder objectToString() {
        return toStringWith(objectToString);
    }

    public ProxyBuilder objectHashCode() {
        return hashCodeWith(objectHashCode);
    }

    public ProxyBuilder objectEquals() {
        return equalsWith(objectEquals);
    }

    public ProxyBuilder identifierMethods() {
        return objectEquals().objectHashCode().objectToString();
    }

    public ProxyBuilder proxyToString() {
        return toStringWith(proxyToString);
    }

    public ProxyBuilder proxyHashCode() {
        return hashCodeWith(proxyHashCode);
    }

    public ProxyBuilder equalsWithProxySame() {
        return equalsWith(proxySame);
    }

    public ProxyBuilder proxyMethods() {
        return equalsWithProxySame().proxyHashCode().proxyToString();
    }

    public ProxyBuilder defaultMethods(boolean useDefaultMethod) {
        this.useDefaultMethod = useDefaultMethod;
        return this;
    }

    public ProxyBuilder defaultMethods() {
        return defaultMethods(true);
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

    public InvocationHandler toInvocationHandler() {
        final ConcurrentMap<MethodKey, InvocationHandler> map = new ConcurrentHashMap<>(this.map);
        final boolean useDefaultMethod = this.useDefaultMethod;
        final Function<Method, InvocationHandler> otherwise = this.otherwise;
        final Function<Method, ? extends Throwable> orThrow = this.orThrow;
        return (proxy, method, args) -> map.computeIfAbsent(MethodKey.of(method), __ -> {
            try {
                if (useDefaultMethod && method.isDefault()) {
                    return DefaultMethodInvoker.getInstance(method);
                }
                final InvocationHandler handler = otherwise != null ? otherwise.apply(method) : null;
                if (handler == null) {
                    throw orThrow.apply(method);
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

    private static final class MethodKey {

        private static MethodKey of(Method method) {
            return new MethodKey(method.getReturnType(), method.getParameterTypes());
        }

        private final Class<?> returnType;
        private final Class<?>[] parameterTypes;

        private MethodKey(Class<?> returnType, Class<?>[] parameterTypes) {
            this.returnType = returnType;
            this.parameterTypes = parameterTypes;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 97 * hash + Objects.hashCode(this.returnType);
            hash = 97 * hash + Arrays.hashCode(this.parameterTypes);
            return hash;
        }

        @Override
        @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj != null && getClass() == obj.getClass()) {
                final MethodKey other = (MethodKey) obj;
                return Objects.equals(this.returnType, other.returnType)
                        && Arrays.equals(this.parameterTypes, other.parameterTypes);
            }
            return false;
        }

    }

}
