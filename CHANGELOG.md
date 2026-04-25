# Changelog

All notable changes to this project will be documented in this file.

## [1.0.2] — 2026-04-25

### Fixed

- `Text#parse` no longer round-trips through the legacy serializer, which had
  been escaping every MiniMessage tag in the prefix and message bodies. As a
  result `<gradient>`, `<red>`, `<yellow>`, etc. now render correctly in chat
  and on the console instead of leaking as raw text.
- Editor chat capture in `/rpc create` and `/rpc edit` no longer loses the
  player's input under FlectonePulse. The chat listener now fires at
  `LOWEST` with `ignoreCancelled=false`, drains the recipient set and also
  intercepts the legacy `AsyncPlayerChatEvent` so other chat plugins cannot
  re-broadcast or pre-empt the editor flow.

### Changed

- RP names accept one or two words instead of requiring «Имя Фамилия». Word
  bounds are configurable via `validation.min_words` / `validation.max_words`
  in `modules/names.yml` (defaults 1 and 2). Legacy `validation.require_space`
  is still honored as a fallback.

### Added

- `notify_unset` block in `modules/names.yml` and `names.notify_unset` message
  key — players without an RP name are reminded on join with a configurable
  message and delay.

## [1.0.1] — 2026-04-25

### Fixed

- Switch to classic `plugin.yml` (Bukkit-style) so commands declared in YAML
  are honored on Paper 1.21.8. Paper plugins (`paper-plugin.yml`) reject
  `JavaPlugin#getCommand` and require `registerCommand` instead, which broke
  plugin enable. All commands and permissions are unchanged.

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
