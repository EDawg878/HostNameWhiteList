package com.edawg878.hostnamewhitelist;

import net.md_5.bungee.api.CommandSender;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.plugin.Command;

/**
 * @author EDawg878 <EDawg878@gmail.com>
 */
public class HostNameWhiteListCommand extends Command {

    private final HostNameWhiteList plugin;

    public HostNameWhiteListCommand(HostNameWhiteList plugin) {
        super("hostnamewhitelist", "hostnamewhitelist.admin", "hnw");
        this. plugin = plugin;
    }

    @Override
    public void execute(CommandSender cs, String[] args) {
        if(args.length >= 1 && args[0].equalsIgnoreCase("reload")) {
            plugin.loadConfig();
            cs.sendMessage(new TextComponent("Configuration reloaded"));
        }
    }
}
