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
package jnc.provider;

import java.nio.ByteBuffer;
import java.nio.charset.*;
import java.util.*;
import static java.util.stream.Collectors.*;
import java.util.stream.IntStream;
import jnc.foreign.Pointer;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertArrayEquals;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhanhb
 */
public class StringCodingTest {

    private static final Logger log = LoggerFactory.getLogger(StringCodingTest.class);

    private static final String ASCII = IntStream.rangeClosed(0, 127).mapToObj(x -> (char) x).map(String::valueOf)
            .collect(joining());
    private static final byte[] ASCII_BYTES = ASCII.getBytes(StandardCharsets.UTF_8);
    private static final List<Charset> asciiCompatibleCharsets = Charset.availableCharsets()
            .values().stream()
            .filter(StringCodingTest::isAsciiCompatible)
            .collect(toList());

    // only make sure the charset can convert all ascii bytes to String, not sure reverse convert
    private static boolean isAsciiCompatible(Charset charset) {
        String s;
        try {
            // TODO leak the bytes, shall we trust the charset won't modify the bytes content?
            s = charset.newDecoder().decode(ByteBuffer.wrap(ASCII_BYTES)).toString();
        } catch (CharacterCodingException e) {
            return false;
        }
        return s.equals(ASCII);
    }

    /**
     * Test of put method, of class StringCoding.
     */
    @Test
    public void testSimplePut() {
        log.info("put");
        Memory memory = AllocatedMemory.allocate(8);
        memory.putLong(0, 0x2020202020202020L);
        MemoryAccessor accessor = memory.getAccessor();
        //noinspection ConstantConditions
        assertThatThrownBy(() -> StringCoding.put(accessor, 0, null, 1))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> StringCoding.put(accessor, 0, new byte[0], 0))
                .isInstanceOf(IllegalArgumentException.class);
        StringCoding.put(accessor, 2, new byte[0], 4);
        assertThat(memory.getLong(0)).isEqualTo(0x2020000000002020L);
    }

    @Test
    public void testAscii() {
        Pointer memory = AllocatedMemory.allocate(128);
        String str = ASCII.substring(1);
        byte[] expect = str.getBytes();

        byte[] bytes = new byte[127];
        for (Charset charset : asciiCompatibleCharsets) {
            if (charset.canEncode()) {
                memory.putString(0, str, charset);
                memory.getBytes(0, bytes, 0, bytes.length);
                assertArrayEquals(charset.name(), expect, bytes);
                assertThat(memory.getString(0, charset)).isEqualTo(str);
                assertThat(memory.slice(32, 40).getString(1, charset))
                        .isEqualTo(str.substring(33, 40));
            }
        }
    }

}
