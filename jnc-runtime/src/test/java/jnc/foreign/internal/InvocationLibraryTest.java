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

import java.util.function.Consumer;
import java.util.function.Function;
import jnc.foreign.LoadOptions;
import jnc.foreign.LoadOptionsBuilder;
import jnc.foreign.annotation.DefaultConvention;
import jnc.foreign.annotation.Stdcall;
import jnc.foreign.enums.CallingConvention;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;

/**
 * @author zhanhb
 */
@SuppressWarnings("PackageVisibleInnerClass")
public class InvocationLibraryTest {

    private static final Library dummy = new ProxyBuilder().useProxyMethods()
            .otherwise(method -> "dlsym".equals(method.getName()) ? (proxy, m, args) -> 0L : null)
            .newInstance(Library.class);
    private static final TypeHandlerRegistry THR = new TypeHandlerRegistry();

    private <T> InvocationLibrary<T> newInvocationLibrary(Class<T> iface, LoadOptions loadOptions) {
        return new InvocationLibrary<>(iface, dummy, loadOptions, THR);
    }

    private <T> void test(Class<T> klass, LoadOptions options, Consumer<Function<String, InvocationLibrary.MethodInvocation>> consumer) {
        InvocationLibrary<T> library = newInvocationLibrary(klass, options);
        consumer.accept(name -> {
            try {
                return library.find(klass.getMethod(name));
            } catch (NoSuchMethodException e) {
                throw new AssertionError(e);
            }
        });
    }

    /**
     * Test of find method, of class InvocationLibrary.
     */
    @Test
    public void testFind() throws Exception {
        LoadOptions noConvenctionOptions = new LoadOptionsBuilder().unsetCallingConvention().create();
        LoadOptions stdcallOptions = new LoadOptionsBuilder().stdcall().create();
        LoadOptions cdeclOptions = new LoadOptionsBuilder().cdecl().create();

        {
            test(NoConvention.class, noConvenctionOptions, function -> {
                assertThat(function.apply("stdcall").getCallingConvention()).isEqualTo(CallingConvention.STDCALL);
                assertThat(function.apply("cdecl").getCallingConvention()).isEqualTo(CallingConvention.DEFAULT);
                assertThat(function.apply("noConvention").getCallingConvention()).isEqualTo(CallingConvention.DEFAULT);
            });

            test(StdcallConvention.class, noConvenctionOptions, function -> {
                assertThat(function.apply("stdcall").getCallingConvention()).isEqualTo(CallingConvention.STDCALL);
                assertThat(function.apply("cdecl").getCallingConvention()).isEqualTo(CallingConvention.DEFAULT);
                assertThat(function.apply("noConvention").getCallingConvention()).isEqualTo(CallingConvention.STDCALL);
            });
        }

        {
            test(NoConvention.class, cdeclOptions, function -> {
                assertThat(function.apply("stdcall").getCallingConvention()).isEqualTo(CallingConvention.STDCALL);
                assertThat(function.apply("cdecl").getCallingConvention()).isEqualTo(CallingConvention.DEFAULT);
                assertThat(function.apply("noConvention").getCallingConvention()).isEqualTo(CallingConvention.DEFAULT);
            });

            test(StdcallConvention.class, cdeclOptions, function -> {
                assertThat(function.apply("stdcall").getCallingConvention()).isEqualTo(CallingConvention.STDCALL);
                assertThat(function.apply("cdecl").getCallingConvention()).isEqualTo(CallingConvention.DEFAULT);
                assertThat(function.apply("noConvention").getCallingConvention()).isEqualTo(CallingConvention.DEFAULT);
            });
        }

        {
            test(NoConvention.class, stdcallOptions, function -> {
                assertThat(function.apply("stdcall").getCallingConvention()).isEqualTo(CallingConvention.STDCALL);
                assertThat(function.apply("cdecl").getCallingConvention()).isEqualTo(CallingConvention.DEFAULT);
                assertThat(function.apply("noConvention").getCallingConvention()).isEqualTo(CallingConvention.STDCALL);
            });

            test(StdcallConvention.class, stdcallOptions, function -> {
                assertThat(function.apply("stdcall").getCallingConvention()).isEqualTo(CallingConvention.STDCALL);
                assertThat(function.apply("cdecl").getCallingConvention()).isEqualTo(CallingConvention.DEFAULT);
                assertThat(function.apply("noConvention").getCallingConvention()).isEqualTo(CallingConvention.STDCALL);
            });
        }
    }

    interface NoConvention {

        @Stdcall
        void stdcall();

        @DefaultConvention
        void cdecl();

        void noConvention();

    }

    @Stdcall
    interface StdcallConvention {

        @Stdcall
        void stdcall();

        @DefaultConvention
        void cdecl();

        void noConvention();

    }

}
