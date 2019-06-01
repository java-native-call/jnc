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

import jnc.foreign.exception.JniLoadingException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

/**
 * @author zhanhb
 */
public class DummyNativeMethodTest {

    private final String message = "It seems something goes wrong.";

    /**
     * Test of createProxy method, of class DummyNativeMethod.
     */
    @Test
    public void testCreateProxy() {
        UnsatisfiedLinkError cause = new UnsatisfiedLinkError(message);
        NativeAccessor nativeAccessor = DummyNativeMethod.createProxy(cause);
        nativeAccessor.getCifInfo();
        assertFalse(nativeAccessor.onFinalize(null));
        nativeAccessor.getMethodId(null);
        assertThatThrownBy(() -> nativeAccessor.allocateMemory(0))
                .isExactlyInstanceOf(JniLoadingException.class)
                .hasCause(cause);
    }

    @Test
    public void testCreateProxy2() {
        NativeAccessor nativeAccessor = DummyNativeMethod.createProxy(new JniLoadingException(message));
        nativeAccessor.getCifInfo();
        assertFalse(nativeAccessor.onFinalize(null));
        nativeAccessor.getMethodId(null);
        assertThatThrownBy(() -> nativeAccessor.allocateMemory(0))
                .isExactlyInstanceOf(JniLoadingException.class)
                .hasNoCause()
                .hasMessage(message);
    }

}
