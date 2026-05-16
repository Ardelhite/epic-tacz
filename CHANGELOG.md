# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

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
