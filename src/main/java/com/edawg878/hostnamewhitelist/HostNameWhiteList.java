package com.edawg878.hostnamewhitelist;

import com.google.common.io.ByteStreams;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ServerPing;
import net.md_5.bungee.api.connection.PendingConnection;
import net.md_5.bungee.api.event.PreLoginEvent;
import net.md_5.bungee.api.event.ProxyPingEvent;
import net.md_5.bungee.api.plugin.Event;
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
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;

/**
 * @author EDawg878 <EDawg878@gmail.com>
 */
public class HostNameWhiteList extends Plugin implements Listener {

    private static final ConfigurationProvider configProvider = ConfigurationProvider.getProvider(YamlConfiguration.class);
    private HostNameWhiteList instance;
    private Set<String> validHostNames;
    private String warning;
    private boolean checkSubdomains;
    private boolean ignoreCase;

    @Override
    public void onEnable() {
        instance = this;
        loadConfig();
        PluginManager pm = getProxy().getPluginManager();
        pm.registerCommand(this, new HostNameWhiteListCommand(this));
        pm.registerListener(this, this);
    }

    protected void loadConfig() {
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
            Set<String> actualHostNames = new HashSet<>(config.getStringList("allowed-host-names"));
            validHostNames = adjustHostNames(actualHostNames);
            checkSubdomains = config.getBoolean("check-subdomains");
            ignoreCase = config.getBoolean("ignore-case");
        } catch (IOException e) {
            getLogger().log(Level.SEVERE, "Error loading configuration", e);
        }
    }

    private Set<String> adjustHostNames(Set<String> hosts) {
        if(ignoreCase) {
            Set<String> adjusted = new HashSet<>();
            for(String host : hosts) {
                adjusted.add(host.toLowerCase());
            }
            return adjusted;
        }
        return hosts;
    }

    private boolean isBlocked(PendingConnection conn) {
        InetSocketAddress address = conn.getVirtualHost();
        String hostname = checkSubdomains ? address.getHostName() : address.getAddress().getCanonicalHostName();
        int index = hostname.indexOf(':');
        if (index != -1) {
            hostname = hostname.substring(0, index);
        }
        if (ignoreCase) {
            hostname = hostname.toLowerCase();
        }
        return !validHostNames.contains(hostname);
    }

    @EventHandler
    public void onPreLogin(PreLoginEvent event) {
        if (isBlocked(event.getConnection())) {
            event.setCancelled(true);
            event.setCancelReason(warning);
        }
    }

    @EventHandler(priority = EventPrority.HIGH)
    public void onPing(ProxyPingEvent event) {
        if (isBlocked(event.getConnection())) {
            ServerPing ping = event.getResponse();
            ping.setDescription(warning);
            event.setResponse(ping);
        }
    }

}
