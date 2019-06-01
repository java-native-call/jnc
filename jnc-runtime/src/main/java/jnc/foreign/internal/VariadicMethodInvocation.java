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

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
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
        RECEIVER_MAP = map;
    }

    private static void add(Map<Class<?>, Receiver> map, Class<?> klass, Receiver receiver) {
        map.put(klass, receiver);
    }

    private final ParameterHandler<?>[] handlers;
    private final CallingConvention convention;
    private final Invoker<?> invoker;
    private final long function;
    private final InternalType retType;
    private final InternalType[] ptypes;
    private final Class<?> methodVariadicType;
    private final AnnotationContext variadicAnnotationContext;
    private final TypeFactory typeFactory;
    private final TypeHandlerFactory typeHandlerFactory;

    VariadicMethodInvocation(
            ParameterHandler<?>[] handlers,
            CallingConvention convention,
            Invoker<?> invoker,
            long function,
            InternalType retType,
            InternalType[] ptypes,
            Class<?> methodVariadicType,
            AnnotationContext variadicAnnotationContext,
            TypeFactory typeFactory,
            TypeHandlerFactory typeHandlerFactory) {
        this.handlers = handlers;
        this.convention = convention;
        this.invoker = invoker;
        this.function = function;
        this.retType = retType;
        this.ptypes = ptypes;
        this.methodVariadicType = methodVariadicType;
        this.variadicAnnotationContext = variadicAnnotationContext;
        this.typeFactory = typeFactory;
        this.typeHandlerFactory = typeHandlerFactory;
    }

    @SuppressWarnings("AssignmentToMethodParameter")
    private void put(
            Object[] values, InternalType[] paramTypes, ParameterHandler<?>[] h, int index,
            Object value, List<Class<? extends Annotation>> annotations) {
        TypeHandlerInfo<? extends ParameterHandler<?>> parameterTypeInfo;
        if (value == null) {
            if (methodVariadicType == Object.class) {
                parameterTypeInfo = typeHandlerFactory.findParameterTypeInfo(Void.class);
            } else {
                try {
                    parameterTypeInfo = typeHandlerFactory.findParameterTypeInfo(methodVariadicType);
                } catch (UnsupportedOperationException ex) {
                    throw new NullPointerException();
                }
            }
        } else {
            parameterTypeInfo = typeHandlerFactory.findParameterTypeInfo(value.getClass());
        }
        values[index] = value;
        InternalType infoType = parameterTypeInfo.getType(typeFactory, AnnotationContext.newMockContext(annotations, variadicAnnotationContext));
        paramTypes[index] = promotions(infoType);
        h[index] = parameterTypeInfo.getHandler();
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
    public Object invoke(Object proxy, Method method, Object[] args) {
        // length > 2 is assumed
        final int fixedArgs = args.length - 1;
        final Object variadics = args[fixedArgs];

        int variadicLen;
        int cur;
        InternalType[] paramTypes;
        ParameterHandler<?>[] h;
        Object[] values;

        if (variadics == null || (variadicLen = Array.getLength(variadics)) == 0) {
            cur = fixedArgs;
            paramTypes = ptypes;
            h = handlers;
            values = Arrays.copyOf(args, fixedArgs);
        } else {
            final int maybeTotalLength = fixedArgs + variadicLen;

            cur = fixedArgs;
            paramTypes = Arrays.copyOf(ptypes, maybeTotalLength);
            h = Arrays.copyOf(handlers, maybeTotalLength);
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
            ParameterHandler<Object> ph = (ParameterHandler<Object>) h[i];
            ph.handle(context, i, values[i]);
        }
        return context.invoke(invoker, function);
    }

    private interface Receiver {

        Object apply(Object array, int index);

    }

}
