package com.milan25.customjoinmessages.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.Commands;
import java.util.List;
import java.util.function.Function;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * /cm command tree, registered through Paper's Brigadier lifecycle API.
 * Replaces the shaded CommandAPI, which needed an NMS adapter per Minecraft version
 * and broke on every release (26.3: NoClassDefFoundError FuelValues).
 */
public final class CMCommandTree {
    private static final String PERM_SET = "custommessages.set";
    private static final String PERM_ADMIN = "custommessages.admin";
    private static final List<String> MESSAGE_TYPES = List.of("join", "leave", "afk", "return");

    @FunctionalInterface
    private interface PlayerAction {
        void run(Player player, CommandContext<CommandSourceStack> ctx);
    }

    private CMCommandTree() {
    }

    public static LiteralCommandNode<CommandSourceStack> build() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("cm")
                .executes(player((p, ctx) -> CMCommand.cm(p)));

        // /cm set <type> <message>
        root.then(forEach(sub("set", PERM_SET), MESSAGE_TYPES, type -> Commands.literal(type)
                .then(message().executes(player((p, ctx) -> CMCommand.cmSet(p, type, msg(ctx)))))));
        // /cm toggle afk|return
        root.then(forEach(sub("toggle", PERM_SET), List.of("afk", "return"), type -> Commands.literal(type)
                .executes(player((p, ctx) -> CMCommand.cmToggle(p, type)))));
        root.then(sub("show", PERM_SET).executes(player((p, ctx) -> CMCommand.cmShowSelf(p))));
        root.then(sub("reset", PERM_SET).executes(player((p, ctx) -> CMCommand.cmResetSelf(p))));

        // /cm adminset <player> <type> <message>
        root.then(sub("adminset", PERM_ADMIN).then(forEach(target(), MESSAGE_TYPES, type -> Commands.literal(type)
                .then(message().executes(player((p, ctx) -> CMCommand.cmAdminSet(p, target(ctx), type, msg(ctx))))))));
        // /cm adminremove <player> <type|all>
        root.then(sub("adminremove", PERM_ADMIN).then(forEach(target(), List.of("join", "leave", "afk", "return", "all"),
                type -> Commands.literal(type)
                        .executes(player((p, ctx) -> CMCommand.cmAdminRemove(p, target(ctx), type))))));
        root.then(sub("adminshow", PERM_ADMIN).then(target()
                .executes(player((p, ctx) -> CMCommand.cmAdminShow(p, target(ctx))))));
        root.then(sub("adminreset", PERM_ADMIN).then(target()
                .executes(player((p, ctx) -> CMCommand.cmAdminReset(p, target(ctx))))));
        root.then(sub("adminreload", PERM_ADMIN).executes(player((p, ctx) -> CMCommand.cmAdminReload(p))));

        return root.build();
    }

    private static LiteralArgumentBuilder<CommandSourceStack> sub(String name, String permission) {
        return Commands.literal(name).requires(src -> src.getSender().hasPermission(permission));
    }

    /** Word argument for a player name (online or offline); suggests online players. */
    private static RequiredArgumentBuilder<CommandSourceStack, String> target() {
        return Commands.argument("targetName", StringArgumentType.word())
                .suggests((ctx, builder) -> {
                    String typed = builder.getRemainingLowerCase();
                    for (Player online : Bukkit.getOnlinePlayers()) {
                        if (online.getName().toLowerCase().startsWith(typed)) {
                            builder.suggest(online.getName());
                        }
                    }
                    return builder.buildFuture();
                });
    }

    private static RequiredArgumentBuilder<CommandSourceStack, String> message() {
        return Commands.argument("message", StringArgumentType.greedyString());
    }

    private static <T extends ArgumentBuilder<CommandSourceStack, T>> T forEach(
            T parent, List<String> values, Function<String, ArgumentBuilder<CommandSourceStack, ?>> child) {
        for (String value : values) {
            parent.then(child.apply(value));
        }
        return parent;
    }

    private static String target(CommandContext<CommandSourceStack> ctx) {
        return StringArgumentType.getString(ctx, "targetName");
    }

    private static String msg(CommandContext<CommandSourceStack> ctx) {
        return StringArgumentType.getString(ctx, "message");
    }

    /** Wraps a player-only action; other senders get a short error, as with CommandAPI's executesPlayer. */
    private static Command<CommandSourceStack> player(PlayerAction action) {
        return ctx -> {
            if (!(ctx.getSource().getSender() instanceof Player p)) {
                ctx.getSource().getSender().sendMessage("This command can only be run by a player.");
                return 0;
            }
            action.run(p, ctx);
            return Command.SINGLE_SUCCESS;
        };
    }
}
