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

import java.io.File;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.ProtectionDomain;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javax.annotation.Nullable;
import jnc.foreign.Struct;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhanhb
 */
@RunWith(Parameterized.class)
public class NotFinalTest {

    private static final Logger log = LoggerFactory.getLogger(NotFinalTest.class);
    private static final int SYNTHETIC = 0x00001000;

    private static boolean isSynthetic(int mod) {
        return (mod & SYNTHETIC) != 0;
    }

    private static void visitMethods(List<Object[]> list, Class<?> klass) {
        Method[] methods = klass.getDeclaredMethods();
        for (Method method : methods) {
            RequireType requireType;
            int modifiers = method.getModifiers();
            if (isSynthetic(modifiers)) {
                continue;
            }
            if (Modifier.isStatic(modifiers) || Modifier.isPrivate(modifiers)) {
                requireType = RequireType.LOG_DEBUG;
            } else if (Modifier.isFinal(modifiers)) {
                requireType = RequireType.NO_ACTION;
            } else if (Modifier.isAbstract(modifiers)) {
                requireType = RequireType.SHOULD_NOT_HAVE;
            } else {
                requireType = RequireType.SHOULD_HAVE;
            }
            list.add(new Object[]{requireType, method});
        }
    }

    @Parameterized.Parameters(name = "{index} {1} {0}")
    public static List<Object[]> data() throws Exception {
        ProtectionDomain protectionDomain = Struct.class.getProtectionDomain();
        URL location = protectionDomain.getCodeSource().getLocation();
        Path directory = Paths.get(location.toURI());
        List<Class<?>> classes = Files.walk(directory)
                .filter(path -> path.getFileName().toString().endsWith(".class"))
                .map(path -> directory.relativize(path).toString())
                .map(str -> str.replaceAll("\\.class$", "").replace(File.separatorChar, '.'))
                .map(name -> {
                    try {
                        return Class.forName(name);
                    } catch (ClassNotFoundException ex) {
                        throw new NoClassDefFoundError(name);
                    }
                })
                .filter(klass -> !isSynthetic(klass.getModifiers()))
                .collect(Collectors.toList());

        List<Object[]> list = new ArrayList<>(classes.size() * 4);
        for (Class<?> klass : classes) {
            RequireType requireType;
            int modifiers = klass.getModifiers();
            if (klass.isEnum() || klass.isInterface()) {
                requireType = RequireType.LOG_DEBUG;
            } else {
                if (Modifier.isAbstract(modifiers)) {
                    requireType = RequireType.SHOULD_NOT_HAVE;
                    visitMethods(list, klass);
                } else if (Modifier.isFinal(modifiers)) {
                    requireType = RequireType.NO_ACTION;
                } else {
                    requireType = RequireType.SHOULD_HAVE;
                    visitMethods(list, klass);
                }
            }
            list.add(new Object[]{requireType, klass});
        }
        return list;
    }

    private final RequireType requireType;
    private final AnnotatedElement ae;

    @SuppressWarnings("NonPublicExported")
    public NotFinalTest(RequireType requireType, AnnotatedElement ae) {
        this.requireType = requireType;
        this.ae = ae;
    }

    @Test
    public void test() {
        requireType.check(ae);
    }

    private enum RequireType {

        SHOULD_HAVE() {
            @Override
            void check0(NotFinal annotation, AnnotatedElement element) {
                if (annotation == null) {
                    throw newAssertionError("%1$s not final, and has no annotation @NotFinal", element, null);
                }
                log.warn("{} {}", nonFinalToString(annotation), element);
            }
        },
        SHOULD_NOT_HAVE() {
            @Override
            void check0(NotFinal annotation, AnnotatedElement element) {
                if (annotation != null) {
                    throw newAssertionError("%2$s %1$s is required to not have annotation NotFinal", element, annotation);
                }
            }
        }, LOG_DEBUG() {
            @Override
            void check0(NotFinal annotation, AnnotatedElement element) {
                if (annotation == null) {
                    log.debug("{}", element);
                } else {
                    log.debug("{} {}", nonFinalToString(annotation), element);
                }
            }
        }, NO_ACTION() {
            @Override
            void check0(NotFinal annotation, AnnotatedElement element) {
            }
        };

        private static String nonFinalToString(NotFinal annotation) {
            return annotation.toString().replaceAll("^@" + NotFinal.class.getName().replace(".", "\\."), "@NotFinal");
        }

        private static AssertionError newAssertionError(String fmt, AnnotatedElement ae, NotFinal nf) {
            String t = nf != null ? nonFinalToString(nf) : "";
            AssertionError error = new AssertionError(String.format(fmt, ae, t));
            if (ae instanceof Method) {
                Method m = (Method) ae;
                Class<?> klass = m.getDeclaringClass();
                StackTraceElement ste = new StackTraceElement(klass.getName(), m.getName(), klass.getSimpleName().concat(".java"), -1);
                error.setStackTrace(new StackTraceElement[]{ste});
            }
            throw error;
        }

        void check(AnnotatedElement element) {
            check0(element.getAnnotation(NotFinal.class), element);
        }

        abstract void check0(@Nullable NotFinal annotation, AnnotatedElement element);
    }

}
