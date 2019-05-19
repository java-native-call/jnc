/*
 * Copyright 2018 zhanhb.
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

public final class Padding extends Struct {

    Padding(int size) {
        this(size, 1);
    }

    Padding(int size, int alignment) {
        if ((alignment & alignment - 1) != 0) {
            throw new IllegalArgumentException("Illegal alignment " + alignment);
        }
        if (size < alignment) {
            throw new IllegalArgumentException("size is smaller than alignment: size=" + size + ",align=" + alignment);
        }
        addField(size, alignment);
    }

}
