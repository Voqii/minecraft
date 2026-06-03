# CustomJoinMessages (fork)

A Minecraft (Paper/Spigot) plugin for custom per-player join and leave messages.

This is a fork of [milan252525/CustomJoinMessages](https://github.com/milan252525/CustomJoinMessages)
by **milan_25** ([SpigotMC resource](https://www.spigotmc.org/resources/custom-player-join-leave-messages.74263/)).
All original functionality and credit belongs to the original author.

## Requirements

- Paper/Spigot server (`api-version: 1.21`)
- [PlaceholderAPI](https://www.spigotmc.org/resources/placeholderapi.6245/) (required — the plugin disables itself without it)
- Java 21 to build

## Commands

`/cm` shows the help menu. Messages are stored per player **UUID** in `config.yml` under
`saved_messages.join.<uuid>` / `saved_messages.leave.<uuid>`.

### Player (`custommessages.set`)
- `/cm set join|leave <message>` — set your own join/leave message
- `/cm show` — view your messages
- `/cm reset` — reset your messages to default

### Admin (`custommessages.admin`)
- `/cm adminset <player> join|leave <message>` — set another player's message
- `/cm adminshow <player>` — view another player's messages
- `/cm adminreset <player>` — reset another player's messages to default
- `/cm adminremove <player> join|leave|all` — **remove** a player's stored custom message(s)
- `/cm adminreload` — reload the configuration

## Changes in this fork

- **Offline player support for admin commands.** `adminset`, `adminshow`, `adminreset`
  and the new `adminremove` now take a player **name** and resolve it to the stored UUID,
  instead of requiring an online entity-selector target. Because messages are keyed by
  UUID, admins can manage (and remove) a player's custom messages while that player is
  offline.
- **New `/cm adminremove <player> join|leave|all`** subcommand that fully removes stored
  message entries (sets the config keys to `null`) rather than blanking them to an empty
  string, so they no longer linger in `config.yml`.

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
