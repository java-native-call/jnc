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

import javax.annotation.Nullable;
import jnc.foreign.Pointer;

/**
 * @author zhanhb
 */
@SuppressWarnings("UtilityClassWithoutPrivateConstructor")
final class Invokers {

    private static final NativeAccessor NA = NativeLoader.getAccessor();

    static Pointer invokePointer(long cif, long function, long base, @Nullable int[] offsets) {
        return UnboundedDirectMemory.of(invokeLong(cif, function, base, offsets));
    }

    static long invokeLong(long cif, long function, long base, @Nullable int[] offsets) {
        return NA.invokeLong(cif, function, base, offsets, ThreadLocalError.getInstance(), LastErrorHandler.METHOD_ID);
    }

    static int invokeInt(long cif, long function, long base, @Nullable int[] offsets) {
        return NA.invokeInt(cif, function, base, offsets, ThreadLocalError.getInstance(), LastErrorHandler.METHOD_ID);
    }

    static boolean invokeBoolean(long cif, long function, long base, @Nullable int[] offsets) {
        return NA.invokeBoolean(cif, function, base, offsets, ThreadLocalError.getInstance(), LastErrorHandler.METHOD_ID);
    }

    static byte invokeByte(long cif, long function, long base, @Nullable int[] offsets) {
        return (byte) invokeInt(cif, function, base, offsets);
    }

    static char invokeChar(long cif, long function, long base, @Nullable int[] offsets) {
        return (char) invokeInt(cif, function, base, offsets);
    }

    static short invokeShort(long cif, long function, long base, @Nullable int[] offsets) {
        return (short) invokeInt(cif, function, base, offsets);
    }

    static float invokeFloat(long cif, long function, long base, @Nullable int[] offsets) {
        return (float) invokeDouble(cif, function, base, offsets);
    }

    static double invokeDouble(long cif, long function, long base, @Nullable int[] offsets) {
        return NA.invokeDouble(cif, function, base, offsets, ThreadLocalError.getInstance(), LastErrorHandler.METHOD_ID);
    }

    static Void invokeVoid(long cif, long function, long base, @Nullable int[] offsets) {
        NA.invokeVoid(cif, function, base, offsets, ThreadLocalError.getInstance(), LastErrorHandler.METHOD_ID);
        return null;
    }

}
