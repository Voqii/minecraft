# CustomJoinAfkMessages

A Minecraft (Paper/Spigot/Purpur) plugin for custom per-player join, leave and AFK messages.

This is a fork of [milan252525/CustomJoinMessages](https://github.com/milan252525/CustomJoinMessages)
by **milan_25** ([SpigotMC resource](https://www.spigotmc.org/resources/custom-player-join-leave-messages.74263/)).
All original functionality and credit belongs to the original author.

## Requirements

- Paper/Spigot/Purpur server (`api-version: 1.21`)
- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) (required — the plugin disables itself without it)
- [EssentialsX](https://essentialsx.net/) (optional — only needed for AFK/return messages)
- Java 21 to build

## Commands

`/cm` shows the help menu. Messages are stored per player **UUID** in `config.yml` under
`saved_messages.join.<uuid>` / `saved_messages.leave.<uuid>`.

### Player (`custommessages.set`)
- `/cm set join|leave|afk|return <message>` — set your own message
- `/cm show` — view your messages
- `/cm reset` — reset your messages to default

### Admin (`custommessages.admin`)
- `/cm adminset <player> join|leave|afk|return <message>` — set another player's message
- `/cm adminshow <player>` — view another player's messages
- `/cm adminreset <player>` — reset another player's messages to default
- `/cm adminremove <player> join|leave|afk|return|all` — **remove** a player's stored custom message(s)
- `/cm adminreload` — reload the configuration

## AFK / return messages (EssentialsX)

`afk` and `return` messages are broadcast when a player's EssentialsX AFK status
changes (via `/afk` or Essentials' idle timer). They work exactly like join/leave —
per-UUID storage under `saved_messages.afk` / `saved_messages.return`, with
`custom_afk_message` / `custom_return_message` (+ prefixes) as defaults in `config.yml`.

This is driven by EssentialsX's `AfkStatusChangeEvent`. EssentialsX is a **soft**
dependency: if it isn't installed the plugin still loads and join/leave keep working —
only the AFK listener is skipped (logged at startup). An empty AFK/return message
broadcasts nothing.

## Changes in this fork

- **Offline player support for admin commands.** `adminset`, `adminshow`, `adminreset`
  and the new `adminremove` now take a player **name** and resolve it to the stored UUID,
  instead of requiring an online entity-selector target. Because messages are keyed by
  UUID, admins can manage (and remove) a player's custom messages while that player is
  offline.
- **New `/cm adminremove <player> join|leave|all`** subcommand that fully removes stored
  message entries (sets the config keys to `null`) rather than blanking them to an empty
  string, so they no longer linger in `config.yml`.

- **AFK / return messages via EssentialsX.** New per-player `afk` and `return` message
  types, broadcast on EssentialsX's `AfkStatusChangeEvent`. Added a soft dependency on
  Essentials and a conditional listener (registered only when Essentials is present),
  mirroring the existing PlaceholderAPI handling. See the section above.

> Name resolution checks online players first, then the server's cached offline players
> (anyone who has joined before) — no blocking Mojang lookup. If the name has never been
> seen on the server, the command reports that instead of acting on a fabricated account.
> (CommandAPI 11.1.0 does not expose an offline-player argument annotation, so a string
> argument is used.)

## Building

```sh
mvn clean package
```

The shaded plugin JAR is produced in `target/`.
