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

import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Paths;
import jnc.foreign.Platform;
import jnc.foreign.exception.JniLoadingException;
import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author zhanhb
 */
public class NativeLoaderTest {

    private static final Logger log = LoggerFactory.getLogger(NativeLoaderTest.class);

    private final NativeLoader instance = new NativeLoader();
    private final String message = "It seems something goes wrong.";

    /**
     * Test of createProxy method, of class DummyNativeMethod.
     */
    @Test
    public void testCreateProxy() {
        UnsatisfiedLinkError cause = new UnsatisfiedLinkError(message);
        NativeAccessor nativeAccessor = instance.createProxy(cause);
        nativeAccessor.getCifInfo();
        assertFalse(nativeAccessor.onFinalize(null));
        nativeAccessor.getMethodId(null);
        assertThatThrownBy(() -> nativeAccessor.allocateMemory(0))
                .isExactlyInstanceOf(JniLoadingException.class)
                .hasCause(cause);
    }

    @Test
    public void testCreateProxy2() {
        NativeAccessor nativeAccessor = instance.createProxy(new JniLoadingException(message));
        nativeAccessor.getCifInfo();
        assertFalse(nativeAccessor.onFinalize(null));
        nativeAccessor.getMethodId(null);
        assertThatThrownBy(() -> nativeAccessor.allocateMemory(0))
                .isExactlyInstanceOf(JniLoadingException.class)
                .hasNoCause()
                .hasMessage(message);
    }

    /**
     * Test of getLibClassPath method, of class NativeLoader.
     */
    @Test
    public void testGetLibClassPath() throws Exception {
        log.info("getLibClassPath");
        Method OS_GETTER = Platform.class.getMethod("getOS");
        Method ARCH_GETTER = Platform.class.getMethod("getArch");
        ProxyBuilder builder = ProxyBuilder.identifier();
        for (Platform.OS os : Platform.OS.values()) {
            for (Platform.Arch arch : Platform.Arch.values()) {
                Platform platform = builder.customize(OS_GETTER, (a, b, c) -> os)
                        .customize(ARCH_GETTER, (a, b, c) -> arch)
                        .newInstance(Platform.class);
                if (os != Platform.OS.UNKNOWN && arch != Platform.Arch.UNKNOWN || os == Platform.OS.DARWIN) {
                    // when profile native is active, maven won't copy lib, thus lib doesn't exist
                    instance.getLibClassPath(platform);
                } else {
                    assertThatThrownBy(() -> instance.getLibClassPath(platform), "%s %s", os, arch)
                            .isInstanceOf(UnsupportedOperationException.class);
                }
            }
        }
    }

    /**
     * Test of loadWithTempFile method, of class NativeLoader.
     */
    @Test
    public void testLoadWithTempFile() throws Exception {
        log.info("loadWithTempFile");
        URL url = instance.getLibPath();
        instance.loadWithTempFile(path -> assertTrue(Files.exists(Paths.get(path))), url);
    }

}
