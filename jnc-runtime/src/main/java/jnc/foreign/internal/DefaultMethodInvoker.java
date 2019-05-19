package jnc.foreign.internal;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/*use jdk7*/
@SuppressWarnings("UtilityClassWithoutPrivateConstructor")
class DefaultMethodInvoker {

    private static final Lookup IMPL_LOOPUP;

    static {
        Lookup lookup = null;
        try {
            lookup = findLookup1();
        } catch (Exception ex) {
            try {
                lookup = findLookup2();
            } catch (Exception ignored) {
            }
        }
        IMPL_LOOPUP = lookup;
    }

    private static Lookup findLookup1() throws Exception {
        Field field = Lookup.class.getDeclaredField("IMPL_LOOKUP");
        field.setAccessible(true);
        return (Lookup) field.get(null);
    }

    private static Lookup findLookup2() throws Exception {
        Constructor<Lookup> constructor = Lookup.class.getDeclaredConstructor(Class.class, int.class);
        constructor.setAccessible(true);
        return constructor.newInstance(Object.class, -1);
    }

    static InvocationHandler getInstance(Method method) {
        try {
            MethodHandle handler = IMPL_LOOPUP.unreflectSpecial(method, method.getDeclaringClass());
            return (Object proxy, Method unused, Object[] args) -> handler.bindTo(proxy).invokeWithArguments(args);
        } catch (IllegalAccessException ex) {
            throw new AssertionError(ex);
        }
    }

    static boolean isAvailable() {
        return IMPL_LOOPUP != null;
    }

}
