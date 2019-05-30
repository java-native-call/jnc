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
import java.util.Objects;
import java.util.Set;
import jnc.foreign.exception.JniLoadingException;

/**
 * @author zhanhb
 */
@SuppressWarnings("unused")
class DummyNativeMethod {

    static NativeAccessor createProxy(Throwable t) {
        return ProxyBuilder.builder().useProxyMethods()
                .otherwise(method -> {
                    try {
                        Method m = DummyNativeMethod.class.getMethod(method.getName(),
                                method.getParameterTypes());
                        return (proxy, method1, args) -> m.invoke(null, args);
                    } catch (NoSuchMethodException e) {
                        return null;
                    }
                })
                .orThrow(__ -> t instanceof UnsatisfiedLinkError ? new JniLoadingException(t) : t)
                .newInstance(NativeAccessor.class);
    }

    public static long getCifInfo() {
        return 0;
    }

    public static long getMethodId(Method method) {
        return 0;
    }

    public static Set<Runnable> onFinalize(Set<Runnable> set) {
        return Objects.requireNonNull(set);
    }

}
