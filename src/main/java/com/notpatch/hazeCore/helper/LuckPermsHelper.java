package com.notpatch.hazeCore.helper;

import lombok.Getter;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.Node;
import net.luckperms.api.node.types.InheritanceNode;
import net.luckperms.api.node.types.PermissionNode;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.RegisteredServiceProvider;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class LuckPermsHelper {

    @Getter
    private static LuckPerms api = null;

    public static boolean setupLuckPerms() {
        RegisteredServiceProvider<LuckPerms> provider = Bukkit.getServicesManager().getRegistration(LuckPerms.class);
        if (provider != null) {
            api = provider.getProvider();
            return true;
        }
        return false;
    }

    public static boolean addGroup(OfflinePlayer player, String group) {
        if (api == null) return false;
        try {
            api.getUserManager().modifyUser(player.getUniqueId(), user -> {
                InheritanceNode node = InheritanceNode.builder(group).build();
                user.data().add(node);
            }).join();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean removeGroup(OfflinePlayer player, String group) {
        if (api == null) return false;
        try {
            api.getUserManager().modifyUser(player.getUniqueId(), user -> {
                InheritanceNode node = InheritanceNode.builder(group).build();
                user.data().remove(node);
            }).join();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean addPermission(OfflinePlayer player, String permission) {
        if (api == null) return false;
        try {
            api.getUserManager().modifyUser(player.getUniqueId(), user -> {
                Node node = PermissionNode.builder(permission).build();
                user.data().add(node);
            }).join();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean removePermission(OfflinePlayer player, String permission) {
        if (api == null) return false;
        try {
            api.getUserManager().modifyUser(player.getUniqueId(), user -> {
                Node node = PermissionNode.builder(permission).build();
                user.data().remove(node);
            }).join();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean addTemporaryPermission(OfflinePlayer player, String permission, long seconds) {
        if (api == null) return false;
        try {
            api.getUserManager().modifyUser(player.getUniqueId(), user -> {
                Node node = PermissionNode.builder(permission)
                        .expiry(seconds, TimeUnit.SECONDS)
                        .build();
                user.data().add(node);
            }).join();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean addTemporaryGroup(OfflinePlayer player, String group, long seconds) {
        if (api == null) return false;
        try {
            api.getUserManager().modifyUser(player.getUniqueId(), user -> {
                Node node = InheritanceNode.builder(group)
                        .expiry(seconds, TimeUnit.SECONDS)
                        .build();
                user.data().add(node);
            }).join();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static Set<String> getGroups(OfflinePlayer player) {
        if (api == null) return new HashSet<>();
        User user = api.getUserManager().getUser(player.getUniqueId());
        if (user == null) return new HashSet<>();
        
        return user.getInheritedGroups(user.getQueryOptions())
                .stream()
                .map(Group::getName)
                .collect(Collectors.toSet());
    }

    public static String getPrimaryGroup(OfflinePlayer player) {
        if (api == null) return null;
        User user = api.getUserManager().getUser(player.getUniqueId());
        return user != null ? user.getPrimaryGroup() : null;
    }

    public static boolean setPrimaryGroup(OfflinePlayer player, String group) {
        if (api == null) return false;
        try {
            api.getUserManager().modifyUser(player.getUniqueId(), user -> {
                user.setPrimaryGroup(group);
            }).join();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean hasPermission(OfflinePlayer player, String permission) {
        if (api == null) return false;
        User user = api.getUserManager().getUser(player.getUniqueId());
        if (user == null) return false;
        
        return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
    }

    public static String getPrefix(OfflinePlayer player) {
        if (api == null) return "";
        User user = api.getUserManager().getUser(player.getUniqueId());
        if (user == null) return "";
        
        return user.getCachedData().getMetaData().getPrefix() != null ? 
               user.getCachedData().getMetaData().getPrefix() : "";
    }

    public static String getSuffix(OfflinePlayer player) {
        if (api == null) return "";
        User user = api.getUserManager().getUser(player.getUniqueId());
        if (user == null) return "";
        
        return user.getCachedData().getMetaData().getSuffix() != null ? 
               user.getCachedData().getMetaData().getSuffix() : "";
    }

    public static Set<String> getAllGroups() {
        if (api == null) return new HashSet<>();
        return api.getGroupManager().getLoadedGroups()
                .stream()
                .map(Group::getName)
                .collect(Collectors.toSet());
    }

    public static void refreshUser(Player player) {
        if (api == null) return;
        User user = api.getUserManager().getUser(player.getUniqueId());
        if (user != null) {
            api.getUserManager().saveUser(user);
        }
    }

    public static CompletableFuture<User> reloadUserAsync(UUID uuid) {
        if (api == null) return CompletableFuture.completedFuture(null);
        return api.getUserManager().loadUser(uuid);
    }
}