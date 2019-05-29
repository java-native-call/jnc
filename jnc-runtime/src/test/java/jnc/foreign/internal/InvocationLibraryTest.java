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
import jnc.foreign.annotation.DefaultConvention;
import jnc.foreign.annotation.Stdcall;
import jnc.foreign.enums.CallingConvention;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * @author zhanhb
 */
@SuppressWarnings("PackageVisibleInnerClass")
public class InvocationLibraryTest {

    private static Library DUMMY_LIB;
    private static TypeFactory typeFactory;
    private static TypeHandlerFactory THR;

    @BeforeClass
    public static void setUpClass() {
        ProxyBuilder builder = ProxyBuilder.builder().useProxyMethods();
        DUMMY_LIB = builder.otherwise(method -> "dlsym".equals(method.getName()) ? (proxy, m, args) -> 0L : null)
                .newInstance(Library.class);
        typeFactory = DefaultForeign.INSTANCE.getTypeFactory();
        THR = DefaultForeign.INSTANCE.getTypeHandlerFactory();
    }

    private <T> InvocationLibrary<T> newInvocationLibrary(Class<T> iface, LoadOptions loadOptions) {
        return new InvocationLibrary<>(iface, DUMMY_LIB, loadOptions, typeFactory, THR);
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
    public void testFind() {
        LoadOptions stdcallOptions = LoadOptions.builder().stdcall().build();
        LoadOptions defaultCallingConventionOptions = LoadOptions.builder().defaultCallingConvention().build();

        {
            test(NoConvention.class, defaultCallingConventionOptions, function -> {
                assertThat(function.apply("stdcall").getCallingConvention()).isEqualTo(CallingConvention.STDCALL);
                assertThat(function.apply("cdecl").getCallingConvention()).isEqualTo(CallingConvention.DEFAULT);
                assertThat(function.apply("noConvention").getCallingConvention()).isEqualTo(CallingConvention.DEFAULT);
            });

            test(StdcallConvention.class, defaultCallingConventionOptions, function -> {
                assertThat(function.apply("stdcall").getCallingConvention()).isEqualTo(CallingConvention.STDCALL);
                assertThat(function.apply("cdecl").getCallingConvention()).isEqualTo(CallingConvention.DEFAULT);
                assertThat(function.apply("noConvention").getCallingConvention()).isEqualTo(CallingConvention.STDCALL);
            });

            test(DefaultConventionLib.class, defaultCallingConventionOptions, function -> {
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

            test(DefaultConventionLib.class, stdcallOptions, function -> {
                assertThat(function.apply("stdcall").getCallingConvention()).isEqualTo(CallingConvention.STDCALL);
                assertThat(function.apply("cdecl").getCallingConvention()).isEqualTo(CallingConvention.DEFAULT);
                assertThat(function.apply("noConvention").getCallingConvention()).isEqualTo(CallingConvention.DEFAULT);
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

    @DefaultConvention
    interface DefaultConventionLib {

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
