# Changelog

All notable changes to this project will be documented in this file.

## [1.0.6] — 2026-04-25

### Added

- **ChatHeads — серверный мост.** Плагин подмешивает к каждому чат-сообщению
  почти-невидимый суффикс `(<ник>)` тёмным цветом. Этого хватает, чтобы
  ChatHeads нашёл автора даже когда сообщение идёт через FlectonePulse и
  «настоящий» ник нигде не виден. Управляется через `chatheads.enabled` и
  `chatheads.suffix_format` в `config.yml`.

### Changed

- **RPC-редактор сжат до одинарного сундука (27 слотов).** Вместо двух рядов
  по 15 цветных кнопок на каждое поле — по одному «крутящемуся» слоту
  «Цвет — имя» / «Цвет — стиль» (ЛКМ — следующий цвет, ПКМ — предыдущий,
  Shift+ЛКМ — убрать цвет) и по одному слоту «Оформление — имя/стиль»
  (ЛКМ — жирный, ПКМ — курсив). Кнопки полей по-прежнему ведут в чатовый
  ввод, так что произвольные `<#hex>`, `<gradient>` и т. п. остаются
  доступны.
- **Превью с цветами.** Слот превью в редакторе и лор у голов в списке
  персонажей теперь рендерят MiniMessage напрямую через Component-лор —
  цвета, градиенты и теги видно «как в чате», а не серым plain-текстом.

## [1.0.5] — 2026-04-25

### Added

- **`/rpname find <ник|РП-имя>`** — двусторонний поиск: РП-имя → оригинальный ник
  и наоборот. Право `astrarp.name.find` (по умолчанию у всех).
- **Help-команды:** `/arp help`, `/ifr help`, `/rpc help`, `/rpname help`
  выводят список своих под-команд.
- **RPC-редактор: цвета и форматирование одним кликом.** В GUI создания
  персонажа добавлены ряды цветовых кнопок (15 ванильных цветов отдельно
  для имени и для стиля), кнопки `B` / `I` для жирности / курсива и
  «сбросить оформление». Превью в слоте 7 обновляется после каждого клика.
- **ChatHeads** — в README добавлена инструкция по совместимости: какие
  настройки мода (`senderDetection`) и что в FlectonePulse-шаблоне нужны,
  чтобы голова отправителя снова появлялась рядом с РП-именем.

### Changed

- **Статус по умолчанию — NRP.** При первом заходе игрок получает NRP
  вместо «нет статуса». Команды `/rp` и `/nrp` теперь только переключают
  между RP и NRP, выключенного состояния больше нет (в БД остаётся
  совместимость со старыми `NONE`-записями).
- **Имя — почти любые символы.** Снят регекс на буквы; теперь допустимо
  «Гриффит | Tannhausers», «:eyes:», «☆Селена☆» — запрещены только
  управляющие ASCII-символы. Жёсткая валидация по-прежнему доступна как
  опционал через `validation.regex`.
- **Палитра плагина** — основной акцент теперь градиент
  `<gradient:#ffb000:#ffe167>` и `#ffda4a`. Все help-сообщения, заголовки
  GUI, успехи и подтверждения перекрашены в новую палитру. Цветовая
  семантика статусов (зелёный/красный) сохранена.
- **`/rpc <id>` — без авто-`[ ]`.** Дефолтный `rpc.format` больше не
  оборачивает имя в скобки, чтобы можно было использовать свои. Старые
  кастомные форматы продолжают работать как есть.

## [1.0.4] — 2026-04-25

### Fixed

- `/rpc create` editor: clicking a field button after typing the previous
  value into chat no longer leaves the GUI unresponsive. `closeInventory()`
  is now scheduled on the next tick instead of being called from inside the
  click handler — the synchronous path was racing with Paper's inventory
  packet sequencing and the reopened editor was rendering with a stale
  client view.
- `%astrarp_status%` keeps its colour. The PlaceholderAPI expansion used to
  strip every `<color>` tag through `PlainTextComponentSerializer`, so
  FlectonePulse received the literal text `RP` and rendered it white. The
  expansion now returns raw MiniMessage so chat plugins that re-parse it
  (FlectonePulse, default Paper chat) keep the colour, and the TAB bridge
  pre-renders to legacy `§` codes so TAB shows colour too.

### Added

- Two new placeholder variants for consumers that don't speak
  MiniMessage: `%astrarp_status_legacy%` (legacy `§` codes) and
  `%astrarp_status_plain%` (no formatting at all). `%astrarp_status%`
  itself is now MiniMessage by default.

### Removed

- `RpcGui` no longer auto-clears the in-memory edit draft on inventory
  close. The five-tick delayed clear was racing with the chat-input flow
  and silently nulling the draft, which is what made every editor button
  appear dead after the first ID input. Drafts are now cleared only by
  Save / Cancel / Delete or on plugin disable.

## [1.0.3] — 2026-04-25

### Fixed

- PlaceholderAPI registration is now resilient: the expansion is unregistered
  before re-register so `/papi reload` and `/astrarp reload` cycles do not
  leave a stale instance behind. The boolean result of `register()` is
  logged so a silent failure is visible in startup output.

### Added

- Direct TAB integration via TAB-API. `%astrarp_rpname%`,
  `%astrarp_rpname_raw%`, `%astrarp_status%`, `%astrarp_status_raw%` are
  registered with TAB's `PlaceholderManager`, so `customtabname` /
  `customtagname` / `tabprefix` resolve them even on servers that do not
  run PlaceholderAPI alongside TAB.
- `/astrarp debug [player]` (permission `astrarp.admin.reload`) — prints
  detected integrations and the resolved values of every AstraRP
  placeholder for the target. If PlaceholderAPI is present it also runs
  `PlaceholderAPI#setPlaceholders` so the output reflects what other
  plugins would see.
- README now documents the FlectonePulse `name.display` template that
  must point at `%astrarp_rpname%` to actually replace the chat name, and
  the TAB group block that uses the new placeholders.

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
