package com.edawg878.hostnamewhitelist.bungee;

import com.edawg878.hostnamewhitelist.common.Util;
import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.api.plugin.Plugin;
import net.md_5.bungee.api.plugin.PluginManager;
import net.md_5.bungee.config.Configuration;
import net.md_5.bungee.config.ConfigurationProvider;
import net.md_5.bungee.config.YamlConfiguration;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.util.Set;
import java.util.logging.Level;

/**
 * @author EDawg878 <EDawg878@gmail.com>
 */
public class BungeeHostNameWhiteList extends Plugin implements Listener {

    private static final ConfigurationProvider configProvider = ConfigurationProvider.getProvider(YamlConfiguration.class);
    private Set<String> validHostNames;
    private String warning;
    private boolean ignoreCase;
    private boolean blockLegacy;

    @Override
    public void onEnable() {
        reloadConfig();
        PluginManager pm = getProxy().getPluginManager();
        pm.registerCommand(this, new BungeeCommand(this));
        pm.registerListener(this, this);
    }

    public void reloadConfig() {
        try {
            File file = new File(getDataFolder(), "config.yml");
            if (!file.exists()) {
                getDataFolder().mkdirs();
                file.createNewFile();
                try (InputStream in = getResourceAsStream("config.yml");
                     FileOutputStream out = new FileOutputStream(file)) {
                    ByteStreams.copy(in, out);
                }
            }
            Configuration config = configProvider.load(file);
            warning = ChatColor.translateAlternateColorCodes('&', config.getString("warning"));
            ignoreCase = config.getBoolean("ignore-case", true);
            validHostNames = Util.getHostNames(config.getStringList("allowed-host-names"), ignoreCase);
            blockLegacy = config.getBoolean("block-legacy", true);
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Error loading configuration", e);
        }
    }

    @EventHandler
    public void onPreLogin(PreLoginEvent event) {
        if (isBlocked(event.getConnection())) {
            event.setCancelled(true);
            event.setCancelReason(warning);
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPing(ProxyPingEvent event) {
        if (isBlocked(event.getConnection())) {
            ServerPing ping = event.getResponse();
            ping.setDescription(warning);
            event.setResponse(ping);
        }
    }

    private boolean isBlocked(PendingConnection conn) {
        InetSocketAddress address = conn.getVirtualHost();
        if (conn.isLegacy() || address == null) {
            return blockLegacy;
        } else {
            String hostname = ignoreCase ? address.getHostName().toLowerCase() : address.getHostName();
            return !validHostNames.contains(hostname);
        }
    }

}
