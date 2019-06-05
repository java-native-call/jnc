package jnc.foreign.typedef;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import jnc.foreign.annotation.Typedef;
import jnc.foreign.enums.TypeAlias;

public class TypedefGenerator {

    private static final String PKG = size_t.class.getPackage().getName();
    private static final String BASEPATH = "src/main/java";

    public static void main(String[] args) throws IOException {
        write(null,
                /* maybe sizeof(long) = 4 or 8 */
                "long",
                "clock_t",
                /* enum on darwin */
                "clockid_t",
                "dev_t",
                "errno_t",
                // fpos_t is struct on linux
                // DEFINE(fpos_t)
                "ino_t",
                // DEFINE(ino64_t)
                "int16_t",
                "int32_t",
                "int64_t",
                "int8_t",
                "intptr_t",
                "mode_t",
                "off_t",
                "pid_t",
                "ptrdiff_t",
                // DEFINE(rsize_t)
                "size_t",
                "socklen_t",
                "ssize_t",
                "time_t",
                "uint16_t",
                "uint32_t",
                "uint64_t",
                "uint8_t",
                "uintptr_t",
                "useconds_t",
                "wchar_t",
                /*
                 * linux: typedef int32_t *wctrans_t;
                 * aix: typedef wint_t (*wctrans_t)();
                 * solaris typedef unsigned int wctrans_t;
                 * mingw typedef wchar_t wctrans_t;
                 */
                "wctrans_t",
                /* pointer type on OpenBSD */
                "wctype_t",
                "wint_t"
        );
        write("unix",
                "blkcnt_t",
                "blksize_t",
                "fsblkcnt_t",
                "fsfilcnt_t",
                "gid_t",
                "id_t",
                "in_addr_t",
                "in_port_t",
                "key_t",
                /* not an integer type */
                // DEFINE(mbstate_t)
                "nlink_t",
                "rlim_t",
                "sa_family_t",
                /* maybe not an integer type */
                /* https://www.gnu.org/software/libc/manual/html_node/Signal-Sets.html */
                // DEFINE(sigset_t)
                "suseconds_t",
                "uid_t"
        );
        write("bsd",
                "register_t",
                "segsz_t"
        );
        write("osx",
                "ct_rune_t",
                "rune_t",
                "sae_associd_t",
                "sae_connid_t",
                "swblk_t",
                "syscall_arg_t",
                "user_addr_t",
                "user_long_t",
                "user_off_t",
                "user_size_t",
                "user_ssize_t",
                "user_time_t",
                "user_ulong_t"
        );
    }

    private static void write(String subPackage, String... typeNames) throws IOException {
        for (String cname : typeNames) {
            writeImpl(cname, subPackage == null ? PKG : PKG + "." + subPackage);
        }
    }

    private static void writeImpl(String cname, String pkg) throws IOException {
        String jName = cname;
        if ("int".equals(jName) || "long".equals(jName)) {
            jName = "c" + jName;
        }
        Path dir = Paths.get(BASEPATH, pkg.replace('.', '/'));
        if (!Files.exists(dir)) {
            Files.createDirectory(dir);
        }
        try (PrintWriter pw = new PrintWriter(dir.resolve(jName + ".java").toFile(), "UTF-8")) {
            pw.println("package " + pkg + ";");
            pw.println();
            pw.println("import java.lang.annotation.Documented;");
            pw.println("import java.lang.annotation.ElementType;");
            pw.println("import java.lang.annotation.Retention;");
            pw.println("import java.lang.annotation.RetentionPolicy;");
            pw.println("import java.lang.annotation.Target;");
            pw.println("import " + Typedef.class.getName() + ";");
            pw.println("import " + TypeAlias.class.getName() + ";");
            pw.println();
            pw.println("@Documented");
            pw.println("@Retention(RetentionPolicy.RUNTIME)");
            pw.println("@Target({ElementType.METHOD, ElementType.PARAMETER})");
            pw.println("@Typedef(TypeAlias." + jName + ")");
            pw.println("public @interface " + jName + " {");
            pw.println("}");
        }
    }

}
