<div align="center">

<img src="assets/icon.png" alt="AstraRP" width="220" />

# AstraRP

**Модульный RolePlay-инструментарий для Paper 1.21.8**
*Modular RolePlay toolkit for Paper 1.21.8*

[![Paper](https://img.shields.io/badge/Paper-1.21.8-0AC2C5?style=for-the-badge&logo=papermc&logoColor=white)](https://papermc.io)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://adoptium.net)
[![License](https://img.shields.io/badge/License-MIT-9d4edd?style=for-the-badge)](LICENSE)
[![Release](https://img.shields.io/github/v/release/tnhzr/astrarp?style=for-the-badge&color=ffb000)](https://github.com/tnhzr/astrarp/releases/latest)

[Возможности](#возможности) ·
[Команды](#команды-и-права) ·
[Плейсхолдеры](#плейсхолдеры) ·
[Установка](#установка) ·
[Конфигурация](#конфигурация) ·
[Интеграции](#интеграции) ·
[English](#english)

</div>

---

## Возможности

AstraRP — это полный набор инструментов для администраторов и Game Master'ов
RP-сервера: переключение RP/NRP-статусов, кастомные имена с подстановкой
в команды, умный KeepInventory только для PvP, бесконечные item-frames
для квестовых предметов и GUI-конструктор NPC-персонажей.

| Модуль | Что делает |
| --- | --- |
| <kbd>**status**</kbd> | RP / NRP-статусы, переключение `/rp` `/nrp` себе или другому, плейсхолдеры в трёх форматах (MiniMessage / legacy `§` / plain), мета-мост в LuckPerms. По умолчанию игрок получает NRP при первом заходе. |
| <kbd>**names**</kbd> | РП-имена с настраиваемым кулдауном, валидацией по словам и опциональным regex. Команды `/rpname set "Имя Фамилия"`, `/rpname find <ник\|РП-имя>`, `/rpaname` для админов. Авто-замена цитированных имён в любых командах: `/tp "Джон Доу"` → `/tp Notch`. Напоминание новичкам без имени при заходе. |
| <kbd>**keepinventory**</kbd> | Smart-KeepInventory только для PvP с матрицей решений по статусам. RP × RP — фулл-лут; NRP-жертва — сохраняет вещи; RP-жертва от NRP — сохраняет (защита от дм-щиков). PvE — ванильно. Полная YAML-таблица. Опциональный keep XP. |
| <kbd>**frames**</kbd> | Бесконечные рамки. Игрок получает копию предмета с полным NBT (лор, чары, кастомная модель). Лимит «один раз на игрока на рамку». Защита от поворотов и поломки рамки. |
| <kbd>**gm**</kbd> | `/feel <ник\|селектор> "<текст>"` — атмосферное описание для игрока или группы. `/rpc create` — GUI-визард для NPC-персонажа: имя, цвет имени, стиль фразы, цвет стиля, жирность/курсив. `/rpc <id> "<текст>" [радиус] [игрок]` — реплика NPC: глобально, в радиусе или приватно. |

> [!TIP]
> Каждый модуль независим. Любой можно отключить в `modules.yml` без потери остальных.

## Команды и права

Все команды работают и без прав у обычного игрока, если в таблице ниже стоит `default: true`.

| Команда | Алиасы | Описание | Право |
| --- | --- | --- | --- |
| `/astrarp reload` | `/arp` | Полная перезагрузка конфигов и модулей | `astrarp.admin.reload` |
| `/astrarp debug [игрок]` | `/arp debug` | Дамп всех плейсхолдеров и интеграций | `astrarp.admin.reload` |
| `/astrarp help` | `/arp help` | Список под-команд | — |
| `/rp [игрок]` | — | Переключить RP-статус | `astrarp.status.use` / `.admin` |
| `/nrp [игрок]` | — | Переключить NRP-статус | `astrarp.status.use` / `.admin` |
| `/rpname set "<имя>"` | — | Установить РП-имя | `astrarp.name.set` |
| `/rpname find <ник\|РП-имя>` | — | Двусторонний поиск | `astrarp.name.find` |
| `/rpname help` | — | Список под-команд | — |
| `/rpaname <ник> set "<имя>"` | — | Принудительное РП-имя | `astrarp.name.admin` |
| `/feel <ник\|@селектор> "<текст>"` | — | Атмосферный текст | `astrarp.feel.use` |
| `/rpc` `/rpc show` | — | GUI-список NPC-персонажей | `astrarp.rpc.use` |
| `/rpc create [id]` | — | Открыть GUI-визард создания NPC | `astrarp.rpc.use` |
| `/rpc edit <id>` | — | Редактировать существующего NPC | `astrarp.rpc.use` |
| `/rpc <id> "<текст>" [радиус] [игрок]` | — | Реплика от лица NPC | `astrarp.rpc.use` |
| `/rpc help` | — | Список под-команд | — |
| `/ifr` | `/infiniteitemframe` | Режим создания бесконечной рамки | `astrarp.ifr.admin` |
| `/ifr del` | — | Режим удаления | `astrarp.ifr.admin` |
| `/ifr reset local [ник]` | — | Сброс лимита одной рамки | `astrarp.ifr.admin` |
| `/ifr reset global` + `/ifr confirm` | — | Глобальный сброс | `astrarp.ifr.admin` |
| `/ifr check` | — | Список забравших | `astrarp.ifr.admin` |
| `/ifr help` | — | Список под-команд | — |

## Плейсхолдеры

Регистрируются в PlaceholderAPI **и** напрямую в TAB через TAB-API
(работают даже если PAPI не установлен).

| Плейсхолдер | Возвращает |
| --- | --- |
| `%astrarp_status%` | Иконка статуса в MiniMessage (по дефолту `<green><b>RP</b></green>` / `<red><b>NRP</b></red>`) |
| `%astrarp_status_legacy%` | То же самое, но в формате legacy `§` — для старых плагинов |
| `%astrarp_status_plain%` | Чистый текст без форматирования |
| `%astrarp_status_raw%` | Сырое значение: `RP`, `NRP` или пусто |
| `%astrarp_rpname%` | РП-имя (рендерится с заданным в `names.yml` форматом) или ник, если не задано |
| `%astrarp_rpname_raw%` | Сырое РП-имя или пустая строка |

## RPC-редактор (NPC-голоса)

`/rpc create` открывает одинарный сундук-GUI:

- **Поля:** `ID`, `displayName` (имя NPC), `style` (префикс перед текстом),
  `preview` (тестовая фраза). ЛКМ по полю → текст вводится в чат, GUI
  переоткрывается с новым значением.
- **Цвета:** один «крутящийся» слот для имени и один для стиля.
  ЛКМ — следующий из 15 ванильных цветов, ПКМ — предыдущий, Shift+ЛКМ —
  убрать цвет. Для произвольных `<#hex>` или `<gradient>` — вводи теги
  прямо в чат-режиме поля.
- **Оформление:** один слот для имени, один для стиля. ЛКМ — toggle жирность,
  ПКМ — toggle курсив.
- **Превью** в слоте 7 рендерит итоговую реплику с MiniMessage в реальном времени.
- **Сохранение:** иконка в правом нижнем слоте. Cancel и Delete — рядом.

После создания `/rpc <id> "Текст" [радиус] [игрок]` отправляет реплику:

- без аргументов — всем онлайн;
- `[радиус]` — только игрокам в радиусе клеток вокруг отправителя;
- `[игрок]` — приватно конкретному получателю.

## Установка

1. Сборка из исходников:
   ```bash
   mvn -B package
   # → target/AstraRP-1.0.7.jar
   ```
   Либо качай готовый jar с [последнего релиза](https://github.com/tnhzr/astrarp/releases/latest).
2. Положи jar в `plugins/` сервера Paper 1.21.8.
3. При первом запуске будут созданы конфиги в `plugins/AstraRP/`.

> [!IMPORTANT]
> Требуется **Java 21** (Paper 1.21+).

## Конфигурация

| Файл | Назначение |
| --- | --- |
| `config.yml` | Язык (`ru` / `en`), префикс, баннер, отладка |
| `modules.yml` | Переключатели модулей (status/names/keepinventory/frames/gm) |
| `modules/status.yml` | Иконки и сырые значения RP/NRP, ключ LuckPerms-меты |
| `modules/names.yml` | Кулдаун, регулярка валидации, формат, кулдаун, `notify_unset`, тоггл Brigadier-замены |
| `modules/keepinventory.yml` | `pvp_only`, `keep_xp`, полная таблица решений по статусам |
| `modules/frames.yml` | Защита рамки и предмета |
| `modules/gm.yml` | Стиль `/feel`, формат `/rpc`, превью GUI |
| `messages_ru.yml` / `messages_en.yml` | Все сообщения (MiniMessage) |

> [!NOTE]
> Хардкод запрещён: тексты, звуки, шансы, тайминги, форматы — всё в YAML.
> Поддерживается современный MiniMessage и legacy `&`-цвета.

## Хранилище

`plugins/AstraRP/data.db` — SQLite в WAL-режиме. Чтение/запись идут на
выделенном потоке, основной тик сервера не блокируется.

| Таблица | Что хранит |
| --- | --- |
| `rp_status` | RP/NRP-статус игрока |
| `rp_names` | РП-имя и таймер кулдауна |
| `infinite_frames` | Бесконечные рамки |
| `infinite_frame_takes` | История взятий |
| `rp_characters` | NPC-персонажи RPC-модуля |

## Интеграции

Все опциональны. Отсутствующие плагины пропускаются без ошибок.

| Плагин | Что даёт |
| --- | --- |
| [PlaceholderAPI](https://wiki.placeholderapi.com/) | Все плейсхолдеры выше доступны как `%astrarp_*%` |
| [LuckPerms](https://luckperms.net/wiki/Home) | Мета-ключи `astrarp_status` / `astrarp_rpname` пишутся в LP-мету игрока |
| [FlectonePulse](https://flectone.net/ru/pulse/docs) | Используй мета-ключи или плейсхолдеры в шаблонах чата |
| [TAB](https://github.com/NEZNAMY/TAB/wiki) | Плейсхолдеры регистрируются напрямую через TAB API — даже без PAPI |

### FlectonePulse — РП-имя в чате

Замени `<player>` на `%astrarp_rpname%` в локализации FlectonePulse:

📂 `plugins/FlectonePulse/localizations/<lang>.yml → message.format.name_.display`

```yaml
message:
  format:
    name_:
      display: "<click:suggest_command:'/msg <player>'><hover:show_text:'<fcolor:2>Написать <player>'><vault_prefix><stream_prefix><fcolor:2>%astrarp_rpname%<afk_suffix><vault_suffix></hover></click>"
```

Затем `/fp reload` (или `/fpulse reload`). В `plugins/FlectonePulse/integration.yml`
должен быть включён PAPI:

```yaml
placeholderapi:
  enable: true
```

Чтобы добавить статус-индикатор перед именем — допиши `%astrarp_status%`
в начало того же шаблона.

### TAB — РП-имя в табе и над головой

Плагин сам регистрирует плейсхолдеры в TAB. Минимальный шаблон в `plugins/TAB/groups.yml`:

```yaml
_DEFAULT_:
  tabprefix: '%luckperms-prefix%'
  tagprefix: '%luckperms-prefix%'
  customtabname: '%astrarp_rpname%'
  tabsuffix: '%luckperms-suffix%'
  tagsuffix: '%luckperms-suffix%'
```

После сохранения — `/tab reload`.

### LuckPerms meta-bridge

Если хочется собирать имя через мету (например, для совместимости со
старыми шаблонами), включи `luckperms.write_meta: true` в `modules/names.yml`
или `modules/status.yml` — AstraRP запишет соответствующий ключ
в LuckPerms-мету игрока, и его можно использовать как
`%luckperms_meta_astrarp_rpname%` или `%luckperms_meta_astrarp_status%`.

### Кастомные шрифты (CraftEngine / ItemsAdder)

Если ты используешь кастомные шрифты в LuckPerms-префиксе, **не** оборачивай
префикс в `<font:custom:default>...</font>` MM-теги — TAB при склейке
`tabprefix + customtabname + tabsuffix` может не сохранить границы scope,
и font-scope утечёт на РП-имя, превратив его в коробочки.

Безопасные варианты:

- регистрируй глифы в `minecraft:default` font (не в кастомном) — тогда
  префикс это просто PUA-символ без обёртки;
- или используй CraftEngine `<image:namespace:id>` тег **только в чате** —
  в TAB он не работает, потому что TAB парсит MiniMessage до того, как
  CraftEngine получит пакет.

## Отладка

`/astrarp debug <ник>` (право `astrarp.admin.reload`) распечатает значения
всех плейсхолдеров для игрока, найденные интеграции и проверит, что PAPI
действительно их парсит. Полезно, если что-то рендерится сырым текстом.

---

<a id="english"></a>

# AstraRP (English)

Modular RolePlay toolkit for **Paper 1.21.8**: RP/NRP status flags, custom
display names with command-rewriting, smart KeepInventory in PvP, infinite
item frames for quest items, and a chest-GUI NPC voice system.

## Features

| Module | Highlights |
| --- | --- |
| **status** | `/rp` / `/nrp` self-toggle, `/rp <player>` admin force, MiniMessage / legacy / plain placeholder variants, optional LuckPerms meta bridge. New players default to NRP. |
| **names** | `/rpname set "First Last"` with cooldown, `/rpname find`, `/rpaname` admin override; command preprocessor rewrites quoted RP-names so vanilla and other-plugin commands resolve them; join reminder for players without a name. |
| **keepinventory** | PvP-only smart KeepInventory honoring victim/killer RP statuses; full configurable rule table; XP toggle; PvE deaths follow vanilla. |
| **frames** | Infinite item frames preserve quest items with full NBT; per-player take limit; rotation/break protection; admin commands `/ifr`, `/ifr del`, `/ifr reset local\|global`, `/ifr check`. |
| **gm** | `/feel` for atmospheric text; `/rpc create` chest-GUI for NPC characters with one-click colour cycling and bold/italic toggles; `/rpc <id> "text" [radius] [player]` for global, local, or private NPC voices. |

## Commands

See the Russian table above — command names and arguments are identical
in both locales; only message text differs.

## Placeholders

| Placeholder | Returns |
| --- | --- |
| `%astrarp_status%` | Status icon as MiniMessage |
| `%astrarp_status_legacy%` | Same value with legacy `§` codes |
| `%astrarp_status_plain%` | Plain text, no formatting |
| `%astrarp_status_raw%` | Raw value: `RP`, `NRP` or empty |
| `%astrarp_rpname%` | RP name (rendered with the configured format) or fallback to nickname |
| `%astrarp_rpname_raw%` | Raw RP name, empty if unset |

Registered in both PlaceholderAPI and TAB API directly, so TAB resolves
them even without PAPI installed.

## Build & install

```bash
mvn -B package
# → target/AstraRP-1.0.7.jar
```

Or grab the latest jar from
[Releases](https://github.com/tnhzr/astrarp/releases/latest).
Drop into `plugins/` of a Paper 1.21.8 server. Default configs are copied
to `plugins/AstraRP/` on first start. **Java 21 required.**

## Soft-dependencies

PlaceholderAPI · LuckPerms · FlectonePulse · TAB — all optional.

## Storage

SQLite (WAL) at `plugins/AstraRP/data.db` on a dedicated executor thread —
the main server tick never blocks on database I/O.

## License

MIT — see [LICENSE](LICENSE).
