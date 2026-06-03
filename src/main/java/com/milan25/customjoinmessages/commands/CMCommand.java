package com.milan25.customjoinmessages.commands;

import com.milan25.customjoinmessages.CustomJoinMessages;
import com.milan25.customjoinmessages.utils.Colors;
import dev.jorel.commandapi.annotations.Command;
import dev.jorel.commandapi.annotations.Default;
import dev.jorel.commandapi.annotations.Permission;
import dev.jorel.commandapi.annotations.Subcommand;
import dev.jorel.commandapi.annotations.arguments.AGreedyStringArgument;
import dev.jorel.commandapi.annotations.arguments.AMultiLiteralArgument;
import dev.jorel.commandapi.annotations.arguments.AStringArgument;
import jdk.jfr.Description;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.UUID;

@Command("cm")
public class CMCommand {
    @Default
    public static void cm(Player player) {
        StringBuilder sb = new StringBuilder();
        sb.append(ChatColor.translateAlternateColorCodes('&', "&A_____CustomMessages commands:_____\n"));
        if (player.hasPermission("custommessages.set")) {
            sb.append(ChatColor.translateAlternateColorCodes('&', "&A/cm set join/leave [message] &F- Set your join or leave message. Don't forget to include your name.\n"));
            sb.append(ChatColor.translateAlternateColorCodes('&', "&A/cm show &F- View your custom messages.\n"));
            sb.append(ChatColor.translateAlternateColorCodes('&', "&A/cm reset &F- Reset your messages to default ones.\n"));
        }

        if (player.hasPermission("custommessages.admin")) {
            sb.append(ChatColor.translateAlternateColorCodes('&', "&A/cm adminset [player] join/leave [message] &F- Set join or leave message of another player (online or offline).\n"));
            sb.append(ChatColor.translateAlternateColorCodes('&', "&A/cm adminshow [player] &F- View custom messages of other players (online or offline).\n"));
            sb.append(ChatColor.translateAlternateColorCodes('&', "&A/cm adminreset [player] &F- Reset a player's messages to default (online or offline).\n"));
            sb.append(ChatColor.translateAlternateColorCodes('&', "&A/cm adminremove [player] join/leave/all &F- Remove a player's stored custom message(s) (online or offline).\n"));
            sb.append(ChatColor.translateAlternateColorCodes('&', "&A/cm adminreload &F- Reload configuration.\n"));
        }

        sb.append(ChatColor.translateAlternateColorCodes('&', "&A_________________________________\n"));

        player.sendMessage(sb.toString());
    }

    private static String prependDefaultColor(String str) {
        if (!str.contains("&")) {
            return "&e" + str;
        }
        return str;
    }

    private static String displayName(OfflinePlayer target) {
        String name = target.getName();
        return name != null ? name : target.getUniqueId().toString();
    }

    /**
     * Resolves a player name to an OfflinePlayer without a blocking Mojang lookup.
     * Checks online players first, then the server's cached offline players (anyone
     * who has joined this server before). Returns null if the name is unknown so the
     * caller can report it instead of fabricating an account.
     */
    private static OfflinePlayer resolveTarget(String targetName) {
        Player online = Bukkit.getPlayerExact(targetName);
        if (online != null) {
            return online;
        }
        for (OfflinePlayer cached : Bukkit.getOfflinePlayers()) {
            if (targetName.equalsIgnoreCase(cached.getName())) {
                return cached;
            }
        }
        return null;
    }

    private static void setPlayersMessage(Player source, OfflinePlayer target, String messageType, String message) {
        UUID playerId = target.getUniqueId();
        var plugin = CustomJoinMessages.getPlugin(CustomJoinMessages.class);

        if (messageType.equalsIgnoreCase("join")) {
            plugin.getConfig().createSection("saved_messages.join." + playerId);
            plugin.getConfig().set("saved_messages.join." + playerId, prependDefaultColor(message));
            plugin.saveConfig();
            source.sendMessage("Custom join message set to:\n" + prependDefaultColor(message));
        } else if (messageType.equalsIgnoreCase("leave")) {
            plugin.getConfig().createSection("saved_messages.leave." + playerId);
            plugin.getConfig().set("saved_messages.leave." + playerId, prependDefaultColor(message));
            plugin.saveConfig();
            source.sendMessage("Custom leave message set to:\n" + prependDefaultColor(message));
        } else {
            source.sendMessage("Usage: /cm set join/leave message");
        }
    }

    @Subcommand("set")
    @Description("Set your join or leave message, don't forget to include your name!")
    @Permission("custommessages.set")
    public static void cmSet(Player player, @AMultiLiteralArgument({"join", "leave"}) String messageType, @AGreedyStringArgument String message) {
        setPlayersMessage(player, player, messageType, message);
    }

    @Subcommand("adminset")
    @Description("Set join or leave message of another player (online or offline).")
    @Permission("custommessages.admin")
    public static void cmAdminSet(Player player, @AStringArgument String targetName, @AMultiLiteralArgument({"join", "leave"}) String messageType, @AGreedyStringArgument String message) {
        OfflinePlayer target = resolveTarget(targetName);
        if (target == null) {
            player.sendMessage("Player '" + targetName + "' was not found (they have never joined this server).");
            return;
        }
        setPlayersMessage(player, target, messageType, message);
    }

    private static void cmResetMessage(Player player, OfflinePlayer target) {
        UUID playerId = target.getUniqueId();
        var plugin = CustomJoinMessages.getPlugin(CustomJoinMessages.class);
        plugin.getConfig().set("saved_messages.join." + playerId, "");
        plugin.getConfig().set("saved_messages.leave." + playerId, "");
        plugin.saveConfig();
        player.sendMessage("Messages of " + displayName(target) + " were reset to default.");
    }

    @Subcommand("adminreset")
    @Description("Reset another player's messages to default (online or offline).")
    @Permission("custommessages.admin")
    public static void cmAdminReset(Player player, @AStringArgument String targetName) {
        OfflinePlayer target = resolveTarget(targetName);
        if (target == null) {
            player.sendMessage("Player '" + targetName + "' was not found (they have never joined this server).");
            return;
        }
        cmResetMessage(player, target);
    }

    @Subcommand("reset")
    @Description("Reset your messages to default ones.")
    @Permission("custommessages.set")
    public static void cmResetSelf(Player player) {
        cmResetMessage(player, player);
    }

    private static void cmRemoveMessage(Player player, OfflinePlayer target, String messageType) {
        UUID playerId = target.getUniqueId();
        var plugin = CustomJoinMessages.getPlugin(CustomJoinMessages.class);
        boolean removeJoin = messageType.equalsIgnoreCase("join") || messageType.equalsIgnoreCase("all");
        boolean removeLeave = messageType.equalsIgnoreCase("leave") || messageType.equalsIgnoreCase("all");

        if (removeJoin) {
            plugin.getConfig().set("saved_messages.join." + playerId, null);
        }
        if (removeLeave) {
            plugin.getConfig().set("saved_messages.leave." + playerId, null);
        }
        plugin.saveConfig();

        player.sendMessage("Removed " + messageType.toLowerCase() + " custom message(s) of " + displayName(target) + ".");
    }

    @Subcommand("adminremove")
    @Description("Remove a player's stored custom message(s), even while they are offline.")
    @Permission("custommessages.admin")
    public static void cmAdminRemove(Player player, @AStringArgument String targetName, @AMultiLiteralArgument({"join", "leave", "all"}) String messageType) {
        OfflinePlayer target = resolveTarget(targetName);
        if (target == null) {
            player.sendMessage("Player '" + targetName + "' was not found (they have never joined this server).");
            return;
        }
        cmRemoveMessage(player, target, messageType);
    }

    private static void cmShowMessage(Player player, OfflinePlayer target) {
        if (target == null) {
            player.sendMessage("Player not found");
            return;
        }

        UUID playerId = target.getUniqueId();
        var plugin = CustomJoinMessages.getPlugin(CustomJoinMessages.class);

        String joinMessage = plugin.getConfig().getString("saved_messages.join." + playerId, "");
        if (joinMessage.isEmpty()) {
            joinMessage = "default join message";
        }
        String joinPreview = Colors.translateHexColorCodes("&#", "", joinMessage);
        joinPreview = ChatColor.translateAlternateColorCodes('&', joinPreview);

        String leaveMessage = plugin.getConfig().getString("saved_messages.leave." + playerId, "");
        if (leaveMessage.isEmpty()) {
            leaveMessage = "default leave message";
        }
        String leavePreview = Colors.translateHexColorCodes("&#", "", leaveMessage);
        leavePreview = ChatColor.translateAlternateColorCodes('&', leavePreview);

        player.sendMessage("Custom messages set by " + displayName(target) + ":\n[Join] " + joinMessage + "\n[Join preview] " + joinPreview);
        player.sendMessage("[Leave] " + leaveMessage + "\n[Leave preview] " + leavePreview);
    }

    @Subcommand("adminshow")
    @Description("View custom messages of other players (online or offline).")
    @Permission("custommessages.admin")
    public static void cmAdminShow(Player player, @AStringArgument String targetName) {
        OfflinePlayer target = resolveTarget(targetName);
        if (target == null) {
            player.sendMessage("Player '" + targetName + "' was not found (they have never joined this server).");
            return;
        }
        cmShowMessage(player, target);
    }

    @Subcommand("show")
    @Description("View your custom messages.")
    @Permission("custommessages.set")
    public static void cmShowSelf(Player player) {
        cmShowMessage(player, player);
    }

    @Subcommand("adminreload")
    @Description("Reload configuration.")
    @Permission("custommessages.admin")
    public static void cmAdminReload(Player player) {
        CustomJoinMessages.getPlugin(CustomJoinMessages.class).reloadConfig();
        player.sendMessage("Config reloaded!");
    }
}
