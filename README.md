# Gugustus Reborn

A client-side utility mod for Minecraft 1.8.9 (Forge) providing PvP and movement enhancements with a modern glassmorphic ClickGUI.

## Features

- **9 Modules**: Autoclicker, Killaura, Velocity, InvManager, ChestStealer, InvMove, Scaffold, Flight, NoSlow
- **Glassmorphic ClickGUI**: Gray-orange themed, semi-transparent panels, toggle switches, settings dropdowns
- **Commands**: `.bind`, `.toggle`, `.config save/load/folder`
- **Config System**: JSON-based profiles auto-saved to `.minecraft/Gugustus/configs/`
- **HUD**: ArrayList, Watermark (version + FPS), TargetHUD

## Known Issues

### ClickGUI Module Cards Not Rendering in Movement/Player Panels

**Status**: Unresolved

The Combat panel correctly renders Autoclicker, Killaura, and Velocity module cards. However, the Movement and Player panels remain empty — Flight, NoSlow, InvManager, ChestStealer, InvMove, and Scaffold do not appear.

**Debug output confirms**:
- `ModuleManager` registers all 9 modules correctly with proper category mapping
- `ClickGUI` constructor sees all 3 panels (Combat, Movement, Player) with correct module counts
- `Panel` constructor receives the correct module list and creates `ModuleCard` objects
- Console shows `"Card added for Flight"`, `"Card added for NoSlow"`, etc.

Despite all cards being created, they are not visible when the GUI renders. The Panel's `draw()` method iterates over the `cards` list and calls `card.draw()` — the draw code is identical for all panels. Combat works, Movement/Player do not.

**Possible causes under investigation:**
- Scissor test clipping region: despite correct `GL11.glScissor()` parameters, the region may compute differently for panels at higher X offsets (x=170, x=320) vs Combat (x=20)
- Z-ordering / draw order: the default background may overdraw certain panel regions
- OpenGL state leak from other GUI elements

**Workaround**: None currently. The Combat panel modules are fully functional.

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
