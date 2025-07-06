package com.notpatch.hazeCore.manager;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.Getter;
import org.bukkit.entity.Player;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class CooldownManager {
    
    @Getter
    private final Cache<String, Cache<UUID, Long>> cooldownMap;
    
    public CooldownManager() {
        // Ana cache: Her cooldown türü için ayrı bir cache tutar
        this.cooldownMap = CacheBuilder.newBuilder()
                .expireAfterAccess(1, TimeUnit.HOURS) // Kullanılmayan cooldown türleri 1 saat sonra temizlenir
                .build();
    }

    public void setCooldown(String key, Player player, long duration, TimeUnit timeUnit) {
        Cache<UUID, Long> cooldowns = cooldownMap.asMap().computeIfAbsent(key, k -> 
            CacheBuilder.newBuilder()
                    .expireAfterWrite(duration * 2, timeUnit) // Otomatik temizleme
                    .build()
        );
        
        cooldowns.put(player.getUniqueId(), 
                System.currentTimeMillis() + timeUnit.toMillis(duration));
    }

    public boolean isOnCooldown(String key, Player player) {
        Cache<UUID, Long> cooldowns = cooldownMap.getIfPresent(key);
        if (cooldowns == null) return false;

        Long endTime = cooldowns.getIfPresent(player.getUniqueId());
        return endTime != null && endTime > System.currentTimeMillis();
    }

    public long getRemainingTime(String key, Player player) {
        Cache<UUID, Long> cooldowns = cooldownMap.getIfPresent(key);
        if (cooldowns == null) return 0L;

        Long endTime = cooldowns.getIfPresent(player.getUniqueId());
        if (endTime == null) return 0L;

        long remaining = endTime - System.currentTimeMillis();
        return Math.max(0, remaining);
    }

    public String getFormattedRemainingTime(String key, Player player) {
        long remainingMs = getRemainingTime(key, player);
        if (remainingMs == 0) return "0s";

        long seconds = remainingMs / 1000;
        long minutes = seconds / 60;
        seconds = seconds % 60;
        
        StringBuilder builder = new StringBuilder();
        if (minutes > 0) {
            builder.append(minutes).append("m ");
        }
        if (seconds > 0 || minutes == 0) {
            builder.append(seconds).append("s");
        }
        return builder.toString().trim();
    }

    public void clearCooldown(String key, Player player) {
        Cache<UUID, Long> cooldowns = cooldownMap.getIfPresent(key);
        if (cooldowns != null) {
            cooldowns.invalidate(player.getUniqueId());
        }
    }

    public void clearAllCooldowns(Player player) {
        UUID uuid = player.getUniqueId();
        cooldownMap.asMap().values().forEach(cache -> cache.invalidate(uuid));
    }

    public void clearCooldownType(String key) {
        cooldownMap.invalidate(key);
    }

    public void clearAll() {
        cooldownMap.invalidateAll();
    }
}