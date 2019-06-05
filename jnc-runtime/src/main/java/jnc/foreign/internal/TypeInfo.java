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

import jnc.foreign.NativeType;

/**
 *
 * @author zhanhb
 */
final class TypeInfo implements InternalType {

    static final int MASK_SIGNED = 1;
    static final int MASK_INTEGRAL = 2;
    static final int MASK_FLOATING = 4;

    private final long address;
    private final long info;
    private final NativeType nativeType;
    private final int attr;

    TypeInfo(long address, long info, NativeType nativeType, int attr) {
        this.address = address;
        this.info = info;
        this.nativeType = nativeType;
        this.attr = attr;
    }

    @Override
    public NativeType nativeType() {
        return nativeType;
    }

    @Override
    public long address() {
        return address;
    }

    @Override
    public int size() {
        return (int) (info >>> 32);
    }

    @Override
    public int alignment() {
        return (int) (info >> 16) & 0xFFFF;
    }

    @Override
    public int type() {
        return (int) info & 0xFFFF;
    }

    @Override
    public boolean isSigned() {
        return (attr & MASK_SIGNED) != 0;
    }

    @Override
    public boolean isFloatingPoint() {
        return (attr & MASK_FLOATING) != 0;
    }

    @Override
    public boolean isIntegral() {
        return (attr & MASK_INTEGRAL) != 0;
    }

    @Deprecated
    @Override
    public void do_not_implement_this_for_its_used_internally() {
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + nativeType() + ')';
    }

}
