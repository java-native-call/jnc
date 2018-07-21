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
        // list.add("pointer"); // should not be used, just for test
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
        list.add("blkcnt_t");
        list.add("blksize_t");
        list.add("fsblkcnt_t");
        list.add("fsfilcnt_t");
        list.add("gid_t");
        list.add("id_t");
        list.add("in_addr_t");
        list.add("in_port_t");
        list.add("key_t");
        /* not an integer type */
        // DEFINE(mbstate_t)
        list.add("nlink_t");
        list.add("rlim_t");
        list.add("sa_family_t");
        /* maybe not an integer type */
        /* https://www.gnu.org/software/libc/manual/html_node/Signal-Sets.html */
        // DEFINE(sigset_t)
        list.add("suseconds_t");
        list.add("uid_t");
        list.add("ct_rune_t");
        list.add("rune_t");
        list.add("sae_associd_t");
        list.add("sae_connid_t");
        // DEFINE(swblk_t)
        list.add("syscall_arg_t");
        list.add("user_addr_t");
        list.add("user_long_t");
        list.add("user_off_t");
        list.add("user_size_t");
        list.add("user_ssize_t");
        list.add("user_time_t");
        list.add("user_ulong_t");
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
