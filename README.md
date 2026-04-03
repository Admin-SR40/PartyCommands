# Party Commands Mod

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.10-blue.svg)](https://minecraft.net/)
[![Fabric](https://img.shields.io/badge/Fabric-0.16.10+-blue.svg)](https://fabricmc.net/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.2.0-orange.svg)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

A **Fabric** client-side mod for **Hypixel SkyBlock** that streamlines party commands. Type `!command` directly in chat — no `/pc` prefix needed.

> Based on concepts from **Odin Mod** and **Meteor Client**.

---

## ✨ Features

- **Direct Commands** — Type `!command` without switching to party chat
- **Smart Completion** — Tab completion for all commands, filters other mods' `!` commands
- **Command History** — Press `↑` to recall previous `!` commands
- **Auto Queue** — Dungeon countdown with automatic queue execution
- **Party Tracking** — Automatic member/leader status tracking from chat
- **YACL Config** — In-game GUI for toggling commands (`/partycmds gui`)

---

## 📦 Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) and [Fabric API](https://modrinth.com/mod/fabric-api) for Minecraft 1.21.10
2. Install [YACL](https://modrinth.com/mod/yacl) (Yet Another Config Lib)
3. Download `PartyCommandsMod-*.jar` from [Releases](../../releases)
4. Place in `.minecraft/mods`

---

## ⌨️ Commands

### Info Commands
| Command | Alias | Description |
|---------|-------|-------------|
| `!ping` | — | Show latency (color-coded) |
| `!tps` | — | Show server TPS |
| `!fps` | — | Show current FPS |
| `!time` | — | Show local date/time |
| `!location` | `!loc` | Show current location |
| `!coords` | `!co` | Show coordinates |
| `!holding` | `!hold` | Show held item |
| `!status` | — | Show party members & leader |
| `!cd <time>` | `!countdown` | Start countdown (`60`, `5m`, `1h`) |
| `!clear` | — | Clear active countdown |

### Party Management *(Leader only)*
| Command | Alias | Description |
|---------|-------|-------------|
| `!warp` | `!w` | Warp party members |
| `!allinvite` | `!allinv` | Enable all invite |
| `!transfer <player>` | `!pt` | Transfer leadership |
| `!promote <player>` | — | Promote member |
| `!demote <player>` | — | Demote member |
| `!kick <player> [reason]` | `!k` | Kick member (reason optional) |
| `!disband` | — | Disband party |

### Party Commands
| Command | Alias | Description |
|---------|-------|-------------|
| `!leave` | — | Leave party |
| `!invite <player>` | `!inv` | Invite player |

### Dungeon & Kuudra
| Command | Description |
|---------|-------------|
| `!f1` ~ `!f7` | Queue Catacombs floors |
| `!m1` ~ `!m7` | Queue Master Mode floors |
| `!t1` ~ `!t5` | Queue Kuudra tiers |

All support countdown: `!f7 60` → counts down → auto-queues

### Fun Commands *(via `!fun`)*
| Command | Description |
|---------|-------------|
| `!fun cf` | Coin flip (heads/tails) |
| `!fun 8ball` | Magic 8-ball |
| `!fun dice` | Roll 1-6 |
| `!fun boop <player>` | Boop a player |
| `!fun random [min] [max]` | Random number (default 1-100) |

### Utility Commands
| Command | Description |
|---------|-------------|
| `!note [message]` | Save/send note to party |
| `!forward` | Toggle party chat forwarding |
| `!gui` | Open config GUI |
| `!reload` | Reload config |
| `!ver` | Show version |
| `!help` | Show help |

### Mod Settings
| Command | Description |
|---------|-------------|
| `/partycmds` | Show usage |
| `/partycmds gui` | Open config GUI |
| `/partycmds reload` | Reload config |
| `/partycmds reset` | Reset party state |

---

## ⚙️ Configuration

Use `/partycmds gui` for in-game configuration:
- **General** — Mod enabled, command prefix
- **Info Commands** — Toggle ping, tps, fps, etc.
- **Party Management** — Toggle warp, kick, promote, etc.
- **Fun Commands** — Toggle cf, 8ball, dice, boop
- **Response** — Where to show command outputs

Config file: `.minecraft/config/partycommands.json`

```json
{
  "enabled": true,
  "prefix": "!",
  "ping": true,
  "tps": true,
  "fps": true,
  "warp": true,
  "kick": true,
  "coinflip": true,
  "queueInstance": true,
  "respondInPartyChat": true,
  "showResponseLocally": true,
  "countdownSound": true
}
```

---

## 🛠️ Development

### Build
```bash
./gradlew build
```
Output: `build/libs/PartyCommandsMod-0.1.jar`

### Requirements
- JDK 21+
- Gradle (wrapper included)

### Project Structure
```
src/main/
├── java/com/partycommands/mixin/
│   ├── ChatMixin.java
│   ├── ChatScreenMixin.java
│   ├── CommandSuggestionsMixin.java
│   └── ClientPacketListenerMixin.java
├── kotlin/com/partycommands/
│   ├── PartyCommandsMod.kt
│   ├── commands/
│   │   ├── Command.kt
│   │   ├── Commands.kt
│   │   └── PartyCommandHandler.kt
│   ├── config/
│   │   └── Config.kt
│   ├── gui/
│   │   └── ConfigGui.kt
│   └── utils/
│       ├── ChatListener.kt
│       ├── ChatUtils.kt
│       ├── PartyListHandler.kt
│       ├── PartyUtils.kt
│       ├── CountdownManager.kt
│       └── ServerUtils.kt
└── resources/
```

---

## 🙏 Credits

- **Odin Mod** — Ping logic and feature inspiration
- **Meteor Client** — Brigadier patterns
- **YACL** — Config GUI by isXander
- **Fabric Team** — Fabric Loader and API

---

## 📄 License

MIT License — see [LICENSE](LICENSE)
