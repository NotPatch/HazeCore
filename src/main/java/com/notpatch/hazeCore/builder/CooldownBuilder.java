package com.notpatch.hazeCore.builder;

import com.notpatch.hazeCore.manager.CooldownManager;
import org.bukkit.entity.Player;

import java.util.concurrent.TimeUnit;

public class CooldownBuilder {

    private final CooldownManager manager;
    private String key;
    private Player player;
    private long duration;
    private TimeUnit timeUnit = TimeUnit.SECONDS;

    public CooldownBuilder(CooldownManager manager) {
        this.manager = manager;
    }

    public CooldownBuilder key(String key) {
        this.key = key;
        return this;
    }

    public CooldownBuilder player(Player player) {
        this.player = player;
        return this;
    }

    public CooldownBuilder duration(long duration) {
        this.duration = duration;
        return this;
    }

    public CooldownBuilder timeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
        return this;
    }

    public CooldownBuilder seconds(long seconds) {
        this.duration = seconds;
        this.timeUnit = TimeUnit.SECONDS;
        return this;
    }

    public CooldownBuilder minutes(long minutes) {
        this.duration = minutes;
        this.timeUnit = TimeUnit.MINUTES;
        return this;
    }

    public CooldownBuilder hours(long hours) {
        this.duration = hours;
        this.timeUnit = TimeUnit.HOURS;
        return this;
    }

    public void apply() {
        if (key == null || player == null || duration <= 0) {
            throw new IllegalStateException("Eksik parametreler!");
        }
        manager.setCooldown(key, player, duration, timeUnit);
    }
}
