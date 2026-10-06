package com.milan25.customjoinmessages;

import com.milan25.customjoinmessages.commands.CMCommandTree;
import com.milan25.customjoinmessages.events.AFKEvents;
import com.milan25.customjoinmessages.events.CMEvents;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.bukkit.plugin.java.JavaPlugin;

public final class CustomJoinMessages extends JavaPlugin {

    @Override
    public void onEnable() {
        // /cm is registered through Paper's Brigadier API rather than CommandAPI, whose
        // per-version NMS adapters broke on 26.3 (NoClassDefFoundError FuelValues).
        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event ->
                event.registrar().register(CMCommandTree.build(), "Custom join, leave and AFK messages"));

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
}
