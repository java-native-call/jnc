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
package jnc.foreign;

/**
 * @param <T> java type to handle
 * @author zhanhb
 */
public interface TypeHandler<T> {

    NativeType nativeType();

    /**
     * Don't use this, currently internal use for enum only
     */
    T get(Pointer memory, int offset);

    /**
     * Don't use this, currently internal use for enum only
     */
    void set(Pointer memory, int offset, T value);

}
