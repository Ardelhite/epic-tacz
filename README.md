# Epic Fight × TacZ First-Person Compat

A tiny client-only compatibility patch for **Minecraft 1.21.1 / NeoForge**
that restores TacZ's first-person gun animations (reload, inspect, draw)
when [Epic Fight] is installed alongside [TacZ].

> Both **TacZ** and **Epic Fight** are required. The mod will refuse to
> load without them — that's intentional, it has no purpose on its own.

[Epic Fight]: https://www.curseforge.com/minecraft/mc-mods/epic-fight-mod
[TacZ]: https://www.curseforge.com/minecraft/mc-mods/tacz-1-21-1

---

## Summary (CurseForge)

> Restores TacZ's first-person gun animations when Epic Fight is installed.
> Client-only, no Mixins, no config — just drop it in.

## The problem

When TacZ and Epic Fight are loaded together, Epic Fight's combat-mode
renderer takes over the player's arms before TacZ can run its first-person
`ItemInHandRenderer` chain. The result: reload, inspect and draw
animations silently disappear while you're holding a gun.

## What this mod does

Every client tick, while the local player is holding a TacZ gun in
either hand:

1. Detect the gun via TacZ's `IGun` capability.
2. Pull the local player's `LocalPlayerPatch` from Epic Fight.
3. If Epic Fight is currently in battle mode, force it back to vanilla
   mode via `patch.toVanillaMode(true)`.

That's the whole mod. No Mixins, no asset replacement, no server-side
code, no config files. Combat mode resumes normally as soon as you
swap to a non-gun item.

## Requirements

| | Version |
|---|---|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.228 or newer |
| TacZ | 1.1.0+ (the [Unofficial NeoForge port][TacZ]) |
| Epic Fight | 21.0.0+ |

## Installation

1. Install NeoForge for Minecraft 1.21.1.
2. Drop **TacZ**, **Epic Fight**, and **this jar** into your `mods/` folder.
3. Launch. You should see TacZ animations play normally while a gun is
   equipped, and Epic Fight stances work as usual for everything else.

This mod only runs on the client. It's safe to omit on dedicated servers.

## Known limitations

- Only the **local player** is patched. Other players holding guns will
  still render with Epic Fight's combat armature on your screen —
  fixing that would require touching server state and is out of scope.
- The patch assumes the public API signatures of TacZ (`IGun`) and Epic
  Fight (`LocalPlayerPatch.toVanillaMode`, `isEpicFightMode`,
  `EpicFightCapabilities.getCachedLocalPlayerPatch`). If either mod
  changes those signatures in a future release, this patch will need a
  rebuild.

## Source / Issues

- Source: <https://github.com/Ardelhite/epic-tacz>
- Bug reports: <https://github.com/Ardelhite/epic-tacz/issues>

## License

[MIT](./LICENSE) — do whatever you want, no warranty.

## Credits

- The TacZ team and the unofficial 1.21.1 NeoForge porter for keeping
  the gun mod alive on modern versions.
- Yesman / the Epic Fight team for the combat mod.

This project is **not affiliated** with either TacZ or Epic Fight.
