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

import java.util.HashMap;
import java.util.Map;
import jnc.foreign.NativeType;
import jnc.foreign.Pointer;
import static jnc.foreign.internal.LastErrorHandlers.methodId;
import static jnc.foreign.internal.LastErrorHandlers.object;

/**
 * @author zhanhb
 */
class PrimaryTypeHandler<T> implements InternalTypeHandler<T> {

    private static final NativeMethods nm = NativeMethods.getInstance();
    private static final Map<Class<?>, PrimaryTypeHandler<?>> TYPE_HANDLERS;

    static {
        Map<Class<?>, PrimaryTypeHandler<?>> map = new HashMap<>(16);
        ParameterHandler<Void> voidParameterHandler = (context, index, obj) -> context.putLong(index, 0);
        add(map, void.class, NativeType.VOID, voidParameterHandler, PrimaryTypeHandler::invokeVoid);
        add(map, boolean.class, NativeType.UINT8, CallContext::putBoolean, PrimaryTypeHandler::invokeBoolean);
        add(map, byte.class, NativeType.SINT8, CallContext::putByte, PrimaryTypeHandler::invokeByte);
        add(map, char.class, NativeType.UINT16, CallContext::putChar, PrimaryTypeHandler::invokeChar);
        add(map, short.class, NativeType.SINT16, CallContext::putShort, PrimaryTypeHandler::invokeShort);
        add(map, int.class, NativeType.SINT32, CallContext::putInt, PrimaryTypeHandler::invokeInt);
        add(map, long.class, NativeType.SINT64, CallContext::putLong, PrimaryTypeHandler::invokeLong);
        add(map, float.class, NativeType.FLOAT, CallContext::putFloat, PrimaryTypeHandler::invokeFloat);
        add(map, double.class, NativeType.DOUBLE, CallContext::putDouble, PrimaryTypeHandler::invokeDouble);
        TYPE_HANDLERS = map;
    }

    private static <T> void add(Map<Class<?>, PrimaryTypeHandler<?>> map, Class<T> type, NativeType nativeType, ParameterHandler<T> handler, Invoker invoker) {
        map.put(type, new PrimaryTypeHandler<>(nativeType, invoker, handler));
    }

    private static long invokeLong(long cif, long function, long avalues) {
        return nm.invokeLong(cif, function, avalues, object(), methodId());
    }

    private static int invokeInt(long cif, long function, long avalues) {
        return nm.invokeInt(cif, function, avalues, object(), methodId());
    }

    private static boolean invokeBoolean(long cif, long function, long avalues) {
        return nm.invokeBoolean(cif, function, avalues, object(), methodId());
    }

    private static byte invokeByte(long cif, long function, long avalues) {
        return (byte) nm.invokeInt(cif, function, avalues, object(), methodId());
    }

    private static char invokeChar(long cif, long function, long avalues) {
        return (char) nm.invokeInt(cif, function, avalues, object(), methodId());
    }

    private static short invokeShort(long cif, long function, long avalues) {
        return (short) nm.invokeInt(cif, function, avalues, object(), methodId());
    }

    private static float invokeFloat(long cif, long function, long avalues) {
        return nm.invokeFloat(cif, function, avalues, object(), methodId());
    }

    private static double invokeDouble(long cif, long function, long avalues) {
        return nm.invokeDouble(cif, function, avalues, object(), methodId());
    }

    private static Void invokeVoid(long cif, long function, long avalues) {
        nm.invokeVoid(cif, function, avalues, object(), methodId());
        return null;
    }

    static <T> PrimaryTypeHandler<T> forType(Class<T> type) {
        // should be safe without check generic type
        @SuppressWarnings("unchecked")
        PrimaryTypeHandler<T> handler = (PrimaryTypeHandler<T>) TYPE_HANDLERS.get(Primitives.unwrap(type));
        return handler;
    }

    private final NativeType nativeType;
    private final Invoker invoker;
    private final ParameterHandler<T> parameterHandler;

    private PrimaryTypeHandler(NativeType nativeType, Invoker invoker, ParameterHandler<T> parameterHandler) {
        this.nativeType = nativeType;
        this.invoker = invoker;
        this.parameterHandler = parameterHandler;
    }

    @Override
    public NativeType nativeType() {
        return nativeType;
    }
    

    @Override
    public Invoker getInvoker() {
        return invoker;
    }

    @Override
    public ParameterHandler<T> getParameterHandler() {
        return parameterHandler;
    }

    @Deprecated
    @Override
    public BuiltinType getBuiltinType() {
        return BuiltinTypeHelper.findByNativeType(nativeType);
    }

    @Override
    public T get(Pointer memory, int offset) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void set(Pointer memory, int offset, T value) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
