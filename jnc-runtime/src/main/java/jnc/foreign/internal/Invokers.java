package jnc.foreign.internal;

import java.util.HashMap;
import java.util.Map;
import jnc.foreign.Pointer;
import static jnc.foreign.internal.LastErrorHandlers.*;

@SuppressWarnings("UtilityClassWithoutPrivateConstructor")
class Invokers {

    private static final NativeMethods nm = NativeMethods.getInstance();
    private static final Map<Class<?>, Invoker> MAP;

    static {
        Map<Class<?>, Invoker> map = new HashMap<>(16);
        map.put(void.class, Invokers::invokeVoid);
        map.put(boolean.class, Invokers::invokeBoolean);
        map.put(byte.class, Invokers::invokeByte);
        map.put(char.class, Invokers::invokeChar);
        map.put(short.class, Invokers::invokeShort);
        map.put(int.class, Invokers::invokeInt);
        map.put(long.class, Invokers::invokeLong);
        map.put(float.class, Invokers::invokeFloat);
        map.put(double.class, Invokers::invokeDouble);
        map.put(Pointer.class, Invokers::invokePointer);
        MAP = map;
    }

    static Invoker forInvoker(Class<?> returnType) {
        return MAP.get(Primitives.unwrap(returnType));
    }

    static long invokeLong(long cif, long function, long avalues) {
        return nm.invokeLong(cif, function, avalues, object(), methodId());
    }

    static int invokeInt(long cif, long function, long avalues) {
        return nm.invokeInt(cif, function, avalues, object(), methodId());
    }

    static boolean invokeBoolean(long cif, long function, long avalues) {
        return nm.invokeBoolean(cif, function, avalues, object(), methodId());
    }

    static byte invokeByte(long cif, long function, long avalues) {
        return (byte) nm.invokeInt(cif, function, avalues, object(), methodId());
    }

    static char invokeChar(long cif, long function, long avalues) {
        return (char) nm.invokeInt(cif, function, avalues, object(), methodId());
    }

    static short invokeShort(long cif, long function, long avalues) {
        return (short) nm.invokeInt(cif, function, avalues, object(), methodId());
    }

    static float invokeFloat(long cif, long function, long avalues) {
        return nm.invokeFloat(cif, function, avalues, object(), methodId());
    }

    static double invokeDouble(long cif, long function, long avalues) {
        return nm.invokeDouble(cif, function, avalues, object(), methodId());
    }

    static Void invokeVoid(long cif, long function, long avalues) {
        nm.invokeVoid(cif, function, avalues, object(), methodId());
        return null;
    }

    static Pointer invokePointer(long cif, long function, long avalues) {
        return DirectMemory.of(nm.invokeLong(cif, function, avalues, object(), methodId()));
    }

}
