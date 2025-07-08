package com.notpatch.hazeCore.helper;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public class VaultHelper {
    private static Economy economy = null;

    public static boolean setupEconomy() {
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        economy = rsp.getProvider();
        return economy != null;
    }

    public static double getBalance(OfflinePlayer player) {
        return economy != null ? economy.getBalance(player) : 0.0;
    }

    public static boolean has(OfflinePlayer player, double amount) {
        return economy != null && economy.has(player, amount);
    }

    public static boolean withdraw(OfflinePlayer player, double amount) {
        if (economy != null && economy.has(player, amount)) {
            economy.withdrawPlayer(player, amount);
            return true;
        }
        return false;
    }

    public static boolean deposit(OfflinePlayer player, double amount) {
        if (economy != null) {
            economy.depositPlayer(player, amount);
            return true;
        }
        return false;
    }

    public static boolean transfer(OfflinePlayer from, OfflinePlayer to, double amount) {
        if (economy != null && economy.has(from, amount)) {
            economy.withdrawPlayer(from, amount);
            economy.depositPlayer(to, amount);
            return true;
        }
        return false;
    }
    
    public static String format(double amount) {
        return economy != null ? economy.format(amount) : String.format("%.2f", amount);
    }

    public static boolean hasAccount(OfflinePlayer player) {
        return economy != null && economy.hasAccount(player);
    }

    public static boolean createAccount(OfflinePlayer player) {
        return economy != null && economy.createPlayerAccount(player);
    }
}