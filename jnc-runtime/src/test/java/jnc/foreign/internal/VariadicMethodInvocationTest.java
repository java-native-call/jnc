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

import java.nio.charset.StandardCharsets;
import jnc.foreign.LibraryLoader;
import jnc.foreign.Platform;
import jnc.foreign.typedef.size_t;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 * @author zhanhb
 */
public class VariadicMethodInvocationTest {

    private final char z = Platform.getNativePlatform().getOS().isWindows() ? 'I' : 'z';

    /**
     * Test of invoke method, of class VariadicMethodInvocation.
     */
    @Test
    public void testInvoke() throws Exception {
        // TODO promotions float->double
        // https://en.cppreference.com/w/c/language/variadic
        byte[] bytes = new byte[200];
        byte[] format = ("%" + z + "x" + " %d %.2f\u0000").getBytes(StandardCharsets.UTF_8);
        int n = Libc.INSTANCE.sprintf(bytes, format, size_t.class, 0x123456, 1234, 0.2);
        assertThat(new String(bytes, 0, n)).isEqualTo("123456 1234 0.20");
    }

    private interface Libc {

        Libc INSTANCE = LibraryLoader.create(Libc.class).load(Platform.getNativePlatform().getLibcName());

        int sprintf(byte[] target, byte[] format, Object... args);
    }

}
