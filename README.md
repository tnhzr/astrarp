# AstraRP

[Русская версия ниже](#russian)

Modular RolePlay toolkit for **Paper 1.21.8**. AstraRP gives Game Masters and
admins everything they need to run a stable RP server: status flags, custom
display names, smart KeepInventory in PvP, infinite item frames for quest
items, and a complete NPC voice system with a chest GUI.

## Features

| Module | Highlights |
| --- | --- |
| **status** | `/rp` / `/nrp` self toggles; `/rp <player>` admin force; placeholder `%astrarp_status%` and `%astrarp_status_raw%`; LuckPerms meta hook for FlectonePulse / TAB chat formats. |
| **names** | `/rpname set "First Last"` with cooldown; `/rpaname` admin override with no cooldown; auto-rewrite of quoted RP-names in commands so `/tp "John Doe"` works; placeholder `%astrarp_rpname%`. |
| **keepinventory** | PvP-only smart KeepInventory: `RP×RP` drops, `NRP victim` keeps, `RP victim killed by NRP` keeps. PvE deaths follow vanilla rules. Configurable rule table. |
| **frames** | Infinite item frames preserve quest items: clicking gives a copy with full NBT (lore, custom model, enchants); per-player limit; `/ifr`, `/ifr del`, `/ifr reset local|global`, `/ifr check` admin tools. |
| **gm** | `/feel <player|@selector> "text"` for atmospheric whispers; `/rpc create` chest-GUI for NPC characters; `/rpc <id> "text" [radius] [player]` to speak as an NPC, locally or privately. |

## Build

```bash
mvn -B package
# -> target/AstraRP-1.0.0.jar
```

Drop the jar into `plugins/` of a Paper 1.21.8 server. Default configs are
copied to `plugins/AstraRP/` on first start.

## Soft dependencies

| Plugin | What it unlocks |
| --- | --- |
| [PlaceholderAPI](https://wiki.placeholderapi.com/) | `%astrarp_status%`, `%astrarp_status_raw%`, `%astrarp_rpname%`, `%astrarp_rpname_raw%` |
| [LuckPerms](https://luckperms.net/wiki/Home) | Meta keys `astrarp_status` and `astrarp_rpname` for chat formats. |
| [FlectonePulse](https://flectone.net/ru/pulse/docs) | Use the LuckPerms meta keys above in your message templates. |
| [TAB](https://github.com/NEZNAMY/TAB/wiki) | Use the LuckPerms meta keys in your tab format. |

All dependencies are optional. Missing plugins are skipped; AstraRP keeps working.

## Configuration

| File | Purpose |
| --- | --- |
| `config.yml` | Language (`ru` / `en`), prefix, debug flag. |
| `modules.yml` | Per-module on/off toggles. |
| `modules/status.yml` | RP/NRP icons, raw values, LuckPerms hook key. |
| `modules/names.yml` | Cooldown, validation regex, format, Brigadier rewrite toggle. |
| `modules/keepinventory.yml` | PvP-only flag, XP keep, full rule matrix. |
| `modules/frames.yml` | Frame protection toggles. |
| `modules/gm.yml` | `/feel` style, `/rpc` chat format, GUI preview. |
| `messages_ru.yml` / `messages_en.yml` | Every player-facing message in MiniMessage. |

All numeric values, sounds, cooldowns and texts live in YAML — no hard-coding.

## Permissions

| Node | Default | Description |
| --- | --- | --- |
| `astrarp.admin.reload` | op | `/astrarp reload` |
| `astrarp.status.use` | true | `/rp` / `/nrp` for self |
| `astrarp.status.admin` | op | Force RP/NRP on others |
| `astrarp.name.set` | true | Set own RP name |
| `astrarp.name.admin` | op | Force RP names, ignore cooldown |
| `astrarp.feel.use` | op | `/feel` |
| `astrarp.rpc.use` | op | `/rpc` |
| `astrarp.ifr.admin` | op | Infinite item frames |

## Storage

A single SQLite database `plugins/AstraRP/data.db` (WAL mode) keeps RP
statuses, RP names, NPC characters, and infinite-frame state. All reads and
writes happen on a dedicated thread, so the main server tick never blocks.

---

<a id="russian"></a>

# AstraRP (RU)

Многофункциональный модульный плагин для серверов **Paper 1.21.8**, направленный
на улучшение RolePlay-составляющей и упрощение работы Game Master'ов и
администраторов.

## Модули

| Модуль | Команды и фичи |
| --- | --- |
| **status** | `/rp`, `/nrp` (переключение себе/принудительно другому), плейсхолдеры `%astrarp_status%`, `%astrarp_status_raw%`, мета LuckPerms для FlectonePulse и TAB. |
| **names** | `/rpname set "Имя Фамилия"` с кулдауном, `/rpaname` без кулдауна, авто-замена цитированных РП-имён в командах (`/tp "Джон Доу"`), плейсхолдер `%astrarp_rpname%`. |
| **keepinventory** | Smart-KeepInventory для PvP: RP×RP — фулл лут, NRP-жертва — сохраняет, RP-жертва от NRP — сохраняет. PvE — ванильно. Гибкая таблица правил. |
| **frames** | Бесконечные рамки. Игрок получает копию предмета (полный NBT, лор, чары, кастомная модель). Лимит «один раз на игрока». Команды `/ifr`, `/ifr del`, `/ifr reset local|global`, `/ifr check`. |
| **gm** | `/feel <ник|селектор> "текст"` — атмосферный текст, `/rpc create` — GUI для NPC, `/rpc <id> "текст" [радиус] [игрок]` — реплика NPC локально или приватно. |

## Сборка

```bash
mvn -B package
# -> target/AstraRP-1.0.0.jar
```

Положите jar в `plugins/`. При первом запуске будут созданы конфиги в
`plugins/AstraRP/`.

## Soft-зависимости

* PlaceholderAPI — плейсхолдеры
* LuckPerms — мета-ключи `astrarp_status` и `astrarp_rpname`
* FlectonePulse — используйте мета-ключи в шаблоне чата
* TAB — используйте мета-ключи в формате таблиста

Все зависимости опциональны.

## Конфигурация

| Файл | Назначение |
| --- | --- |
| `config.yml` | Язык, префикс, дебаг. |
| `modules.yml` | Вкл/выкл модулей. |
| `modules/status.yml` | Иконки и сырые значения RP/NRP. |
| `modules/names.yml` | Кулдаун, валидация, формат отображаемого имени. |
| `modules/keepinventory.yml` | Таблица решений и опции. |
| `modules/frames.yml` | Защита рамки. |
| `modules/gm.yml` | Стили `/feel` и `/rpc`. |
| `messages_ru.yml` / `messages_en.yml` | Все сообщения (MiniMessage). |

Никакого хардкода — всё через YAML.

## Хранилище

Один SQLite файл `plugins/AstraRP/data.db` (WAL). Чтение/запись — в отдельном
потоке: основной тик сервера не блокируется.

## Лицензия

MIT — см. [LICENSE](LICENSE).
