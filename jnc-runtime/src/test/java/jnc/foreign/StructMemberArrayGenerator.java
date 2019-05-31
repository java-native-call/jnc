package jnc.foreign;

import java.io.PrintStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

public class StructMemberArrayGenerator {

    public static void main(String[] args) {
        Class<?>[] declaredClasses = Struct.class.getDeclaredClasses();
        List<Class<?>> list = new ArrayList<>(declaredClasses.length);
        for (Class<?> inner : declaredClasses) {
            if (!inner.isAnonymousClass() && (inner.getModifiers() & (Modifier.PRIVATE | Modifier.STATIC)) == 0) {
                list.add(inner);
            }
        }
        PrintStream out = System.out;
        for (int i = list.size() - 1; i >= 0; --i) {
            String s = list.get(i).getSimpleName();
            out.println("@Nonnull");
            out.println("protected final " + s + "[] array(@Nonnull " + s + "[] array) {");
            out.println("    return memberArray(array, struct -> struct.new " + s + "());");
            out.println("}");
        }
    }

}
