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
        NativeLoader.getAccessor().onFinalize(() -> performRemove(list));
    }

    static Cleaner getInstance() {
        return INSTANCE;
    }

    // visible for test
    @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
    static void performRemove(Ref list) {
        while (true) {
            //noinspection SynchronizationOnLocalVariableOrMethodParameter
            synchronized (list) {
                // assert list == list.list;
                if (list.isListEmpty()) {
                    break;
                }
                try {
                    list.next.clean();
                } catch (Throwable t) {
                }
            }
        }
    }

    private final ReferenceQueue<Object> queue = new ReferenceQueue<>();
    private final Ref list;

    // visible for test
    Cleaner(Ref list) {
        this.list = list;
    }

    @SuppressWarnings("NestedAssignment")
    Cleanable register(Object obj, Runnable action) {
        Objects.requireNonNull(obj, "obj");
        Objects.requireNonNull(action, "action");
        Ref ref;
        while ((ref = (Ref) queue.poll()) != null) {
            try {
                ref.clean();
            } catch (Throwable t) {
            }
        }
        return new Ref(obj, this, action);
    }

    // visible for test
    @SuppressWarnings({"AccessingNonPublicFieldOfAnotherObject", "PackageVisibleInnerClass"})
    static final class Ref extends PhantomReference<Object>
            implements Cleanable {

        private final Runnable action;
        private Ref prev = this, next = this;
        private final Ref list;

        Ref(Object referent, Cleaner cleaner, Runnable action) {
            super(referent, cleaner.queue);
            this.list = cleaner.list;
            this.action = action;

            insert();
        }

        Ref() {
            super(null, null);
            this.action = null;
            this.list = this;
        }

        private void insert() {
            synchronized (list) {
                this.prev = list;
                this.next = list.next;
                this.next.prev = this;
                this.list.next = this;
            }
        }

        // visible for test
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
        public void clean() {
            if (remove()) {
                super.clear();
                action.run();
            }
        }

        boolean isListEmpty() {
            synchronized (list) {
                return list == list.next;
            }
        }

    }

    @SuppressWarnings("PackageVisibleInnerClass")
    interface Cleanable {

        void clean();

    }

}
