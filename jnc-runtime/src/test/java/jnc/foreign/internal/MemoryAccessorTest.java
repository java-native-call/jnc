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
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;
import jnc.foreign.Pointer;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 * @author zhanhb
 */
public class MemoryAccessorTest {

    private static final String UTF_STRING = "\u20ac\u01cb\u051c\u4f60\u597d";

    private String str2Hex(String str) {
        return str.chars().mapToObj(x -> String.format("%04x", x)).collect(Collectors.joining(""));
    }

    private void assertStringEquals(String expect, String result) {
        assertThat(str2Hex(result)).isEqualTo(str2Hex(expect));
    }

    private void test(Pointer memory) {
        char[] array = new char[ThreadLocalRandom.current().nextInt(0, 8)];
        // use known character
        // jdk may convert to 0xfdff if character is not a valid utf-8 char 
        for (int i = 0; i < array.length; i++) {
            array[i] = UTF_STRING.charAt(ThreadLocalRandom.current().nextInt(UTF_STRING.length()));
        }
        String str = new String(array);

        {
            byte[] result = new byte[str.getBytes(StandardCharsets.UTF_8).length];
            memory.putStringUTF(0, str);
            memory.getBytes(0, result, 0, result.length);
            assertStringEquals(str, new String(result, StandardCharsets.UTF_8));
            assertStringEquals(str, memory.getStringUTF(0));
        }

        {
            char[] chars = new char[str.length()];
            memory.putString16(0, str);
            memory.getCharArray(0, chars, 0, chars.length);
            assertStringEquals(str, new String(chars));
            assertStringEquals(str, memory.getString16(0));
        }
    }

    /**
     * Test of getStringUTF/putStringUTF/putStringUTFN method, of class
     * MemoryAccessor.
     */
    @Test
    public void testStringUTF() {
        AllocatedMemory memory = AllocatedMemory.allocate(32);
        Runnable clear = () -> {
            String random = ThreadLocalRandom.current().ints(32, 127)
                    .limit(31)
                    .mapToObj(ch -> (char) ch)
                    .map(String::valueOf)
                    .collect(Collectors.joining());
            memory.putStringUTF(0, random);
        };

        clear.run();
        test(memory);

        clear.run();
        test(UnboundedDirectMemory.of(memory.address()));

        clear.run();
        // TODO cast long to int
        // maybe api should add another method slice(int)
        test(memory.slice(0, (int) memory.size()));

        clear.run();
        test(UnboundedDirectMemory.of(memory.address() + 1));
    }

}
