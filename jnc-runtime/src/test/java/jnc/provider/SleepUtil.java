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

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.BooleanSupplier;

/**
 * @author zhanhb
 */
public interface SleepUtil {

    static boolean sleepWhile(BooleanSupplier condition) {
        if (condition.getAsBoolean()) {
            int k = 0;
            long sleep = 1;
            boolean interrupted = false;
            try {
                do {
                    System.gc();
                    System.runFinalization();
                    if (!condition.getAsBoolean()) {
                        return false;
                    }
                    try {
                        TimeUnit.MILLISECONDS.sleep(sleep);
                    } catch (InterruptedException ex) {
                        interrupted = true;
                    }
                    ++k;
                    sleep = Math.min(sleep << 1, 1000);
                } while (k < 120);
                return true;
            } finally {
                if (interrupted) {
                    Thread.currentThread().interrupt();
                }
            }
        }
        return false;
    }

    // simple usage assertTrue(() -> sleepUntil(...))
    // ... is the condition, which ought be true after sleep
    static boolean sleepUntil(BooleanSupplier condition) {
        Objects.requireNonNull(condition);
        return !sleepWhile(() -> !condition.getAsBoolean());
    }

}
