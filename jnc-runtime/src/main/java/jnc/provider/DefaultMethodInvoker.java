package jnc.provider;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodHandles.Lookup;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Handle invoke_special for interface methods default methods
 *
 * @author zhanhb
 * @see
 * <a href="https://github.com/spring-projects/spring-data-commons/blob/2.1.8.RELEASE/src/main/java/org/springframework/data/projection/DefaultMethodInvokingMethodInterceptor.java">Spring
 * Data DefaultMethodInvokingMethodInterceptor.java</a>
 */
@SuppressWarnings("UtilityClassWithoutPrivateConstructor")
final class DefaultMethodInvoker {

    private static final DefaultMethodUnreflector unreflector = createUnreflector();
    private static final Class<? extends RuntimeException> inaccessibleObject;

    static {
        Class<? extends RuntimeException> ioe = null;
        try {
            ioe = Class.forName("java.lang.reflect.InaccessibleObjectException").asSubclass(RuntimeException.class);
        } catch (ClassNotFoundException ignored) {
        }
        inaccessibleObject = ioe;
    }

    private static <T> T unboxInvocationTarget(IMethod<T> iMethod, Object... args) throws Throwable {
        try {
            return iMethod.invoke(args);
        } catch (InvocationTargetException e) {
            throw e.getCause();
        }
    }

    private static Lookup getLookup(Class<?> declaringClass, Method privateLookupIn)
            throws Throwable {
        IMethod<Lookup> iMethod = args -> (Lookup) privateLookupIn.invoke(null, args);
        return unboxInvocationTarget(iMethod, declaringClass, MethodHandles.lookup());
    }

    private static MethodHandle doLookup(Method method, Lookup lookup)
            throws NoSuchMethodException, IllegalAccessException {
        MethodType methodType = MethodType.methodType(method.getReturnType(), method.getParameterTypes());
        return lookup.findSpecial(method.getDeclaringClass(), method.getName(), methodType, method.getDeclaringClass());
    }

    @SuppressWarnings({"BroadCatchBlock", "TooBroadCatch"})
    private static DefaultMethodUnreflector createUnreflector() {
        try {
            Method privateLookupIn = MethodHandles.class.getMethod("privateLookupIn", Class.class, Lookup.class);
            return method -> doLookup(method, getLookup(method.getDeclaringClass(), privateLookupIn));
        } catch (NoSuchMethodException ignore) {
        }
        try {
            Constructor<Lookup> constructor = Lookup.class.getDeclaredConstructor(Class.class);
            constructor.setAccessible(true);
            return method -> {
                Class<?> declaringClass = method.getDeclaringClass();
                return unboxInvocationTarget(constructor::newInstance, declaringClass)
                        .unreflectSpecial(method, declaringClass);
            };
        } catch (NoSuchMethodException | SecurityException ignored) {
        } catch (RuntimeException ex) {
            if (inaccessibleObject == null || !inaccessibleObject.isInstance(ex)) {
                throw ex;
            }
        }
        return method -> doLookup(method, MethodHandles.lookup());
    }

    static InvocationHandler getInstance(Method method) throws Throwable {
        MethodHandle handle = unreflector.unreflectSpecial(method);
        return (proxy, __, args) -> handle.bindTo(proxy).invokeWithArguments(args);
    }

    private interface IMethod<T> {

        T invoke(Object[] args) throws IllegalAccessException, InvocationTargetException, InstantiationException;
    }

    private interface DefaultMethodUnreflector {

        MethodHandle unreflectSpecial(Method method) throws Throwable;

    }

}
