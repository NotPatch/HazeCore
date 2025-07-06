package com.notpatch.hazeCore.util;


import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class NLogger {

    public static void info(String message) {
        Bukkit.getConsoleSender().sendMessage("§7[§fHazeLands§7] " + ChatColor.GREEN + message);
    }

    public static void warn(String message) {
        Bukkit.getConsoleSender().sendMessage("§7[§fHazeLands§7] " + ChatColor.YELLOW + message);
    }

    public static void error(String message) {
        Bukkit.getConsoleSender().sendMessage("§7[§fHazeLands§7] " + ChatColor.RED + message);
    }

    public static void exception(Exception e){
        Bukkit.getConsoleSender().sendMessage("§7[§fHazeLands§7] | " + e.getMessage());
    }

}