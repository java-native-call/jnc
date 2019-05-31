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
import java.util.Objects;
import jnc.foreign.enums.CallingConvention;

/**
 * @author zhanhb
 */
final class VariadicMethodInvocation implements MethodInvocation {

    private static final Map<Class<?>, Reciever> RECIEVER_MAP;

    static {
        Map<Class<?>, Reciever> map = new HashMap<>(12);
        add(map, boolean[].class, Array::getBoolean);
        add(map, byte[].class, Array::getByte);
        add(map, short[].class, Array::getShort);
        add(map, char[].class, Array::getChar);
        add(map, int[].class, Array::getInt);
        add(map, long[].class, Array::getLong);
        add(map, float[].class, Array::getFloat);
        add(map, double[].class, Array::getDouble);
        RECIEVER_MAP = map;
    }

    private static void add(Map<Class<?>, Reciever> map, Class<?> klass, Reciever reciever) {
        map.put(klass, reciever);
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

    private void handle(
            Object[] values, InternalType[] paramTypes, ParameterHandler<?>[] h, int k,
            Object value, List<Class<? extends Annotation>> annotations) {
        TypeHandlerInfo<? extends ParameterHandler<?>> parameterTypeInfo;
        if (value == null) {
            parameterTypeInfo = typeHandlerFactory.findParameterTypeInfo(methodVariadicType);
        } else {
            parameterTypeInfo = typeHandlerFactory.findParameterTypeInfo(value.getClass());
        }
        values[k] = value;
        paramTypes[k] = parameterTypeInfo.getType(typeFactory, AnnotationContext.newMockContext(annotations, variadicAnnotationContext));
        h[k] = parameterTypeInfo.getHandler();
        annotations.clear();
    }

    @Override
    public CallingConvention getCallingConvention() {
        return convention;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) {
        // length > 2 is assumpted
        Object arg = Objects.requireNonNull(args[args.length - 1], "variadic should be an array, but got null");
        int variadicLen = Array.getLength(arg);
        int maybeTotalLength = args.length + variadicLen - 1;

        int k = args.length - 1;
        InternalType[] paramTypes = Arrays.copyOf(ptypes, maybeTotalLength);
        ParameterHandler<?>[] h = Arrays.copyOf(handlers, maybeTotalLength);
        Object[] values = Arrays.copyOf(args, maybeTotalLength, Object[].class);

        Reciever reciever = RECIEVER_MAP.getOrDefault(arg.getClass(), Array::get);

        List<Class<? extends Annotation>> annotations = new ArrayList<>(4);
        for (int i = 0; i < variadicLen; ++i) {
            Object value = reciever.apply(arg, i);
            if (value == null) {
                handle(values, paramTypes, h, k, value, annotations);
                ++k;
                continue;
            }
            if (value instanceof Class) {
                if (((Class) value).isAnnotation()) {
                    @SuppressWarnings("unchecked")
                    Class<? extends Annotation> as = (Class<? extends Annotation>) value;
                    annotations.add(as);
                    continue;
                }
                throw new IllegalArgumentException("unknown class");
            }
            handle(values, paramTypes, h, k, value, annotations);
            ++k;
        }

        if (k < maybeTotalLength) {
            paramTypes = Arrays.copyOf(paramTypes, k);
            h = Arrays.copyOf(h, k);
            values = Arrays.copyOf(values, k);
        }
        CallContext context = CifContainer.createVariadic(convention, k, retType, paramTypes).newCallContext();

        for (int i = 0; i < k; i++) {
            @SuppressWarnings("unchecked")
            ParameterHandler<Object> ph = (ParameterHandler<Object>) h[i];
            ph.handle(context, i, values[i]);
        }
        return context.invoke(invoker, function);
    }

    private interface Reciever {

        Object apply(Object array, int index);

    }

}
