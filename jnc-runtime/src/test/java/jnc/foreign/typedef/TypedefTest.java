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
package jnc.foreign.typedef;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import jnc.foreign.Platform;
import jnc.foreign.enums.TypeAlias;
import jnc.foreign.spi.ForeignProvider;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 * @author zhanhb
 */
public class TypedefTest {

    private final String OSX = "osx";
    private final String UNIX = "unix";
    private final String BSD = "bsd";
    private final String[] subPackages = {OSX, UNIX, BSD};
    private final String basePackage = TypedefTest.class.getPackage().getName();

    @Test
    public void testSupport() {
        // test annotation class present
        Map<String, List<TypeAlias>> map = new HashMap<>(4);
        for (TypeAlias typeAlias : EnumSet.allOf(TypeAlias.class)) {
            if (typeAlias == TypeAlias.cint) {
                continue;
            }
            AtomicReference<String> found = new AtomicReference<>();
            try {
                Class.forName(basePackage + "." + typeAlias);
                found.set("");
            } catch (ClassNotFoundException ignored) {
            }
            for (String subPackage : subPackages) {
                try {
                    Class.forName(basePackage + "." + subPackage + "." + typeAlias);
                    assertThat(found.compareAndSet(null, subPackage))
                            .describedAs("duplicate annotation '%s' found both in package '%s' and '%s'", typeAlias, found, subPackage)
                            .isTrue();
                } catch (ClassNotFoundException ignored) {
                }
            }
            String pkg = found.get();
            assertThat(pkg)
                    .describedAs("alias %s not found in any packages", typeAlias)
                    .isNotNull();
            map.computeIfAbsent(pkg, __ -> new ArrayList<>(16)).add(typeAlias);
        }

        // test platform support
        Set<String> supported = new HashSet<>(4);
        ForeignProvider foreignProvider = ForeignProvider.getDefault();
        Platform.OS os = foreignProvider.getPlatform().getOS();
        if (os == Platform.OS.DARWIN) {
            supported.add(OSX);
        }
        if (os.isBSD()) {
            supported.add(BSD);
        }
        if (os.isUnix()) {
            supported.add(UNIX);
        }

        for (Map.Entry<String, List<TypeAlias>> entry : map.entrySet()) {
            String key = entry.getKey();
            List<TypeAlias> value = entry.getValue();
            boolean s = supported.contains(key) || key.isEmpty();
            for (TypeAlias typeAlias : value) {
                boolean result;
                try {
                    foreignProvider.getForeign().findType(typeAlias);
                    result = true;
                } catch (UnsupportedOperationException ex) {
                    result = false;
                }
                String msg = "alias support '%s' should %sbe supported on platform %s, but got %s";
                assertThat(s == result)
                        .describedAs(msg, typeAlias, s ? "" : "not ", os, result ? "supported" : "unsupported")
                        .isTrue();
            }
        }
    }

}
