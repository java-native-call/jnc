package jnc.foreign.typedef;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

public class TypedefTest {

    public static void main(String[] args) throws IOException {
        ArrayList<String> list = new ArrayList<>(20);
        /* maybe sizeof(long) = 4 or 8 */
        list.add("long");
        list.add("clock_t");
        /* enum on darwin */
        list.add("clockid_t");
        list.add("dev_t");
        /* use int instead */
        // DEFINE(errno_t)
        list.add("ino_t");
        // DEFINE(ino64_t)
        list.add("int16_t");
        list.add("int32_t");
        list.add("int64_t");
        list.add("int8_t");
        // DEFINE(intmax_t)
        list.add("intptr_t");
        list.add("mode_t");
        list.add("off_t");
        list.add("pid_t");
        list.add("ptrdiff_t");
        // DEFINE(rsize_t)
        list.add("size_t");
        list.add("socklen_t");
        list.add("ssize_t");
        list.add("time_t");
        list.add("uint16_t");
        list.add("uint32_t");
        list.add("uint64_t");
        list.add("uint8_t");
        // DEFINE(uintmax_t)
        list.add("uintptr_t");
        list.add("useconds_t");
        list.add("wchar_t");
        /*
         * linux: typedef int32_t *wctrans_t;
         * aix: typedef wint_t (*wctrans_t)();
         * solaris typedef unsigned int wctrans_t;
         * mingw typedef wchar_t wctrans_t;
         */
        list.add("wctrans_t");
        /* pointer type on OpenBSD */
        list.add("wctype_t");
        list.add("wint_t");
        String pkg = Typedef.class.getPackage().getName();
        for (String string : list) {
            String className = string;
            if ("int".equals(className) || "long".equals(className)) {
                className = "c" + className;
            }
            File file = new File("src/main/java/" + pkg.replace(".", "/") + "/" + className + ".java");
            try (PrintWriter pw = new PrintWriter(file, "UTF-8")) {
                pw.println("package " + pkg + ";");
                pw.println();
                pw.println("import java.lang.annotation.ElementType;");
                pw.println("import java.lang.annotation.Retention;");
                pw.println("import java.lang.annotation.RetentionPolicy;");
                pw.println("import java.lang.annotation.Target;");
                pw.println();
                pw.println("@Retention(RetentionPolicy.RUNTIME)");
                pw.println("@Target({ElementType.METHOD, ElementType.PARAMETER})");
                pw.println("@" + Typedef.class.getSimpleName() + "(\"" + string + "\")");
                pw.println("public @interface " + className + " {");
                pw.println("}");
            }
        }
    }

}
