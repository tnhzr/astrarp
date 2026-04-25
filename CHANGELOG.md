# Changelog

All notable changes to this project will be documented in this file.

## [1.0.0] — 2026-04-25

### Added — Initial release

- Modular core: `modules.yml` toggles every feature on/off independently.
- **status** module: `/rp`, `/nrp` (self toggle and admin force), PlaceholderAPI
  expansion (`%astrarp_status%`, `%astrarp_status_raw%`), LuckPerms meta bridge
  for FlectonePulse and TAB.
- **names** module: `/rpname set "First Last"` with configurable cooldown,
  `/rpaname` admin override, command-preprocessor that translates quoted
  RP-names into real player names so vanilla and other plugin commands resolve
  them (`/tp "Джон Доу"`), placeholder `%astrarp_rpname%`.
- **keepinventory** module: PvP-only smart KeepInventory honoring victim/killer
  RP statuses; full rule table configurable in YAML; XP keep toggle; PvE deaths
  use vanilla behaviour.
- **frames** module: infinite item frames with full NBT preservation, per-player
  take limit, SQLite-backed storage, admin commands `/ifr`, `/ifr del`,
  `/ifr reset local|global`, `/ifr check`, plus rotation and break protection.
- **gm** module: `/feel <player|selector> "text"`; `/rpc create` chest GUI with
  chat-driven field editing, `/rpc show` browser, `/rpc <id> "text" [radius]
  [player]` for global, local, or private NPC voices.
- Localization: `messages_ru.yml` (primary) and `messages_en.yml`.
- SQLite (WAL) storage on a dedicated executor thread.
- Soft-depends on PlaceholderAPI, LuckPerms, FlectonePulse, TAB; all optional.
