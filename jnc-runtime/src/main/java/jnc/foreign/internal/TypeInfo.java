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

/**
 *
 * @author zhanhb
 */
class TypeInfo {

    private final long address;
    private final long info;

    TypeInfo(long address, long info) {
        this.address = address;
        this.info = info;
    }

    final long address() {
        return address;
    }

    final int size() {
        return (int) (info >>> 32);
    }

    final int alignment() {
        return (int) (info >> 16) & 0xFFFF;
    }

    final int type() {
        return (int) info & 0xFFFF;
    }

}
