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

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;
import java.util.Objects;

/**
 * @author zhanhb
 */
final class Cleaner {

    private static final Cleaner INSTANCE;

    static {
        Ref list = new Ref();
        INSTANCE = new Cleaner(list);
        NativeLoader.getAccessor().onFinalize(list::cleanAll);
    }

    static Cleaner getInstance() {
        return INSTANCE;
    }

    private final ReferenceQueue<Object> queue = new ReferenceQueue<>();
    private final Ref list;

    @VisibleForTesting
    Cleaner(Ref list) {
        this.list = list;
    }

    @SuppressWarnings("NestedAssignment")
    Runnable register(Object obj, Runnable action) {
        Objects.requireNonNull(obj, "obj");
        Objects.requireNonNull(action, "action");
        Ref ref;
        while ((ref = (Ref) queue.poll()) != null) {
            try {
                ref.run();
            } catch (Throwable ignored) {
            }
        }
        return new Ref(obj, this, action);
    }

    @VisibleForTesting
    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject", "PackageVisibleInnerClass"})
    static final class Ref extends PhantomReference<Object> implements Runnable {

        private final Runnable action;
        private final Ref list;
        private Ref prev, next;

        @SuppressWarnings("LeakingThisInConstructor")
        private Ref(Object referent, Cleaner cleaner, Runnable action) {
            super(referent, cleaner.queue);
            Ref list = cleaner.list;
            this.action = action;
            this.list = list;

            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (list) {
                this.prev = list;
                this.next = list.next;
                this.next.prev = this;
                list.next = this;
            }
        }

        @VisibleForTesting
        Ref() {
            super(null, null);
            this.prev = this;
            this.next = this;
            this.action = null;
            this.list = this;
        }

        @VisibleForTesting
        boolean remove() {
            synchronized (list) {
                if (next != this) {
                    next.prev = prev;
                    prev.next = next;
                    prev = this;
                    next = this;
                    return true;
                }
                return false;
            }
        }

        @Override
        public void run() {
            if (remove()) {
                super.clear();
                //noinspection ConstantConditions
                action.run();
            }
        }

        @VisibleForTesting
        void cleanAll() {
            final Ref list = this.list;
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (list) {
                while (true) {
                    Ref next = list.next;
                    if (next == list) {
                        break;
                    }
                    try {
                        next.run();
                    } catch (Throwable ignored) {
                    }
                }
            }
        }

    }

}
