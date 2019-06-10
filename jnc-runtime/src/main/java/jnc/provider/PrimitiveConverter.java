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
final class PrimitiveConverter {

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
    private static final int CID_VOID = 8;
    private static final int CID_CAPACITY = CID_VOID + 1;

    private final Map<NativeType, Integer> nativeToId;
    private final Map<Class<?>, Integer> classToId;
    private final RawConverter<?>[][] converters;

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
        class2Id.put(void.class, CID_VOID);
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
        RawConverter<?>[][] array = new RawConverter[CID_CAPACITY][NID_CAPACITY];

        for (int i = 0; i < NID_CAPACITY; i++) {
            array[CID_VOID][i] = __ -> null;
        }

        // There is no conversion needed from floating pointer type or void now.
        // If a method is annotated with @Typedef, then it's a integral type
        // uncomment these if actually needed.
        // Like floating alias or user customize conversion.
        // After comment these, the test case for this class will fail.
        //array[CID_BOOLEAN][NID_FLOAT] = x -> !(Float.intBitsToFloat((int) x) == 0);
        //array[CID_BOOLEAN][NID_DOUBLE] = x -> !(Double.longBitsToDouble(x) == 0);
        //array[CID_BOOLEAN][NID_VOID] = __ -> false;
        //
        //array[CID_BYTE][NID_FLOAT] = x -> (byte) (Float.intBitsToFloat((int) x));
        //array[CID_BYTE][NID_DOUBLE] = x -> (byte) (Double.longBitsToDouble(x));
        //array[CID_BYTE][NID_VOID] = __ -> (byte) 0;
        //
        //array[CID_SHORT][NID_FLOAT] = x -> (short) (Float.intBitsToFloat((int) x));
        //array[CID_SHORT][NID_DOUBLE] = x -> (short) (Double.longBitsToDouble(x));
        //array[CID_SHORT][NID_VOID] = __ -> (short) 0;
        //
        //array[CID_CHAR][NID_FLOAT] = x -> (char) (Float.intBitsToFloat((int) x));
        //array[CID_CHAR][NID_DOUBLE] = x -> (char) (Double.longBitsToDouble(x));
        //array[CID_CHAR][NID_VOID] = __ -> (char) 0;
        //
        //array[CID_INT][NID_FLOAT] = x -> (int) (Float.intBitsToFloat((int) x));
        //array[CID_INT][NID_DOUBLE] = x -> (int) (Double.longBitsToDouble(x));
        //array[CID_INT][NID_VOID] = __ -> 0;
        //
        //array[CID_LONG][NID_FLOAT] = x -> (long) (Float.intBitsToFloat((int) x));
        //array[CID_LONG][NID_DOUBLE] = x -> (long) (Double.longBitsToDouble(x));
        //array[CID_LONG][NID_VOID] = __ -> 0L;

        array[CID_FLOAT][NID_FLOAT] = x -> (Float.intBitsToFloat((int) x));
        //array[CID_FLOAT][NID_DOUBLE] = x -> (float) (Double.longBitsToDouble(x));
        //array[CID_FLOAT][NID_VOID] = __ -> 0f;

        //array[CID_DOUBLE][NID_FLOAT] = x -> (double) Float.intBitsToFloat((int) x);
        array[CID_DOUBLE][NID_DOUBLE] = Double::longBitsToDouble;
        //array[CID_DOUBLE][NID_VOID] = __ -> 0d;

        for (int i = 0; i < 8; i++) {
            array[CID_BOOLEAN][i] = x -> x != 0;
            array[CID_BYTE][i] = x -> (byte) x;
            array[CID_SHORT][i] = x -> (short) x;
            array[CID_CHAR][i] = x -> (char) x;
            array[CID_INT][i] = x -> (int) x;
            array[CID_LONG][i] = x -> x;
            array[CID_FLOAT][i] = x -> (float) x;
            array[CID_DOUBLE][i] = x -> (double) x;
        }

        this.converters = array;
    }

    // Notice: Pointer is not allowed here.
    // klass: accept both primitive types and their wrap types.
    // we only process type with actual size.
    <T> Function<NativeType, RawConverter<T>> getConverters(Class<T> klass) {
        Class<T> unwrap = Primitives.unwrap(klass);
        if (!unwrap.isPrimitive()) {
            throw new IllegalArgumentException();
        }
        // we have check primary type already, cid should not be null
        int cid = classToId.get(unwrap);
        @SuppressWarnings("unchecked")
        RawConverter<T>[] funs = (RawConverter<T>[]) converters[cid];
        return new ConverterHolder<>(nativeToId, funs);
    }

    private static final class ConverterHolder<T> implements Function<NativeType, RawConverter<T>> {

        private final Map<NativeType, Integer> nativeToId;
        private final RawConverter<T>[] converters;

        ConverterHolder(Map<NativeType, Integer> nativeToId, RawConverter<T>[] functions) {
            this.nativeToId = nativeToId;
            this.converters = functions;
        }

        @Override
        public RawConverter<T> apply(NativeType type) {
            Objects.requireNonNull(type);
            Integer nid = nativeToId.get(type);
            if (nid == null) {
                throw new IllegalArgumentException();
            }
            return converters[nid];
        }

    }

}
