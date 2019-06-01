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

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/**
 * @author zhanhb
 */
public interface SleepUtil {

    static boolean sleepWhile(BooleanSupplier condition) {
        Objects.requireNonNull(condition);
        System.gc();
        boolean interrupted = false;
        try {
            int sleep = 1;
            for (int i = 0; i < 12 && condition.getAsBoolean(); ++i) {
                try {
                    TimeUnit.MILLISECONDS.sleep(sleep);
                } catch (InterruptedException ex) {
                    interrupted = true;
                }
                sleep <<= 1;
                if (i > 8) {
                    System.gc();
                }
            }
        } finally {
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
        return condition.getAsBoolean();
    }

    // usually usage assertTrue(() -> sleepUntil(...))
    // ... should be the condition should be true after sleep
    static boolean sleepUntil(BooleanSupplier condition) {
        Objects.requireNonNull(condition);
        return !sleepWhile(() -> !condition.getAsBoolean());
    }

}
