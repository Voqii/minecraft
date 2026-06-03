package com.milan25.customjoinmessages;

import com.milan25.customjoinmessages.commands.CMCommand;
import com.milan25.customjoinmessages.events.AFKEvents;
import com.milan25.customjoinmessages.events.CMEvents;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIPaperConfig;
import org.bukkit.plugin.java.JavaPlugin;

public final class CustomJoinMessages extends JavaPlugin {

    @Override
    public void onLoad() {
        CommandAPI.onLoad(new CommandAPIPaperConfig(this));
        CommandAPI.registerCommand(CMCommand.class);
    }

    @Override
    public void onEnable() {
        CommandAPI.onEnable();

        this.getLogger().info("CustomMessages plugin loaded");


        this.getConfig().options().copyDefaults(true);
        this.saveDefaultConfig();

        if (this.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            this.getServer().getPluginManager().registerEvents(new CMEvents(this), this);
        } else {
            this.getLogger().warning("Could not find PlaceholderAPI! This plugin is required.");
            this.getServer().getPluginManager().disablePlugin(this);
            return;
        }

        if (this.getServer().getPluginManager().getPlugin("Essentials") != null) {
            this.getServer().getPluginManager().registerEvents(new AFKEvents(this), this);
            this.getLogger().info("Hooked into EssentialsX - AFK/return messages enabled.");
        } else {
            this.getLogger().info("EssentialsX not found - AFK/return messages disabled (join/leave still work).");
        }
    }

    @Override
    public void onDisable() {
        CommandAPI.onDisable();
    }
}
