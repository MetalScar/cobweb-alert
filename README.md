# Cobweb Alert Mod
**Minecraft 1.21.11 · Fabric · Client-side only**

Cobwebs glow **red** the moment any player or mob touches them, and return to
normal the instant they leave. Every client with the mod installed sees the
same thing — including cobwebs that your *opponent* is trapped in, making it
perfect for PvP.

---

## How It Works (no-code summary)

| Piece | What it does |
|---|---|
| `CobwebTracker` | Once per game tick, scans all nearby entities. If any living entity's bounding box overlaps a cobweb block, that position is "active". |
| `CobwebAlertClient` | Registers a *colour provider* for `minecraft:cobweb`. Fabric calls it whenever a cobweb is about to be drawn; we return red for active positions and white (= no tint) for everything else. |
| `assets/minecraft/models/block/cobweb.json` | Vanilla's cobweb model has no tint slot. Ours is identical in shape but adds `"tintindex": 0` to every face, which is what tells the renderer to apply our colour. |

No server mod needed. No mixins. No custom network packets.
The mod reads only data that every Minecraft client already receives.

---

## Prerequisites

Make sure you have installed:
- **Java 21 JDK** (not just a JRE)
- **IntelliJ IDEA** (Community or Ultimate)
- **Fabric Loader** installed in your Minecraft launcher for version **1.21.11**

---

## Opening the Project in IntelliJ

1. Open IntelliJ IDEA → **File → Open** → select the `cobweb-alert` folder.
2. When asked, choose **"Open as Gradle Project"**.
3. Wait for Gradle to sync (it will download Minecraft, Fabric API, and mappings
   on first run — this takes a few minutes).
4. Once synced, click the **Gradle panel** (elephant icon, top-right) →
   `cobweb-alert → Tasks → fabric → genSources`
   This generates human-readable Minecraft source for IntelliJ autocomplete.
5. Reload Gradle one more time after that task finishes.

---

## Building the Mod (.jar file)

In IntelliJ's terminal (or any terminal inside the project folder):

```
./gradlew build          # Mac / Linux
gradlew.bat build        # Windows
```

Your finished mod file appears at:
```
build/libs/cobweb-alert-1.0.0.jar
```

(Ignore the `-dev` and `-sources` jars; only the main one is needed.)

---

## Installing

1. Copy `cobweb-alert-1.0.0.jar` into your Minecraft **mods** folder.
   - Windows: `%AppData%\.minecraft\mods\`
   - Mac: `~/Library/Application Support/minecraft/mods/`
   - Linux: `~/.minecraft/mods/`
2. Make sure [**Fabric API**](https://modrinth.com/mod/fabric-api) is also in
   your mods folder for the same version.
3. Launch Minecraft with the **Fabric 1.21.11** profile.

The mod only needs to be installed on **your** client. The server does not need
it. Other players on the same server also see red cobwebs if they install it.

---

## One Likely IDE Fix

The call `client.worldRenderer.scheduleBlockRenders(...)` in `CobwebTracker.java`
is the only line that Mojang occasionally renames between minor versions.
If IntelliJ shows it underlined in red:

1. Delete `scheduleBlockRenders` (keep `client.worldRenderer.`).
2. Press **Ctrl + Space** (or **⌃ Space** on Mac).
3. Look for a method that takes three `int` arguments and whose name contains
   "schedule" or "renderBlock" or "markDirty". It will be right there.
4. Select it and save.

Everything else in the mod uses fully stable, public Fabric / Minecraft APIs
that haven't changed across the entire 1.21.x cycle.

---

## Customising the Colour

Open `CobwebAlertClient.java`. At the top of the class:

```java
private static final int ALERT_COLOR  = 0xFF3B3B;  // ← change this hex colour
private static final int NORMAL_COLOR = 0xFFFFFF;  // white = no tint (leave as-is)
```

RGB hex, same format as HTML colours. Rebuild after changing.

---

## File Map

```
cobweb-alert/
├── build.gradle
├── gradle.properties           ← all version numbers live here
├── settings.gradle
├── gradle/wrapper/
│   └── gradle-wrapper.properties
└── src/
    ├── main/
    │   └── resources/
    │       └── fabric.mod.json ← mod metadata
    └── client/
        ├── java/com/cobwebalert/
        │   ├── CobwebAlertClient.java   ← entry point, registers colour provider
        │   └── CobwebTracker.java       ← per-tick entity scan
        └── resources/assets/minecraft/models/block/
            └── cobweb.json     ← vanilla model + tintindex added
```
