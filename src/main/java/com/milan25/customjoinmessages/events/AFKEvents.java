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
    // Built-in fallback messages used when neither the player nor the server admin
    // has set one. This keeps AFK/return announcements working out of the box for
    // players without a custom (paid-tier) message, now that EssentialsX's own AFK
    // broadcasts are turned off. {NAME} is replaced with the player's name. The admin
    // can override these globally via custom_afk_message / custom_return_message.
    private static final String DEFAULT_AFK_MESSAGE = "&7{NAME} is now AFK";
    private static final String DEFAULT_RETURN_MESSAGE = "&7{NAME} is no longer AFK";

    private final CustomJoinMessages plugin;

    public AFKEvents(CustomJoinMessages plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAfkStatusChange(AfkStatusChangeEvent event) {
        Player player = event.getAffected().getBase();
        boolean nowAfk = event.getValue();

        // Players can silence their own AFK/return broadcast via /cm toggle.
        String type = nowAfk ? "afk" : "return";
        if (this.plugin.getConfig().getBoolean("silenced." + type + "." + player.getUniqueId(), false)) {
            return;
        }

        String savedKey = nowAfk ? "saved_messages.afk." : "saved_messages.return.";
        String defaultKey = nowAfk ? "custom_afk_message" : "custom_return_message";
        String prefixKey = nowAfk ? "custom_afk_message_prefix" : "custom_return_message_prefix";

        String message = this.plugin.getConfig().getString(savedKey + player.getUniqueId(), "");

        if (message.isEmpty()) {
            // No per-player message: fall back to the admin-configured default, and
            // if that is empty too, to the built-in default so it is never silent.
            String defaultMessage = this.plugin.getConfig().getString(defaultKey, "");
            if (defaultMessage.isEmpty()) {
                defaultMessage = nowAfk ? DEFAULT_AFK_MESSAGE : DEFAULT_RETURN_MESSAGE;
            }
            message = defaultMessage.replace("{NAME}", player.getName());
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
