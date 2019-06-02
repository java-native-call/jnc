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
import java.lang.reflect.Modifier;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.ProtectionDomain;
import javax.annotation.Nullable;
import jnc.foreign.Struct;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhanhb
 */
public class NotFinalTest {

    private static final Logger log = LoggerFactory.getLogger(NotFinalTest.class);
    private static final int SYNTHETIC = 0x00001000;

    private static boolean isSynthetic(int mod) {
        return (mod & SYNTHETIC) != 0;
    }

    @Test
    public void testValue() throws Exception {
        ProtectionDomain protectionDomain = Struct.class.getProtectionDomain();
        URL location = protectionDomain.getCodeSource().getLocation();
        Path directory = Paths.get(location.toURI());
        Files.walk(directory)
                .filter(path -> path.getFileName().toString().endsWith(".class"))
                .map(path -> directory.relativize(path).toString())
                .map(str -> str.replaceAll("\\.class$", "").replace(File.separatorChar, '.'))
                .forEach(this::checkClass);
    }

    private void checkClass0(String className) throws Throwable {
        Class<?> klass = Class.forName(className);
        NotFinal annotation = klass.getAnnotation(NotFinal.class);
        RequireType requireType;
        int modifiers = klass.getModifiers();
        if (isSynthetic(modifiers)) {
            return;
        }
        if (klass.isEnum() || klass.isInterface()) {
            requireType = RequireType.LOG_DEBUG;
        } else {
            if (Modifier.isAbstract(modifiers)) {
                requireType = RequireType.SHOUD_NOT_HAVE;
            } else if (Modifier.isFinal(modifiers)) {
                requireType = RequireType.NO_ACTION;
            } else {
                requireType = RequireType.SHOUD_HAVE;
            }
        }
        requireType.check(annotation, klass);
    }

    private void checkClass(String className) {
        try {
            checkClass0(className);
        } catch (Throwable ex) {
            throw new RuntimeException(ex);
        }

    }

    private enum RequireType {

        SHOUD_HAVE() {
            @Override
            void check(NotFinal annotation, Class<?> klass) {
                if (annotation == null) {
                    throw new AssertionError(klass + " not final, and has no annotation @NotFinal");
                }
                log.warn("{} {}", nonFinalToString(annotation), klass);
            }
        },
        SHOUD_NOT_HAVE() {
            @Override
            void check(NotFinal annotation, Class<?> klass) {
                if (annotation != null) {
                    throw new AssertionError(nonFinalToString(annotation) + " " + klass + " is required to not have annotation NotFinal");
                }
            }
        }, LOG_DEBUG() {
            @Override
            void check(NotFinal annotation, Class<?> klass) {
                if (annotation == null) {
                    log.debug("{}", klass);
                } else {
                    log.debug("{} {}", nonFinalToString(annotation), klass);
                }
            }
        }, NO_ACTION() {
            @Override
            void check(NotFinal annotation, Class<?> klass) {
            }
        };

        private static String nonFinalToString(NotFinal annotation) {
            return annotation.toString().replaceAll("^@" + NotFinal.class.getName().replace(".", "\\."), "@NotFinal");
        }

        abstract void check(@Nullable NotFinal annotation, Class<?> klass);
    }

}
