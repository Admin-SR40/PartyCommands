# Party Commands Mod

[![Minecraft](https://img.shields.io/badge/Minecraft-1.21.10-blue.svg)](https://minecraft.net/)
[![Fabric](https://img.shields.io/badge/Fabric-0.16.10+-blue.svg)](https://fabricmc.net/)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0.21-orange.svg)](https://kotlinlang.org/)
[![License](https://img.shields.io/badge/License-MIT-green.svg)](LICENSE)

A standalone **Fabric** client-side mod for Hypixel SkyBlock that lets you type party commands directly in chat with a `!` prefix — no need to manually use `/pc !command`.

> Based on concepts from **Odin Mod** and **Meteor Client**.

---

## ✨ Features

- 🚀 Type `!command` directly in chat — fast and intuitive
- 🎮 No `/pc` prefix required
- ⚙️ Toggle individual commands on/off via config
- 💬 Choose to respond in party chat, locally, or both
- 🧠 Automatic party state tracking from Hypixel chat messages
- 🔧 Built-in tab completion for all `!` commands

---

## 📦 Installation

1. Install [Fabric Loader](https://fabricmc.net/use/) and [Fabric API](https://modrinth.com/mod/fabric-api) for Minecraft 1.21.10
2. Download the latest `PartyCommandsMod-*.jar` from [Releases](../../releases)
3. Place the jar into your `.minecraft/mods` folder
4. Launch the game and enjoy!

---

## ⌨️ Commands

### Info Commands
| Command | Alias | Description |
|---------|-------|-------------|
| `!ping` | — | Show current latency (ms) |
| `!tps` | — | Show estimated server TPS |
| `!fps` | — | Show current FPS |
| `!time` | — | Show local date and time |
| `!location` | `!loc` | Show current location |
| `!coords` | `!co` | Show current coordinates |
| `!holding` | `!hold` | Show held item name |

### Party Management *(Leader only)*
| Command | Alias | Description |
|---------|-------|-------------|
| `!warp` | `!w` | Warp all party members |
| `!allinvite` | `!allinv` | Enable all invite |
| `!transfer <player>` | `!pt` | Transfer party leadership |
| `!promote <player>` | — | Promote a member |
| `!demote <player>` | — | Demote a member |
| `!kick <player> [reason]` | `!k` | Kick a member from party |

### Dungeon & Kuudra Queue *(Leader only)*
| Command | Description |
|---------|-------------|
| `!f1` ~ `!f7` | Queue normal Catacombs floors |
| `!m1` ~ `!m7` | Queue Master Mode Catacombs floors |
| `!t1` ~ `!t5` | Queue Kuudra tiers |

### Fun Commands
| Command | Alias | Description |
|---------|-------|-------------|
| `!cf` | `!coinflip` | Flip a coin (heads / tails) |
| `!8ball` | — | Magic 8-ball answer |
| `!dice` | — | Roll a dice (1–6) |

### Utility Commands
| Command | Alias | Description |
|---------|-------|-------------|
| `!boop <player>` | — | Boop a player |
| `!invite <player>` | `!inv` | Invite a player to party |
| `!forward` | — | Toggle party chat forwarding |
| `!reload` | — | Reload config |
| `!ver` | `!version` | Show mod version info |
| `!help` | `!h` | Show help message |

### Mod Settings Commands
| Command | Description |
|---------|-------------|
| `/partycmds` | Show usage help |
| `/partycmds reload` | Reload configuration |
| `/partycmds reset` | Reset tracked party state |

---

## ⚙️ Configuration

Config file location: `.minecraft/config/partycommands.json`

```json
{
  "enabled": true,
  "prefix": "!",
  "ping": true,
  "tps": true,
  "fps": true,
  "time": true,
  "location": true,
  "warp": true,
  "allinvite": true,
  "kick": true,
  "promote": true,
  "demote": true,
  "coinflip": true,
  "eightball": true,
  "dice": true,
  "queueInstance": true,
  "boop": true,
  "invite": true,
  "respondInPartyChat": true,
  "showResponseLocally": true
}
```

### Config Options
- **`enabled`** — Master toggle for the mod
- **`prefix`** — Prefix for party commands (default: `!`)
- **Individual command toggles** — Enable/disable specific `!` commands
- **`respondInPartyChat`** — Send command responses to party chat
- **`showResponseLocally`** — Show command responses in your own chat HUD

---

## 🛠️ Development

### Requirements
- JDK 21 or newer
- Gradle (wrapper included)

### Build
```bash
./gradlew build
```

The compiled jar will be located at:
```
build/libs/PartyCommandsMod-0.1.jar
```

### Project Structure
```
src/main/
├── java/com/partycommands/mixin/    # Mixin injections
├── kotlin/com/partycommands/        # Main logic
│   ├── PartyCommandsMod.kt
│   ├── config/Config.kt
│   ├── commands/
│   └── utils/
└── resources/                       # fabric.mod.json, mixins, assets
```

---

## 🙏 Credits

- **Odin Mod** — Ping calculation logic and original feature inspiration
- **Meteor Client** — Brigadier command system patterns
- **Fabric Team** — Fabric Loader and API

---

## 📄 License

This project is licensed under the [MIT License](LICENSE).
