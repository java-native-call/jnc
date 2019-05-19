/*
 * Copyright 2018 zhanhb.
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
package jnc.foreign;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 *
 * @author zhanhb
 */
public class PaddingTest {

    @Test(expected = IllegalArgumentException.class)
    public void testIllegal1() {
        Padding padding = new Padding(0);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testIllegal2() {
        Padding padding = new Padding(3, 3);
    }

    @Test
    public void testPaddingInStruct() {
        class A extends Struct {
           private final Padding padding = padding(9, 2);
        }
        assertEquals(10, new A().size());
    }

}
