# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1201-0.5.0] - 2026-05-16

### Added
- Backport to **Minecraft 1.20.1 / Forge 47.4.10**. Behaviour matches
  the 1.21.1/NeoForge 0.4.0 release (first-person tick patch +
  third-person `HumanoidModel` mixin + PlayerAnimator as a required
  dependency).

### Notes
- This is a separate distribution from the 1.21.1/NeoForge build.
  Version string is prefixed with `1201-` so CurseForge and clients
  can tell the two apart.
- Targets TacZ 1.1.8 (1.20.1 Forge port — Modrinth slug
  `timeless-and-classics-zero`), EpicFight `20.14.17-mc1.20.1-forge`,
  and PlayerAnimator `1.0.2-rc1+1.20` (Forge).

## [0.4.0] - 2026-05-16

### Added
- PlayerAnimator (by KosmX) is now a required dependency. TacZ's
  third-person gun pose internally branches: with PlayerAnimator it
  plays a full-body animation (body tilt, leg stance, kneeling crouch),
  otherwise it falls back to an arm-only pose. 0.3.0 only restored the
  arm-only fallback under Epic Fight; this version restores the full
  body animation by ensuring PlayerAnimator is always loaded.

### Notes
- This adds a third mandatory mod to the runtime requirements
  (TacZ + Epic Fight + PlayerAnimator). Players who do not want
  PlayerAnimator should stay on 0.3.0, which produces the arm-only
  pose without that dependency.

## [0.3.0] - 2026-05-16

### Fixed
- Third-person view now keeps TacZ's gun-holding pose while Epic Fight
  is installed. Previously, even after the patch forced Epic Fight into
  vanilla mode, TacZ's third-person `HumanoidModel` pose was being lost
  somewhere between `setupAnim` and the final render, leaving the
  player with the default vanilla walking animation.

### Added
- New Mixin `HumanoidModelMixin` (priority 1500) that injects after
  TacZ's own `HumanoidModel.setupAnim` TAIL hook and re-invokes
  `InnerThirdPersonManager.setRotationAnglesHead` for any player
  holding a TacZ gun. This is gated to players only — every other
  `LivingEntity` short-circuits immediately.

### Changed
- Project no longer claims to be Mixin-free. A new `mixins.json`
  (`epictaczcompat.mixins.json`) is declared from `neoforge.mods.toml`
  with a single client-side mixin.

## [0.2.0] - 2026-05-16

### Fixed
- Third-person view now keeps TacZ's gun-holding pose instead of falling
  back to the vanilla walking animation. Previously, `toVanillaMode(true)`
  was unintentionally forcing the camera to `FIRST_PERSON` every tick via
  Epic Fight's `autoPerspectiveSwithing` config, snapping players out of
  third person before TacZ's `HumanoidModel` mixin could render the pose.

### Changed
- The tick handler now snapshots the camera type before calling
  `LocalPlayerPatch.toVanillaMode` and restores it afterwards, so manual
  F5 perspective changes are preserved.
- Switched the `toVanillaMode` dispatch flag from `true` to `false`,
  avoiding redundant `CPChangePlayerMode` packets to the server every
  tick while a gun is held.

## [0.1.1] - 2026-05-16

### Changed
- Declare the mod as `side="CLIENT"` in `neoforge.mods.toml`. The patch
  has no server-side code, so installing it on a dedicated server is
  now unnecessary and a server-side install is no longer expected by
  the manifest.
- TacZ and Epic Fight dependencies are now also `side="CLIENT"`. Clients
  with this mod can connect to servers that run TacZ and Epic Fight
  without also requiring this patch to be installed server-side.

### Notes
- No behavioural change on the client. If you already had this mod
  installed on both sides, you can safely remove it from the server
  side after updating; or leave it in place — it remains harmless.

## [0.1.0] - 2026-05-16

Initial public release.

### Added
- Client-only compatibility patch that forces Epic Fight into vanilla mode
  while the local player is holding a TacZ gun (main hand or off hand).
- Restores TacZ first-person animations (reload, inspect, draw) when
  Epic Fight is installed alongside it.
- Targets Minecraft 1.21.1 / NeoForge 21.1.228.
- Requires TacZ 1.1.0 or newer and Epic Fight 21.0.0 or newer.

### Notes
- No Mixins are used; the patch only relies on Epic Fight's
  `LocalPlayerPatch` and TacZ's `IGun` public APIs.
- Server-side is intentionally untouched to avoid desyncing Epic Fight's
  client/server state.
