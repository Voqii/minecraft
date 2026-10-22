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
    // has set one. {NAME} is replaced with the player's name. The admin can override
    // these globally via custom_afk_message / custom_return_message.
    //
    // This mirrors EssentialsX's old behaviour: going AFK is silent, returning is
    // announced. The AFK default is intentionally empty so players without a custom
    // (paid-tier) message broadcast nothing when they go AFK, but are still announced
    // on return now that EssentialsX's own AFK broadcasts are turned off.
    private static final String DEFAULT_AFK_MESSAGE = "";
    // The &7 after {NAME} re-asserts grey, since the player's Essentials display name
    // may carry its own colour (a coloured nickname).
    private static final String DEFAULT_RETURN_MESSAGE = "&7{NAME}&7 is no longer AFK.";

    // Every AFK/return broadcast starts with "* ", EssentialsX style, so they read as status
    // lines rather than chat. Per-player custom (paid-tier) messages get a white asterisk and
    // defaults a grey one, so you can tell them apart at a glance.
    private static final String CUSTOM_MARKER = "&f* ";
    private static final String DEFAULT_MARKER = "&7* ";

    private final CustomJoinMessages plugin;

    public AFKEvents(CustomJoinMessages plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAfkStatusChange(AfkStatusChangeEvent event) {
        // EssentialsX clears a stale AFK flag (it's saved in userdata) during PlayerJoinEvent,
        // and clears it again on quit. Broadcasting those would print "is no longer AFK"
        // before the join message / alongside the leave message, so only real returns count.
        AfkStatusChangeEvent.Cause cause = event.getCause();
        if (cause == AfkStatusChangeEvent.Cause.JOIN || cause == AfkStatusChangeEvent.Cause.QUIT) {
            return;
        }

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
        String marker = message.isEmpty() ? DEFAULT_MARKER : CUSTOM_MARKER;

        if (message.isEmpty()) {
            // No per-player message: fall back to the admin-configured default, and
            // if that is empty too, to the built-in default so it is never silent.
            String defaultMessage = this.plugin.getConfig().getString(defaultKey, "");
            if (defaultMessage.isEmpty()) {
                defaultMessage = nowAfk ? DEFAULT_AFK_MESSAGE : DEFAULT_RETURN_MESSAGE;
            }
            // Use the player's Essentials display name (their coloured nickname when set)
            // so default messages aren't a flat grey name; fall back to the plain name.
            String displayName = event.getAffected().getDisplayName();
            if (displayName == null || displayName.isEmpty()) {
                displayName = player.getName();
            }
            message = defaultMessage.replace("{NAME}", displayName);
        }

        String prefix = this.plugin.getConfig().getString(prefixKey, "");
        message = prefix + message;
        if (!message.isEmpty() && !startsWithAsterisk(message)) {
            message = marker + message;
        }

        String withPlaceholdersFilled = PlaceholderAPI.setPlaceholders(player, message);

        String replacedColors = Colors.translateHexColorCodes("&#", "", withPlaceholdersFilled);
        replacedColors = ChatColor.translateAlternateColorCodes('&', replacedColors);

        if (!withPlaceholdersFilled.isEmpty()) {
            this.plugin.getServer().broadcastMessage(replacedColors);
        }
    }

    // True if the visible text already starts with "*" once colour codes (&a, &#RRGGBB) are
    // ignored, so a player who typed their own asterisk doesn't get two.
    private static boolean startsWithAsterisk(String message) {
        return message.replaceAll("(?i)&(#[0-9a-f]{6}|[0-9a-fk-or])", "").stripLeading().startsWith("*");
    }
}
