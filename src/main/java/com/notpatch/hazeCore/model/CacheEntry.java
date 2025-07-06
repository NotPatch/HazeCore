package com.notpatch.hazeCore.model;

import lombok.Getter;

public class CacheEntry<V> {

    @Getter
    private final V value;
    private final long expiryTime;

    public CacheEntry(V value, long expiryTime) {
        this.value = value;
        this.expiryTime = expiryTime;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() > expiryTime;
    }
}
