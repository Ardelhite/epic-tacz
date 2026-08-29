# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [1201-0.6.0] - Unreleased

### Added
- **Epic Fight dodge now works while holding a gun.** Battle-mode
  suppression used to make the dodge key completely dead with a gun in
  hand: Epic Fight's `CombatKeyMapping` drops all input to its combat
  keys outside battle mode, and its skill input handler is gated on
  `isEpicFightMode()`. The mod now reads the dodge key's physical state
  directly (GLFW) while suppressed and casts the dodge skill through
  Epic Fight's public `SkillContainer.sendCastRequest` — the skill
  pipeline itself (ground/stamina/cooldown checks, server execution)
  has no mode gate. While the dodge animation plays, suppression is
  lifted so the roll renders on Epic Fight's armature, and battle-mode
  suppression resumes automatically when it ends. The usual dodge
  requirements (a learned dodge skill, stamina) still apply. If the
  dodge key shares its physical key with sneak — a setup Epic Fight
  explicitly supports — the mod does not auto-cast, to keep sneaking
  with a gun from triggering rolls. (Requested on CurseForge.)
- **LesRaisins Tactical Equipements (lrtactical) support.** Battle-mode
  suppression now also triggers for items implementing TacZ's
  `IAnimationItem` — the interface lrtactical's melee weapons,
  throwables and consumables use to hook into TacZ's animation system.
  Holding one of those items previously left Epic Fight's battle mode
  active from the moment you joined a world (Epic Fight starts in
  battle mode on 1.20.1), which wiped the item's first-person view
  model entirely. No new dependency: the check uses only TacZ's public
  API, and other TacZ-based equipment addons that implement
  `IAnimationItem` are covered too. Verified in-game with lrtactical
  0.4.1: the karambit's first-person model now stays rendered while
  idle and during attacks, battle mode cannot be entered while holding
  it, and switching to a sword still restores battle mode as usual.
  (Requested on CurseForge.)

## [1201-0.5.2] - 2026-08-20

### Fixed
- **Epic Fight battle mode stayed off after putting a gun away.** Holding
  a TacZ gun forced the player into vanilla mode every tick, but nothing
  ever restored battle mode, so switching back to a sword left the skill
  UI and Epic Fight combat disabled until the mode key was pressed by
  hand. (Reported on CurseForge.)
  The suppression now goes through Epic Fight's own
  `BattleModeSustainableEvent`: cancelling it makes `PlayerPatch.tick`
  set `battleModeRestricted`, and Epic Fight restores battle mode itself
  as soon as the gun is no longer held. The camera type is still
  preserved, but only on the tick the mode actually flips, so it no
  longer competes with normal F5 perspective changes.
- **Worn armor turned invisible.** Epic Fight's `WearableItemLayer`
  calls `HumanoidModel.setupAnim` on armor models — a call site vanilla
  never uses — so the third-person gun pose hook was firing once per
  equipped armor slot, per frame, while in battle mode, and armor
  stopped being drawn. The `setupAnim` TAIL inject is now limited to
  `PlayerModel`; mob models (zombies, skeletons, armor stands) are
  excluded for the same reason. Vanilla and modded armor were both
  affected, and the armor itself was never lost — only its rendering.
  Confirmed fixed in testing.

## [1201-0.5.1] - 2026-08-09

### Fixed
- **Startup crash.** `1201-0.5.0` shipped without its Mixin refmap and
  with the `@Shadow` fields left under Mojang names, so in production
  Mixin failed with
  `@Shadow field head was not located in the target class
  net.minecraft.client.model.HumanoidModel. No refMap loaded.`
  and the game died during mod loading — before the main menu.
  (Reported as [#1](https://github.com/Ardelhite/epic-tacz/issues/1)
  and on CurseForge.)
  Root cause: `org.gradle.caching=true` let Gradle restore `compileJava`
  from the build cache, which skips MixinGradle's annotation processor.
  The AP writes the refmap and the reobf mapping outside `compileJava`'s
  declared outputs, so both silently vanished from the artifact.
  `compileJava` is now excluded from the build cache, and a new
  `verifyMixinArtifact` task fails the build if the packaged jar is
  missing its refmap or still carries unmapped `@Shadow` names.
- Third-person gun pose no longer plays the walking lower-body
  animation while standing still: the `setupAnim` TAIL inject was
  passing `ageInTicks` where TacZ passes `limbSwingAmount`, and TacZ
  uses that value for its `> 0.05` walk check.

### Changed
- `@Shadow` fields are now marked `@Final`, matching TacZ's own
  `HumanoidModelMixin` and the target fields.
- `src/main/resources/META-INF/mods.toml` is now tracked in git. It
  never was, so `1201-0.5.0` was built from an untracked working-tree
  file; a fresh clone produced a jar with no mod manifest at all.
  `verifyMixinArtifact` now also rejects a jar without `mods.toml` or
  with unexpanded `${...}` placeholders in it.

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
