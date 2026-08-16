# TF2 Teleporters — Minecraft 1.21.1 port

NeoForge 1.21.1 port of the classic **TF2 Teleporter** mod.

## Port target

The port intentionally preserves the parts players actually see and hear:

- original RED / BLU teleporter appearance,
- original model geometry,
- original propeller spin animation style,
- original `spin.ogg` and `teleport.ogg`,
- original GUI texture and `<<`, `<`, `>`, `>>` interaction,
- frequency pairing gameplay.

The legacy Forge 1.7.10 implementation is not carried over internally. The backend is rewritten for NeoForge 1.21.1.

## Frequency rules

- Frequencies are **1–99**.
- A frequency can contain at most **two teleporters in the same dimension**.
- A third teleporter can select a full frequency in the GUI, but the GUI marks it as unavailable and closing the GUI will not commit the change.
- RED and BLU are visual variants; either color can pair with either color.

## Requirements

- Minecraft 1.21.1
- NeoForge 21.1.x
- Java 21

## Build

```bash
gradle build
```

GitHub Actions also builds every push to `main`.
