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

import java.io.PrintStream;
import java.util.Map;
import java.util.function.Function;

/**
 * @author zhanhb
 */
public class ConversionsGenerator {

    public static void main(String[] args) {
        @SuppressWarnings("UseOfSystemOutOrSystemErr")
        PrintStream pw = System.out;
        Class<?>[] primitives = {
            boolean.class,
            byte.class,
            short.class,
            char.class,
            int.class,
            long.class,
            float.class,
            double.class
        };

        for (Class<?> from : primitives) {
            for (Class<?> to : primitives) {
                if (from == to) {
                    continue;
                }
                String exp;
                if (from == boolean.class) {
                    exp = to != int.class ? "b -> (%2$s) (b ? 0 : 1)" : "b -> b ? 0 : 1";
                } else if (to == boolean.class) {
                    exp = from == float.class || from == double.class ? "v -> !(v == 0)" : "v -> v != 0";
                } else {
                    exp = "value -> (%2$s) (%1$s) value";
                }
                pw.printf("add(%1$s.class, %2$s.class, " + exp + ", map);%n",
                        from.getName(), to.getName());
            }
        }
    }

    private static <S, D> void add(Class<S> source, Class<D> dest, Function<S, D> apply, Map<?, Function<?, ?>> map) {
    }

}
