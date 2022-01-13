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

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jnc.foreign.NativeType;
import jnc.foreign.enums.CallingConvention;
import jnc.foreign.enums.TypeAlias;

/**
 * @author zhanhb
 */
final class VariadicMethodInvocation implements MethodInvocation {

    private static final Map<Class<?>, Receiver> RECEIVER_MAP;

    static {
        Map<Class<?>, Receiver> map = new HashMap<>(12);
        add(map, boolean[].class, Array::getBoolean);
        add(map, byte[].class, Array::getByte);
        add(map, short[].class, Array::getShort);
        add(map, char[].class, Array::getChar);
        add(map, int[].class, Array::getInt);
        add(map, long[].class, Array::getLong);
        add(map, float[].class, Array::getFloat);
        add(map, double[].class, Array::getDouble);
        RECEIVER_MAP = Collections.unmodifiableMap(map);
    }

    private static void add(Map<Class<?>, Receiver> map, Class<?> klass, Receiver receiver) {
        map.put(klass, receiver);
    }

    private final ParameterPutter<?>[] putters;
    private final CallingConvention convention;
    private final RawConverter<?> rawConverter;
    private final long function;
    private final InternalType retType;
    private final InternalType[] ptypes;
    private final Class<?> methodVariadicType;
    private final AnnotationContext annotationContext;
    private final TypeFactory typeFactory;
    private final TypeHandlerFactory typeHandlerFactory;

    VariadicMethodInvocation(
            ParameterPutter<?>[] putters,
            CallingConvention convention,
            RawConverter<?> rawConverter,
            long function,
            InternalType retType,
            InternalType[] ptypes,
            Class<?> methodVariadicType,
            AnnotationContext variadicAnnotationContext,
            TypeFactory typeFactory,
            TypeHandlerFactory typeHandlerFactory) {
        this.putters = putters;
        this.convention = convention;
        this.rawConverter = rawConverter;
        this.function = function;
        this.retType = retType;
        this.ptypes = ptypes;
        this.methodVariadicType = methodVariadicType == Object.class ? Void.class : methodVariadicType;
        this.annotationContext = variadicAnnotationContext;
        this.typeFactory = typeFactory;
        this.typeHandlerFactory = typeHandlerFactory;
    }

    @SuppressWarnings("AssignmentToMethodParameter")
    private void put(
            Object[] values, InternalType[] paramTypes, ParameterPutter<?>[] h, int index,
            Object value, List<Class<? extends Annotation>> annotations) {
        ParameterHandlerInfo info;
        Class<?> type;
        if (value == null) {
            try {
                type = methodVariadicType;
                info = typeHandlerFactory.findParameterTypeInfo(type);
            } catch (UnsupportedOperationException ex) {
                throw new NullPointerException();
            }
        } else {
            type = value.getClass();
            info = typeHandlerFactory.findParameterTypeInfo(type);
        }
        AnnotationContext ac = AnnotationContext.newMockContext(annotations, annotationContext);
        values[index] = value;
        InternalType infoType = info.getType(type, typeFactory, ac);
        paramTypes[index] = promotions(infoType);
        h[index] = info.getPutter(type);
        annotations.clear();
    }

    // [u]int8/16->int, float->double
    // https://en.cppreference.com/w/c/language/variadic
    private InternalType promotions(InternalType infoType) {
        switch (infoType.nativeType()) {
            case SINT8:
            case UINT8:
            case SINT16:
            case UINT16:
                return typeFactory.findByAlias(TypeAlias.cint);
            case FLOAT:
                return typeFactory.findByNativeType(NativeType.DOUBLE);
        }
        return infoType;
    }

    @Override
    public CallingConvention getCallingConvention() {
        return convention;
    }

    @Override
    @SuppressWarnings("NestedAssignment")
    public Object invoke(Object proxy, Object[] args) {
        // length > 2 is assumed
        final int fixedArgs = args.length - 1;
        final Object variadics = args[fixedArgs];

        int variadicLen;
        int cur;
        InternalType[] paramTypes;
        ParameterPutter<?>[] h;
        Object[] values;

        if (variadics == null || (variadicLen = Array.getLength(variadics)) == 0) {
            cur = fixedArgs;
            paramTypes = ptypes;
            h = putters;
            values = Arrays.copyOf(args, fixedArgs);
        } else {
            final int maybeTotalLength = fixedArgs + variadicLen;

            cur = fixedArgs;
            paramTypes = Arrays.copyOf(ptypes, maybeTotalLength);
            h = Arrays.copyOf(putters, maybeTotalLength);
            values = Arrays.copyOf(args, maybeTotalLength, Object[].class);

            final Receiver receiver = RECEIVER_MAP.getOrDefault(variadics.getClass(), Array::get);

            final List<Class<? extends Annotation>> annotations = new ArrayList<>(4);
            for (int i = 0; i < variadicLen; ++i) {
                Object value = receiver.apply(variadics, i);
                if (value == null) {
                    put(values, paramTypes, h, cur, null, annotations);
                    ++cur;
                    continue;
                }
                if (value instanceof Class) {
                    if (((Class<?>) value).isAnnotation()) {
                        annotations.add(((Class<?>) value).asSubclass(Annotation.class));
                        continue;
                    }
                }
                put(values, paramTypes, h, cur, value, annotations);
                ++cur;
            }

            if (cur < maybeTotalLength) {
                paramTypes = Arrays.copyOf(paramTypes, cur);
                h = Arrays.copyOf(h, cur);
                values = Arrays.copyOf(values, cur);
            }
        }

        CallContext context = CifContainer.createVariadic(convention, fixedArgs, retType, paramTypes).newCallContext();

        for (int i = 0; i < cur; i++) {
            @SuppressWarnings("unchecked")
            ParameterPutter<Object> ph = (ParameterPutter<Object>) h[i];
            ph.doPut(context, i, values[i]);
        }
        return context.invoke(rawConverter, function);
    }

    private interface Receiver {

        Object apply(Object array, int index);

    }

}
