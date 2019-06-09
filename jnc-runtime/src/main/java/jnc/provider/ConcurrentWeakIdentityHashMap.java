/*
 * Copyright 2016 zhanhb.
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

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.WeakReference;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;

/**
 *
 * @author zhanhb
 * @param <K> the type of keys maintained by this map
 * @param <V> the type of mapped values
 */
@SuppressWarnings({"element-type-mismatch", "WeakerAccess"})
final class ConcurrentWeakIdentityHashMap<K, V> {

    private final ConcurrentMap<Key<K>, V> map;
    private final ReferenceQueue<K> queue = new ReferenceQueue<>();

    ConcurrentWeakIdentityHashMap(int initialCapacity) {
        this.map = new ConcurrentHashMap<>(initialCapacity);
    }

    public V get(Object key) {
        if (key == null) {
            return null;
        }
        purgeKeys();
        //noinspection SuspiciousMethodCalls
        return map.get(new Key<>(key, null));
    }

    @SuppressWarnings("NestedAssignment")
    private void purgeKeys() {
        Reference<? extends K> reference;
        while ((reference = this.queue.poll()) != null) {
            //noinspection SuspiciousMethodCalls
            this.map.remove(reference);
        }
    }

    @SuppressWarnings("UnusedReturnValue")
    public V putIfAbsent(K key, V value) {
        Objects.requireNonNull(key, "key");
        purgeKeys();
        return map.putIfAbsent(new Key<>(key, queue), value);
    }

    public V computeIfAbsent(K key,
            Function<? super K, ? extends V> mappingFunction) {
        Objects.requireNonNull(key, "key");
        Objects.requireNonNull(mappingFunction, "mappingFunction");
        purgeKeys();
        return map.computeIfAbsent(new Key<>(key, queue), kk -> mappingFunction.apply(kk.get()));
    }

    public int size() {
        purgeKeys();
        return map.size();
    }

    private static final class Key<T> extends WeakReference<T> {

        private final int hash;

        Key(T t, ReferenceQueue<T> queue) {
            super(t, queue);
            hash = System.identityHashCode(t);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Key)) {
                // won't happen, just avoid IDE warning.
                // all the objects in the delegated map are all Key
                return false;
            }
            return ((Key<?>) obj).get() == get();
        }

        @Override
        public int hashCode() {
            return hash;
        }

    }

}
