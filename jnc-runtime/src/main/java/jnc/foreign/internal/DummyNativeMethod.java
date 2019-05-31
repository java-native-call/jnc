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

import jnc.foreign.exception.JniLoadingException;

/**
 * @author zhanhb
 */
interface DummyNativeMethod {

    static NativeAccessor createProxy(Throwable t) {
        return ProxyBuilder.builder()
                .orThrow(__ -> t instanceof UnsatisfiedLinkError ? new JniLoadingException(t) : t)
                .newInstance(NativeAccessor.class);
    }

}
