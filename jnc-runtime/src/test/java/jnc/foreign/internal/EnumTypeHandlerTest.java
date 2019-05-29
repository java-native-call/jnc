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

import jnc.foreign.Foreign;
import jnc.foreign.LibraryLoader;
import jnc.foreign.NativeType;
import jnc.foreign.Platform;
import jnc.foreign.Struct;
import jnc.foreign.Type;
import jnc.foreign.annotation.Continuously;
import jnc.foreign.exception.UnmappableNativeValueException;
import jnc.foreign.typedef.int32_t;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhanhb
 */
@SuppressWarnings("PublicInnerClass")
public class EnumTypeHandlerTest {

    private static final Logger log = LoggerFactory.getLogger(EnumTypeHandlerTest.class);

    /**
     * Test of getInstance method, of class EnumTypeHandler.
     */
    @Test
    @SuppressWarnings("unchecked")
    public void testGetInstance() {
        log.info("getInstance");
        assertThatThrownBy(() -> EnumTypeHandler.getInstance(Enum.class)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> EnumTypeHandler.getInstance((Class) Object.class)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    public void testNotAllow() {
        assertThatThrownBy(() -> new Struct() {
            private final Struct.EnumField<Test1> f = enumField(Test1.class);
        }).isInstanceOf(IllegalStateException.class).hasMessageContaining(" allowed ");
    }

    @Test
    public void testFieldAccess() {
        Struct1 struct1 = new Struct1();
        assertThat(struct1.size()).isEqualTo(1);
        struct1.setSeason(Season1.Winter);
        assertThat(struct1.getSeason()).isEqualTo(Season1.Winter);

        Struct2 struct2 = new Struct2();
        assertThat(struct2.size()).isEqualTo(4);
        struct2.setSeason(Season2.Winter);
        assertThat(struct2.getSeason()).isEqualTo(Season2.Winter);
    }

    @Test
    public void testInvoke() {
        assertTrue(Libc.INSTANCE.isalpha(Char.A));
        assertThat(Libc.INSTANCE.toupper(0)).isNull();
        assertThat(Libc.INSTANCE.toupper('a')).isEqualTo(Char.A);
        assertThatThrownBy(() -> Libc.INSTANCE.toupper('c'))
                .isInstanceOf(UnmappableNativeValueException.class)
                .matches(ex -> ((UnmappableNativeValueException) ex).getValue() == 'C');
    }

    @Test
    public void testSignedChange() throws Exception {
        Type type = Foreign.getDefault().getEnumFieldAccessor(Signed1.class).type();
        boolean isSigned = (boolean) type.getClass().getMethod("isSigned").invoke(type);
        assertTrue(isSigned);
    }

    @SuppressWarnings("UnusedReturnValue")
    private <E extends Enum<E>> Struct testTooLarge(Class<E> klass) {
        return new Struct() {
            {
                enumField(klass);
            }
        };
    }

    @Test
    public void testTooLarge() {
        testTooLarge(Large1.class);
        assertThatThrownBy(() -> testTooLarge(Large2.class)).isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> testTooLarge(Large3.class)).isInstanceOf(IllegalArgumentException.class);
        testTooLarge(Large4.class);
    }

    private static class Struct1 extends Struct {

        private final EnumField<Season1> season = enumField(Season1.class);

        public Season1 getSeason() {
            return season.get();
        }

        public void setSeason(Season1 field) {
            this.season.set(field);
        }

    }

    private static class Struct2 extends Struct {

        private final EnumField<Season2> season = enumField(Season2.class);

        public Season2 getSeason() {
            return season.get();
        }

        public void setSeason(Season2 field) {
            this.season.set(field);
        }

    }

    public interface Libc {

        Libc INSTANCE = LibraryLoader.create(Libc.class).load(Platform.getNativePlatform().getLibcName());

        @int32_t
        boolean isalpha(Char ch);

        Char toupper(int ch);

    }

    @Continuously(type = NativeType.ADDRESS)
    private enum Test1 {
    }

    @Continuously(type = NativeType.UINT8, start = -1)
    private enum Signed1 {
    }

    @Continuously(type = NativeType.UINT8, start = 255)
    private enum Large1 {
        A // ok
    }

    @Continuously(type = NativeType.UINT8, start = 255)
    private enum Large2 {
        A, B // failed
    }

    @Continuously(type = NativeType.UINT8, start = 256)
    private enum Large3 {
        A, B // failed
    }

    @Continuously(type = NativeType.UINT8, start = 254)
    private enum Large4 {
        A, B // ok
    }

    @Continuously(start = 'A')
    public enum Char {
        A, B
    }

    @Continuously(type = NativeType.SINT8)
    private enum Season1 {
        Spring, Summer, Autumn, Winter
    }

    private enum Season2 {
        Spring, Summer, Autumn, Winter
    }

}
