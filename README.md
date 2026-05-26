# Epic Fight × TacZ Compat

A client-only compatibility patch that restores TacZ's gun animations
— both first-person (reload, inspect, draw) and third-person
(gun-holding pose, body tilt, leg stance) — when [Epic Fight] is
installed alongside [TacZ].

Two parallel builds are maintained from this repository:

- **1.21.1 / NeoForge** — `main` branch.
- **1.20.1 / Forge** — `1201` branch (backport).

Both builds implement the same fix (public-API combat-mode suppression
+ a single TAIL inject on `HumanoidModel.setupAnim`); only the loader
  and dependency versions differ.

> **TacZ**, **Epic Fight**, and **PlayerAnimator** are all required.
> The mod will refuse to load without them — it has no purpose on its own.

[Epic Fight]: https://www.curseforge.com/minecraft/mc-mods/epic-fight-mod
[TacZ]: https://www.curseforge.com/minecraft/mc-mods/tacz
[TacZ-1211]: https://www.curseforge.com/minecraft/mc-mods/tacz-1-21-1
[PlayerAnimator]: https://www.curseforge.com/minecraft/mc-mods/playeranimator

---

## Summary (CurseForge)

> Restores TacZ's first-person and third-person gun animations when
> Epic Fight is installed. Client-only. Separate builds for
> 1.21.1/NeoForge and 1.20.1/Forge.

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
   vanilla mode via `LocalPlayerPatch.toVanillaMode(...)`. Combat
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

### 1.21.1 / NeoForge build (`main` branch)

| | Version |
|---|---|
| Minecraft | 1.21.1 |
| NeoForge | 21.1.228 or newer |
| TacZ | 1.1.0+ (the [Unofficial NeoForge port][TacZ-1211]) |
| Epic Fight | 21.0.0+ |
| PlayerAnimator | 2.0.0+ |

### 1.20.1 / Forge build (`1201` branch)

| | Version |
|---|---|
| Minecraft | 1.20.1 |
| Forge | 47.4.10 or newer |
| TacZ | 1.1.8 (1.20.1 Forge port — [CurseForge][TacZ] / [Modrinth](https://modrinth.com/mod/timeless-and-classics-zero)) |
| Epic Fight | `20.14.17-mc1.20.1-forge` (CurseForge file `8049910`) |
| PlayerAnimator | `1.0.2-rc1+1.20` (CurseForge file `4587214`) |

Newer point releases of TacZ / Epic Fight / PlayerAnimator on the same
Minecraft line are accepted at load time, but only the versions listed
above are verified to work with each build.

## Installation

1. Install the matching loader for your Minecraft version
   (NeoForge 21.1.228+ for 1.21.1, or Forge 47.4.10+ for 1.20.1).
2. Drop **TacZ**, **Epic Fight**, **PlayerAnimator**, and **this jar**
   (the one matching your Minecraft version) into your `mods/` folder.
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
  (1.21.1: `main` / 1.20.1: `1201`)
- Bug reports: <https://github.com/Ardelhite/epic-tacz/issues>

## License

[MIT](./LICENSE) — do whatever you want, no warranty.

## Credits

- The TacZ team for the original gun mod, and the 1.20.1 Forge
  maintainers + the unofficial 1.21.1 NeoForge porter for keeping it
  alive across versions.
- Yesman / the Epic Fight team for the combat mod.
- KosmX for [PlayerAnimator], without which TacZ's third-person gun
  animation falls back to an arm-only pose.

This project is **not affiliated** with TacZ, Epic Fight, or PlayerAnimator.
