package com.notpatch.hazeCore.manager;

import com.notpatch.hazeCore.model.CacheEntry;
import lombok.Getter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CacheManager<K, V> {
    private static CacheManager<?, ?> instance;
    private final Map<K, CacheEntry<V>> cache = new ConcurrentHashMap<>();
    private final ScheduledExecutorService cleanup = Executors.newSingleThreadScheduledExecutor();
    
    @Getter
    private final long cleanupInterval;

    private CacheManager(long cleanupInterval, TimeUnit unit) {
        this.cleanupInterval = unit.toMillis(cleanupInterval);
        startCleanupTask();
    }

    @SuppressWarnings("unchecked")
    public static <K, V> CacheManager<K, V> getInstance() {
        if (instance == null) {
            instance = new CacheManager<K, V>(5, TimeUnit.MINUTES);
        }
        return (CacheManager<K, V>) instance;
    }

    private void startCleanupTask() {
        cleanup.scheduleAtFixedRate(this::cleanupExpiredEntries, 
            cleanupInterval, cleanupInterval, TimeUnit.MILLISECONDS);
    }

    public void put(K key, V value, long duration, TimeUnit unit) {
        cache.put(key, new CacheEntry<>(value, 
            System.currentTimeMillis() + unit.toMillis(duration)));
    }
    
    public V get(K key) {
        CacheEntry<V> entry = cache.get(key);
        if (entry != null && !entry.isExpired()) {
            return entry.getValue();
        }
        cache.remove(key);
        return null;
    }
    
    public void remove(K key) {
        cache.remove(key);
    }
    
    public void clear() {
        cache.clear();
    }
    
    public boolean contains(K key) {
        return get(key) != null;
    }
    
    private void cleanupExpiredEntries() {
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
    }
    
    public void shutdown() {
        cleanup.shutdown();
        cache.clear();
        instance = null;
    }
    
    public int size() {
        return (int) cache.entrySet().stream()
            .filter(entry -> !entry.getValue().isExpired())
            .count();
    }
}