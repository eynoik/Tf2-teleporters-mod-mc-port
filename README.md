# TF2 Teleporters — Minecraft 1.21.1

A NeoForge 1.21.1 port of the classic **TF2 Teleporter** mod from Minecraft 1.7.10.

The goal of this port is to preserve the original look, sound and simple frequency-based gameplay while replacing the old Forge internals with a modern and safer implementation.

## Features

- RED and BLU TF2 teleporters
- Original teleporter model geometry
- Original base and propeller textures
- Original GUI texture and `<<`, `<`, `>`, `>>` controls
- Original `spin.ogg` and `teleport.ogg` sounds
- Legacy-style spinning propeller animation
- Frequencies **1–99**
- Maximum **two teleporters per frequency per dimension**
- Teleports players and mobs
- Saved frequency assignments survive world/server restarts
- RED and BLU teleporters can pair with either color
- Original crafting progression restored
- Original Teleporter Base and Teleporter Propeller component items restored

## Requirements

- Minecraft **1.21.1**
- NeoForge **21.1.x**
- Java **21**

## Installation

1. Install NeoForge for Minecraft 1.21.1.
2. Put the TF2 Teleporters `.jar` into the `mods` folder.
3. Launch the game.

The mod does not require any additional dependencies.

## How to use

Place two teleporters in the same dimension and assign both of them the same frequency through the teleporter GUI.

Available frequencies range from **1 to 99**. A frequency can contain at most two teleporters. If two teleporters already use a frequency, a third teleporter cannot commit that frequency.

Once a valid pair exists, the propeller starts spinning. Stand on an active teleporter briefly to be sent to the paired teleporter.

## Crafting

### Teleporter Base

Two legacy recipe layouts are supported:

```text
I I
III
I I
```

or

```text
III
 I 
III
```

`I` = Iron Ingot

### Teleporter Propeller

```text
TRT
RRR
III
```

- `T` = Redstone Torch
- `R` = Redstone
- `I` = Iron Ingot

### RED Teleporter

```text
D
P
B
```

- `D` = Red Dye
- `P` = Teleporter Propeller
- `B` = Teleporter Base

### BLU Teleporter

```text
D
P
B
```

- `D` = Blue Dye
- `P` = Teleporter Propeller
- `B` = Teleporter Base

A RED teleporter can also be recolored to BLU with Blue Dye, and a BLU teleporter can be recolored to RED with Red Dye.

## Port notes

The visible behavior is intentionally close to the 1.7.10 mod, but the backend has been rewritten for modern Minecraft.

The port uses per-dimension `SavedData` instead of the old location database and does not permanently force-load teleporter chunks. A destination chunk is loaded only when teleportation is actually needed.

Legacy update-checking code, obsolete networking, optional TF2 team-addon integration and known legacy debug/bug behavior were intentionally not carried over.

For the 1.0.0 release, teleporter sounds are slightly louder than the original port alphas and teleporter blocks use increased durability with very high blast resistance.

## Building from source

```bash
gradle build
```

The built JAR is written to `build/libs/`.

GitHub Actions builds pushes to `main` and verifies the restored legacy assets during the build.

## Version

Current release: **1.0.0** for Minecraft **1.21.1 NeoForge**.
