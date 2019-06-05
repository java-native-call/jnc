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

import java.nio.ByteOrder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import jnc.foreign.Pointer;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhanhb
 */
public class MemoryAccessorTest {

    private static final Logger log = LoggerFactory.getLogger(MemoryAccessorTest.class);
    // use known character
    // jdk will convert to 0xfdff if character is not a valid utf-8 char
    private static final String UTF_STRING = "\u20ac\u01cb\u051c\u4f60\u597d";
    private static final String[] EMOJI = IntStream.of(
            0x1f34c, 0x1f600, 0x1f92c, 0x1f44d
    ).mapToObj(MemoryAccessorTest::fromUnicode).toArray(String[]::new);

    private static String fromUnicode(int codePoint) {
        return new String(Character.toChars(codePoint));
    }

    private static String generate(int allow, int count) {
        return IntStream.range(0, count).mapToObj(__ -> {
            switch (ThreadLocalRandom.current().nextInt(Integer.MAX_VALUE) % allow) {
                case 0:
                    return String.valueOf(UTF_STRING.charAt(ThreadLocalRandom.current().nextInt(UTF_STRING.length())));
                case 1:
                    return String.valueOf((char) ThreadLocalRandom.current().nextInt(32, 127));
                default:
                    return EMOJI[ThreadLocalRandom.current().nextInt(EMOJI.length)];
            }
        }).collect(Collectors.joining());
    }

    @SuppressWarnings("unused")
    private String str2Hex(String str) {
        return str.chars().mapToObj(x -> String.format("%04x", x)).collect(Collectors.joining(""));
    }

    private void assertStringEquals(String expect, String result) {
        // assertThat(str2Hex(result)).isEqualTo(str2Hex(expect));
        assertThat(result).isEqualTo(expect);
    }

    private void testStringUTF(Pointer memory, String str) {
        byte[] result = new byte[str.getBytes(StandardCharsets.UTF_8).length];
        memory.putStringUTF(0, str);
        memory.getBytes(0, result, 0, result.length);
        assertStringEquals(str, new String(result, StandardCharsets.UTF_8));
        assertStringEquals(str, memory.getStringUTF(0));
    }

    private void testString16(Pointer memory, String str) {
        // UTF-16 decoder is a decoder for UTF-16BE or UTF-16LE(auto detecting when encoding)
        // UTF-16 encoder is same meaning as UTF-16BE
        // But StandardCharset.UTF_16.newDecoder().isAutoDetecting() reporting false
        Charset charset = ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN
                ? StandardCharsets.UTF_16BE : StandardCharsets.UTF_16LE;
        char[] chars = new char[str.length()];
        memory.putString(0, str, charset);
        memory.getCharArray(0, chars, 0, chars.length);
        assertStringEquals(str, new String(chars));
        assertStringEquals(str, memory.getString(0, charset));
    }

    private void testStringBase(BiConsumer<Pointer, String> test, int allow) {
        SizedDirectMemory memory = AllocatedMemory.allocate(100);

        for (int repeat = 0; repeat < 200; ++repeat) {
            String str = generate(allow, ThreadLocalRandom.current().nextInt(0, 16));
            test.accept(memory, str);
            test.accept(UnboundedDirectMemory.of(memory.address()), str);

            // TODO cast long to int
            // maybe api should add another method slice(int)
            test.accept(memory.slice(0, (int) memory.capacity()), str);
            test.accept(UnboundedDirectMemory.of(memory.address() + 1), str);
        }
    }

    /**
     * Test of getStringUTF/putStringUTF/putStringUTFN method, of class
     * MemoryAccessor.
     */
    @Test
    public void testStringUTF() {
        // JNI uses Modified UTF-8 Strings instead of UTF-8 Strings
        // https://docs.oracle.com/javase/7/docs/technotes/guides/jni/spec/types.html#wp477.
        // emoji is different
        testStringBase(this::testStringUTF, 2);
    }

    /**
     * Test of getStringUTF/putStringUTF/putStringUTFN method, of class
     * MemoryAccessor.
     */
    @Test
    public void testString16() {
        testStringBase(this::testString16, 3);
    }

    @Test
    public void testLimit() {
        Pointer pointer = AllocatedMemory.allocate(16);
        long spaces = 0x2020202020202020L;
        pointer.putLong(0, spaces);
        pointer.putLong(8, spaces);
        Pointer slice = pointer.slice(6, 9);
        assertThat(slice.getStringUTF(1)).isEqualTo("  ");
        assertThat(slice.getString(0, StandardCharsets.UTF_16BE)).isEqualTo("\u2020");
        assertThat(slice.getString(0, StandardCharsets.UTF_16LE)).isEqualTo("\u2020");
    }

}
