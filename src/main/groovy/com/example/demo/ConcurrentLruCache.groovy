package com.example.demo

import java.util.concurrent.locks.ReentrantLock
import java.util.function.BiConsumer
import java.util.function.Function

class ConcurrentLruCache<K, V> extends AbstractLogging {

    private final ReentrantLock[] locks
    private final LinkedHashMap<K, V>[] buckets
    private final BiConsumer<K, V> evictListener

    protected final int mask

    ConcurrentLruCache(int concurrency, int bucketSize, BiConsumer<K, V> evictListener) {
        if (Integer.bitCount(concurrency) != 1) {
            throw new IllegalArgumentException("concurrency must be a power of 2")
        }

        this.evictListener = evictListener

        mask = concurrency - 1
        buckets = new LinkedHashMap[concurrency]
        locks = new ReentrantLock[concurrency]

        for (int i = 0; i < concurrency; i++) {
            buckets[i] = new LinkedHashMap<K, V>(bucketSize) {

                @Override
                protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
                    def removed = this.size() > bucketSize

                    if (evictListener != null && removed) {
                        evictListener.accept(eldest.key, eldest.value)
                    }

                    return removed
                }
            }

            locks[i] = new ReentrantLock()
        }
    }

    V lookup(final K key, Function<K, V> function) {
        def i = getIndex(key)

        def lock = locks[i]
        lock.lock()

        try {
            return buckets[i].computeIfAbsent(key, function)
        } finally {
            lock.unlock()
        }
    }

    V lookup(final K key) {
        def i = getIndex(key)

        def lock = locks[i]
        lock.lock()

        try {
            return buckets[i].get(key)
        } finally {
            lock.unlock()
        }
    }

    V put(final K key, final V value) {
        def i = getIndex(key)

        def lock = locks[i]
        lock.lock()

        try {
            return buckets[i].put(key, value)
        } finally {
            lock.unlock()
        }
    }

    V remove(final K key) {
        def i = getIndex(key)

        def lock = locks[i]
        lock.lock()

        try {
            return buckets[i].remove(key)
        } finally {
            lock.unlock()
        }
    }

    protected int getIndex(final K key) {
        return Objects.hashCode(key) & mask
    }
}
