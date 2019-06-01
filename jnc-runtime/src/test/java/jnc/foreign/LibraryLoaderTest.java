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

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhanhb
 */
public class LibraryLoaderTest {

    private static final Logger log = LoggerFactory.getLogger(LibraryLoaderTest.class);

    /**
     * Test of failImmediately method, of class LibraryLoader.
     */
    @Test
    public void testFailImmediately() {
        log.info("failImmediately");
        LibraryLoader<LibInterface> loader = LibraryLoader.create(LibInterface.class);
        assertThatThrownBy(() -> loader.failImmediately().load("not_exist"))
                .isExactlyInstanceOf(UnsatisfiedLinkError.class);
        LibInterface lib = loader.failDeferred().load("not_exist");
        assertThatThrownBy(lib::getpid).isExactlyInstanceOf(UnsatisfiedLinkError.class);
    }

    private interface LibInterface {

        @SuppressWarnings("UnusedReturnValue")
        long getpid();

    }

}
