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

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.function.Consumer;
import jnc.foreign.Platform;
import jnc.foreign.exception.JniLoadingException;

/**
 * @author zhanhb
 */
final class NativeLoader {

    private static final NativeAccessor NATIVE_ACCESSOR = init();

    static {
        // Initialize Cleaner when our instance is ready.
        // Make sure class Cleaner is initialized.
        // Cleaner => NativeLoader => NativeMethods
        //noinspection ResultOfMethodCallIgnored
        Cleaner.getInstance();
    }

    private static NativeAccessor init() {
        NativeLoader loader = new NativeLoader();
        try {
            loader.load(System::load, loader.getLibPath());
        } catch (Throwable t) {
            return loader.createProxy(t);
        }
        return NativeMethods.INSTANCE;
    }

    static NativeAccessor getAccessor() {
        return NATIVE_ACCESSOR;
    }

    @VisibleForTesting
    NativeAccessor createProxy(Throwable t) {
        return ProxyBuilder.builder()
                .orThrow(__ -> t instanceof UnsatisfiedLinkError ? new JniLoadingException(t) : t)
                .newInstance(NativeAccessor.class);
    }

    private void load(Consumer<String> loadAction, URL url) {
        try {
            if ("file".equalsIgnoreCase(url.getProtocol())) {
                loadAction.accept(Paths.get(url.toURI()).toString());
            } else {
                loadWithTempFile(loadAction, url);
            }
        } catch (IOException | URISyntaxException ex) {
            throw new JniLoadingException(ex);
        }
    }

    @VisibleForTesting
    URL getLibPath() {
        String libPath = getLibClassPath(DefaultPlatform.INSTANCE);
        URL url = NativeLoader.class.getClassLoader().getResource(libPath);
        if (url == null) {
            throw new UnsatisfiedLinkError("unable to find native lib in the classpath");
        }
        return url;
    }

    @VisibleForTesting
    String getLibClassPath(Platform platform) {
        StringBuilder sb = new StringBuilder(NativeLoader.class.getPackage().getName().replace(".", "/")).append("/native/");
        Platform.OS os = platform.getOS();
        switch (os) {
            case DARWIN:
                return sb.append("darwin/libjnc.jnilib").toString();
            case UNKNOWN:
                throw new UnsupportedOperationException("unsupported operation system");
            default:
                sb.append(os.name().toLowerCase(Locale.US));
                break;
        }
        sb.append('/');
        Platform.Arch arch = platform.getArch();
        switch (arch) {
            case I386:
            case X86_64:
                return sb.append(System.mapLibraryName("jnc-" + arch.name().toLowerCase(Locale.US))).toString();
            default:
                throw new UnsupportedOperationException("unsupported operation system arch");
        }
    }

    @VisibleForTesting
    void loadWithTempFile(Consumer<String> loadAction, URL url) throws IOException {
        Path tmp = Files.createTempFile("lib", System.mapLibraryName("jnc"));
        try {
            try (InputStream is = url.openStream()) {
                Files.copy(is, tmp, StandardCopyOption.REPLACE_EXISTING);
            }
            loadAction.accept(tmp.toAbsolutePath().toString());
        } finally {
            try {
                Files.delete(tmp);
            } catch (IOException ignored) {
            }
        }
    }

}
