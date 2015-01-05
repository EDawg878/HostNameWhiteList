package com.edawg878.hostnamewhitelist.bukkit;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * @author EDawg878 <EDawg878@gmail.com>
 */
public class BukkitCommand implements CommandExecutor {

    private final BukkitHostNameWhiteList plugin;

    public BukkitCommand(BukkitHostNameWhiteList plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender cs, Command command, String label, String[] args) {
        if (args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfig();
            cs.sendMessage("Configuration reloaded");
            return true;
        }
        return false;
    }

}
