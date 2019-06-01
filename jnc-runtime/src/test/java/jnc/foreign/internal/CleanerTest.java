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

import java.util.concurrent.atomic.AtomicBoolean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.junit.Assert.*;
import org.junit.Test;

/**
 * @author zhanhb
 */
public class CleanerTest {

    private static final Runnable NOOP = () -> {
    };

    @Test
    public void testPerformRemove() throws Exception {
        Cleaner.Ref list = new Cleaner.Ref();
        assertFalse(list.remove());

        Cleaner cleaner = new Cleaner(list);

        {
            AtomicBoolean executed = new AtomicBoolean();
            cleaner.register(list, () -> {
                executed.set(true);
            });
            Cleaner.performRemove(list);
            assertTrue("performRemove not executed", executed.get());
        }

        {
            // should be reusable
            // list should be empty, or a NullPointerException will be throwed
            assertFalse(list.remove());

            cleaner.register(list, () -> {
                throw new RuntimeException();
            });
            Cleaner.performRemove(list);
            assertFalse(list.remove());
        }
    }

    @Test
    public void testReferenceUnreachable() {
        Cleaner.Ref list = new Cleaner.Ref();
        Cleaner cleaner = new Cleaner(list);

        {
            AtomicBoolean executed = new AtomicBoolean();
            {
                cleaner.register(new Object(), () -> {
                    executed.set(true);
                });
            }
            // register new object to make sure trigged
            assertTrue(SleepUtil.sleepUntil(() -> {
                cleaner.register(new Object(), NOOP);
                return executed.get();
            }));
        }
    }

    @Test
    public void testReferenceUnreachableExceptional() {
        Cleaner.Ref list = new Cleaner.Ref();
        Cleaner cleaner = new Cleaner(list);
        {
            AtomicBoolean executed = new AtomicBoolean();
            {
                cleaner.register(new Object(), () -> {
                    executed.set(true);
                    throw new RuntimeException();
                });
            }

            // register new object to make sure trigged
            assertThat(catchThrowable(() -> {
                assertTrue(SleepUtil.sleepUntil(() -> {
                    cleaner.register(new Object(), NOOP);
                    return executed.get();
                }));
            })).doesNotThrowAnyException();
        }
    }

}
