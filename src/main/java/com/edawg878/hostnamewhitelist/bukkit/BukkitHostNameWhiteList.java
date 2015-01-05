package com.edawg878.hostnamewhitelist.bukkit;

import com.edawg878.hostnamewhitelist.common.Util;
import org.bukkit.ChatColor;
import org.bukkit.configuration.Configuration;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerLoginEvent.Result;
import org.bukkit.event.server.ServerListPingEvent;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Set;

/**
 * @author EDawg878 <EDawg878@gmail.com>
 */
public class BukkitHostNameWhiteList extends JavaPlugin implements Listener {

    private Set<String> validHostNames;
    private String warning;
    private boolean ignoreCase;
    private boolean blockLegacy;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        reloadConfig();
        PluginManager pm = getServer().getPluginManager();
        getCommand("hostnamewhitelist").setExecutor(new BukkitCommand(this));
        pm.registerEvents(this, this);
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        Configuration config = getConfig();
        warning = ChatColor.translateAlternateColorCodes('&', config.getString("warning"));
        ignoreCase = config.getBoolean("ignore-case", true);
        validHostNames = Util.getHostNames(config.getStringList("allowed-host-names"), ignoreCase);
        blockLegacy = config.getBoolean("block-legacy", true);
    }

    @EventHandler
    public void onLogin(PlayerLoginEvent event) {
        String hostname = event.getHostname();
        int port = hostname.indexOf(":");
        if (port != -1) {
            hostname = hostname.substring(0, port);
        }
        if (isBlocked(hostname)) {
            event.disallow(Result.KICK_OTHER, warning);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPing(ServerListPingEvent event) {
        if (isBlocked(event.getAddress().getHostName())) {
            event.setMotd(warning);
        }
    }

    private boolean isBlocked(String host) {
        if (host == null) {
            return blockLegacy;
        } else {
            if (ignoreCase) {
                host = host.toLowerCase();
            }
            return !validHostNames.contains(host);
        }
    }

}
