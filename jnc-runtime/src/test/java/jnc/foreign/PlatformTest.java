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
package jnc.foreign;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * @author zhanhb
 */
public class PlatformTest {

    @Test
    public void test() {
        assertTrue(Platform.OS.WINDOWS.isWindows());
        assertFalse(Platform.OS.WINDOWS.isUnix());
        assertFalse(Platform.OS.WINDOWS.isBSD());
        assertFalse(Platform.OS.WINDOWS.isELF());

        assertFalse(Platform.OS.DARWIN.isWindows());
        assertTrue(Platform.OS.DARWIN.isUnix());
        assertTrue(Platform.OS.DARWIN.isBSD());
        assertFalse(Platform.OS.DARWIN.isELF());

        assertTrue(Platform.OS.LINUX.isELF());
        assertTrue(Platform.OS.OPENBSD.isELF());
        assertTrue(Platform.OS.FREEBSD.isELF());
    }

}
