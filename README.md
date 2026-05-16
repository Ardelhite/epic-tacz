# Epic Fight × TacZ Compat

A client-only compatibility patch for **Minecraft 1.21.1 / NeoForge**
that restores TacZ's gun animations — both first-person (reload,
inspect, draw) and third-person (gun-holding pose, body tilt, leg
stance) — when [Epic Fight] is installed alongside [TacZ].

> **TacZ**, **Epic Fight**, and **PlayerAnimator** are all required.
> The mod will refuse to load without them — it has no purpose on its own.

[Epic Fight]: https://www.curseforge.com/minecraft/mc-mods/epic-fight-mod
[TacZ]: https://www.curseforge.com/minecraft/mc-mods/tacz-1-21-1
[PlayerAnimator]: https://www.curseforge.com/minecraft/mc-mods/playeranimator

---

## Summary (CurseForge)

> Restores TacZ's first-person and third-person gun animations when
> Epic Fight is installed. Client-only.

## The problem

When TacZ and Epic Fight are loaded together, two regressions appear:

- **First person**: Epic Fight's combat-mode renderer takes over the
  player's arms before TacZ can run its `ItemInHandRenderer` chain.
  Reload, inspect, and draw animations silently disappear while you
  hold a gun.
- **Third person**: even after forcing Epic Fight into vanilla mode,
  Epic Fight's `HumanoidModel.setupAnim` TAIL inject still runs after
  TacZ's, wiping out the gun-holding pose. The player reverts to the
  default vanilla walking animation.

## What this mod does

Three orthogonal fixes:

1. **First-person tick patch.** Every client tick, while the local
   player is holding a TacZ gun (`IGun`), forces Epic Fight back to
   vanilla mode via `LocalPlayerPatch.toVanillaMode(false)`. Combat
   mode resumes the moment you swap to a non-gun item.
2. **Third-person mixin.** A single `HumanoidModel.setupAnim` TAIL
   inject at `priority=1500` (so it runs after Epic Fight's) re-invokes
   TacZ's `InnerThirdPersonManager.setRotationAnglesHead`, restoring
   the gun-holding pose.
3. **PlayerAnimator dependency.** TacZ's third-person logic only
   plays the full-body gun animation (body tilt, leg stance, kneeling
   crouch) when PlayerAnimator is on the classpath; otherwise it
   silently falls back to an arm-only pose. This mod makes
   PlayerAnimator a required dependency so the visual matches
   TacZ-standalone.

No asset replacement, no server-side code, no config files.

## Requirements

| | Version |
|---|---|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.228 or newer |
| TacZ | 1.1.0+ (the [Unofficial NeoForge port][TacZ]) |
| Epic Fight | 21.0.0+ |
| PlayerAnimator | 2.0.0+ |

## Installation

1. Install NeoForge for Minecraft 1.21.1.
2. Drop **TacZ**, **Epic Fight**, **PlayerAnimator**, and **this jar**
   into your `mods/` folder.
3. Launch. TacZ animations should play normally in both first and
   third person while a gun is equipped, and Epic Fight stances work
   as usual for everything else.

This mod only runs on the client. It's safe to omit on dedicated servers.

## Known limitations

- Only the **local player** is patched in first person. Other players
  holding guns will still render with Epic Fight's combat armature on
  your screen — fixing that would require touching server state and is
  out of scope. (The third-person mixin applies to every player.)
- The patch assumes the public API signatures of TacZ (`IGun`,
  `InnerThirdPersonManager.setRotationAnglesHead`), Epic Fight
  (`LocalPlayerPatch.toVanillaMode`, `isEpicFightMode`,
  `EpicFightCapabilities.getCachedLocalPlayerPatch`) and the mod ID of
  PlayerAnimator (`playeranimator`). If any of these change in a
  future release, this patch will need a rebuild.

## Source / Issues

- Source: <https://github.com/Ardelhite/epic-tacz>
- Bug reports: <https://github.com/Ardelhite/epic-tacz/issues>

## License

[MIT](./LICENSE) — do whatever you want, no warranty.

## Credits

- The TacZ team and the unofficial 1.21.1 NeoForge porter for keeping
  the gun mod alive on modern versions.
- Yesman / the Epic Fight team for the combat mod.
- KosmX for [PlayerAnimator], without which TacZ's third-person gun
  animation falls back to an arm-only pose.

This project is **not affiliated** with TacZ, Epic Fight, or PlayerAnimator.
