package com.milan25.customjoinmessages.events;

import com.milan25.customjoinmessages.CustomJoinMessages;
import com.milan25.customjoinmessages.utils.Colors;
import net.ess3.api.events.AfkStatusChangeEvent;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import me.clip.placeholderapi.PlaceholderAPI;

/**
 * Broadcasts custom AFK / return messages, driven by EssentialsX's AFK status.
 * Only registered when EssentialsX is present (see CustomJoinMessages#onEnable).
 * Mirrors the join/leave handling in {@link CMEvents}: per-player messages are
 * stored under saved_messages.afk.&lt;uuid&gt; / saved_messages.return.&lt;uuid&gt;,
 * falling back to the custom_afk_message / custom_return_message defaults.
 */
public class AFKEvents implements Listener {
    private final CustomJoinMessages plugin;

    public AFKEvents(CustomJoinMessages plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAfkStatusChange(AfkStatusChangeEvent event) {
        Player player = event.getAffected().getBase();
        boolean nowAfk = event.getValue();

        // Per-player opt-out. When disabled (via /cm afktoggle) we skip the chat
        // broadcast entirely - the player still shows as AFK in the tab list,
        // since that is handled by Essentials, not this plugin. Defaults to on.
        if (!this.plugin.getConfig().getBoolean("afk_broadcast." + player.getUniqueId(), true)) {
            return;
        }

        String savedKey = nowAfk ? "saved_messages.afk." : "saved_messages.return.";
        String defaultKey = nowAfk ? "custom_afk_message" : "custom_return_message";
        String prefixKey = nowAfk ? "custom_afk_message_prefix" : "custom_return_message_prefix";

        String message = this.plugin.getConfig().getString(savedKey + player.getUniqueId(), "");

        if (message.isEmpty()) {
            String defaultMessage = this.plugin.getConfig().getString(defaultKey, "");

            if (!defaultMessage.isEmpty()) {
                message = defaultMessage.replace("{NAME}", player.getName());
            }
        }

        String prefix = this.plugin.getConfig().getString(prefixKey, "");
        message = prefix + message;

        String withPlaceholdersFilled = PlaceholderAPI.setPlaceholders(player, message);

        String replacedColors = Colors.translateHexColorCodes("&#", "", withPlaceholdersFilled);
        replacedColors = ChatColor.translateAlternateColorCodes('&', replacedColors);

        if (!withPlaceholdersFilled.isEmpty()) {
            this.plugin.getServer().broadcastMessage(replacedColors);
        }
    }
}
