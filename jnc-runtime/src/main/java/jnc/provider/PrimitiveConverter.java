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

import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import jnc.foreign.NativeType;

/**
 * @author zhanhb
 */
enum PrimitiveConverter {
    INSTANCE;

    private static final int NID_SINT8 = 0;
    private static final int NID_UINT8 = 1;
    private static final int NID_SINT16 = 2;
    private static final int NID_UINT16 = 3;
    private static final int NID_SINT32 = 4;
    private static final int NID_UINT32 = 5;
    private static final int NID_SINT64 = 6;
    private static final int NID_UINT64 = 7;
    private static final int NID_FLOAT = 8;
    private static final int NID_DOUBLE = 9;
    private static final int NID_VOID = 10;
    private static final int NID_CAPACITY = NID_VOID + 1;

    private static final int CID_BYTE = 0;
    private static final int CID_SHORT = 1;
    private static final int CID_INT = 2;
    private static final int CID_LONG = 3;
    private static final int CID_FLOAT = 4;
    private static final int CID_DOUBLE = 5;
    private static final int CID_BOOLEAN = 6;
    private static final int CID_CHAR = 7;
    private static final int CID_CAPACITY = CID_CHAR + 1;

    private static <T> void add(
            @SuppressWarnings("unused") Class<T> unused,
            InvokeHandler<?>[] array,
            int nid,
            InvokeHandler<T> fun) {
        array[nid] = fun;
    }

    private final Map<NativeType, Integer> nativeToId;
    private final Map<Class<?>, Integer> classToId;
    private final InvokeHandler<?>[][] functions;

    {
        EnumMap<NativeType, Integer> native2Id = new EnumMap<>(NativeType.class);
        native2Id.put(NativeType.SINT8, NID_SINT8);
        native2Id.put(NativeType.UINT8, NID_UINT8);
        native2Id.put(NativeType.SINT16, NID_SINT16);
        native2Id.put(NativeType.UINT16, NID_UINT16);
        native2Id.put(NativeType.SINT32, NID_SINT32);
        native2Id.put(NativeType.UINT32, NID_UINT32);
        native2Id.put(NativeType.SINT64, NID_SINT64);
        native2Id.put(NativeType.UINT64, NID_UINT64);
        native2Id.put(NativeType.FLOAT, NID_FLOAT);
        native2Id.put(NativeType.DOUBLE, NID_DOUBLE);
        native2Id.put(NativeType.VOID, NID_VOID);
        nativeToId = Collections.unmodifiableMap(native2Id);
    }

    {
        Map<Class<?>, Integer> class2Id = new HashMap<>(16);
        class2Id.put(boolean.class, CID_BOOLEAN);
        class2Id.put(byte.class, CID_BYTE);
        class2Id.put(short.class, CID_SHORT);
        class2Id.put(char.class, CID_CHAR);
        class2Id.put(int.class, CID_INT);
        class2Id.put(long.class, CID_LONG);
        class2Id.put(float.class, CID_FLOAT);
        class2Id.put(double.class, CID_DOUBLE);
        this.classToId = Collections.unmodifiableMap(class2Id);
    }

    {
        @SuppressWarnings("rawtypes")
        InvokeHandler<?>[][] funs = new InvokeHandler[CID_CAPACITY][NID_CAPACITY];

        add(boolean.class, funs[CID_BOOLEAN], NID_FLOAT, x -> !(Float.intBitsToFloat((int) x) == 0));
        add(boolean.class, funs[CID_BOOLEAN], NID_DOUBLE, x -> !(Double.longBitsToDouble(x) == 0));
        add(boolean.class, funs[CID_BOOLEAN], NID_VOID, __ -> false);

        add(byte.class, funs[CID_BYTE], NID_FLOAT, x -> (byte) (Float.intBitsToFloat((int) x)));
        add(byte.class, funs[CID_BYTE], NID_DOUBLE, x -> (byte) (Double.longBitsToDouble(x)));
        add(byte.class, funs[CID_BYTE], NID_VOID, __ -> (byte) 0);

        add(short.class, funs[CID_SHORT], NID_FLOAT, x -> (short) (Float.intBitsToFloat((int) x)));
        add(short.class, funs[CID_SHORT], NID_DOUBLE, x -> (short) (Double.longBitsToDouble(x)));
        add(short.class, funs[CID_SHORT], NID_VOID, __ -> (short) 0);

        add(char.class, funs[CID_CHAR], NID_FLOAT, x -> (char) (Float.intBitsToFloat((int) x)));
        add(char.class, funs[CID_CHAR], NID_DOUBLE, x -> (char) (Double.longBitsToDouble(x)));
        add(char.class, funs[CID_CHAR], NID_VOID, __ -> (char) 0);

        add(int.class, funs[CID_INT], NID_FLOAT, x -> (int) (Float.intBitsToFloat((int) x)));
        add(int.class, funs[CID_INT], NID_DOUBLE, x -> (int) (Double.longBitsToDouble(x)));
        add(int.class, funs[CID_INT], NID_VOID, __ -> 0);

        add(long.class, funs[CID_LONG], NID_FLOAT, x -> (long) (Float.intBitsToFloat((int) x)));
        add(long.class, funs[CID_LONG], NID_DOUBLE, x -> (long) (Double.longBitsToDouble(x)));
        add(long.class, funs[CID_LONG], NID_VOID, __ -> 0L);

        add(float.class, funs[CID_FLOAT], NID_FLOAT, x -> (Float.intBitsToFloat((int) x)));
        add(float.class, funs[CID_FLOAT], NID_DOUBLE, x -> (float) (Double.longBitsToDouble(x)));
        add(float.class, funs[CID_FLOAT], NID_VOID, __ -> 0f);

        add(double.class, funs[CID_DOUBLE], NID_FLOAT, x -> (double) Float.intBitsToFloat((int) x));
        add(double.class, funs[CID_DOUBLE], NID_DOUBLE, x -> (Double.longBitsToDouble(x)));
        add(double.class, funs[CID_DOUBLE], NID_VOID, __ -> 0d);

        for (int i = 0; i < 8; i++) {
            add(boolean.class, funs[CID_BOOLEAN], i, x -> x != 0);
            add(byte.class, funs[CID_BYTE], i, x -> (byte) x);
            add(short.class, funs[CID_SHORT], i, x -> (short) x);
            add(char.class, funs[CID_CHAR], i, x -> (char) x);
            add(int.class, funs[CID_INT], i, x -> (int) x);
            add(long.class, funs[CID_LONG], i, x -> x);
            add(float.class, funs[CID_FLOAT], i, x -> (float) x);
            add(double.class, funs[CID_DOUBLE], i, x -> (double) x);
        }

        this.functions = funs;
    }

    // Notice: Pointer is not allowed here.
    // klass: accept both primitive types and their wrap types.
    // we only process type with actual size.
    public <T> Function<NativeType, InvokeHandler<T>> getInvokerConvertors(Class<T> klass) {
        Class<T> unwrap = Primitives.unwrap(klass);
        if (!unwrap.isPrimitive()) {
            throw new IllegalArgumentException();
        }
        if (unwrap == void.class) {
            return type -> {
                // make it same behaviour as others.
                Objects.requireNonNull(type);
                return __ -> null;
            };
        }
        // we have check primary type alreay, cid should not be null
        int cid = classToId.get(unwrap);
        @SuppressWarnings("unchecked")
        InvokeHandler<T>[] funs = (InvokeHandler<T>[]) functions[cid];
        return new FunctionHolder<>(nativeToId, funs);
    }

    private static final class FunctionHolder<T> implements Function<NativeType, InvokeHandler<T>> {

        private final Map<NativeType, Integer> nativeToId;
        private final InvokeHandler<T>[] functions;

        FunctionHolder(Map<NativeType, Integer> nativeToId, InvokeHandler<T>[] functions) {
            this.nativeToId = nativeToId;
            this.functions = functions;
        }

        @Override
        public InvokeHandler<T> apply(NativeType type) {
            Objects.requireNonNull(type);
            Integer nid = nativeToId.get(type);
            if (nid == null) {
                throw new IllegalArgumentException();
            }
            return functions[nid];
        }

    }

}
