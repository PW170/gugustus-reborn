# Gugustus Reborn

A client-side utility mod for Minecraft 1.8.9 (Forge) providing PvP and movement enhancements with a modern glassmorphic ClickGUI.

## Features

- **9 Modules**: Autoclicker, Killaura, Velocity, InvManager, ChestStealer, InvMove, Scaffold, Flight, NoSlow
- **Glassmorphic ClickGUI**: Gray-orange themed, semi-transparent panels, toggle switches, settings dropdowns
- **Commands**: `.bind`, `.toggle`, `.config save/load/folder`
- **Config System**: JSON-based profiles auto-saved to `.minecraft/Gugustus/configs/`
- **HUD**: ArrayList, Watermark (version + FPS), TargetHUD

## Issues

- **ClickGUI needs to be revamped** — the current ClickGUI is a placeholder and needs a complete redesign with proper scrolling, resizable panels, and better visual layout.

## Building

```bash
gradlew clean build
```

Output JAR: `build/libs/gugustus-reborn-0.1.0.jar`

Place the JAR in `.minecraft/mods/` and launch with Forge 1.8.9.

## Controls

| Key | Action |
|-----|--------|
| Right Shift | Open ClickGUI |
| `.bind <module> <key>` | Bind a module to a key |

## License

MIT
