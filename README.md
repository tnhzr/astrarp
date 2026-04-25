<div align="center">

<img src="assets/icon.png" alt="AstraRP" width="220" />

# AstraRP

**Модульный RolePlay-инструментарий для Paper 1.21.8**
*Modular RolePlay toolkit for Paper 1.21.8*

[![Paper](https://img.shields.io/badge/Paper-1.21.8-0AC2C5?style=for-the-badge&logo=papermc&logoColor=white)](https://papermc.io)
[![Java](https://img.shields.io/badge/Java-21-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://adoptium.net)
[![License](https://img.shields.io/badge/License-MIT-9d4edd?style=for-the-badge)](LICENSE)
[![Status](https://img.shields.io/badge/status-stable-3a86ff?style=for-the-badge)](#)

[Возможности](#возможности) ·
[Команды](#команды) ·
[Установка](#установка) ·
[Конфигурация](#конфигурация) ·
[English](#english)

</div>

---

## Возможности

AstraRP даёт админам и Game Master'ам всё необходимое для стабильного RP-сервера: статусы готовности к РП, кастомные имена, умный KeepInventory в PvP, бесконечные рамки для квестовых предметов и полноценную систему NPC-голосов с GUI.

| Модуль | Описание |
| --- | --- |
| <kbd>**status**</kbd> | RP / NRP-статусы. `/rp`, `/nrp` — переключение себе или другому. Плейсхолдеры `%astrarp_status%`, `%astrarp_status_raw%`. Мост LuckPerms-меты для FlectonePulse и TAB. |
| <kbd>**names**</kbd> | РП-имена с кулдауном. `/rpname set "Имя Фамилия"`, `/rpaname` без кулдауна для админов. Авто-замена цитированных РП-имён в командах: `/tp "Джон Доу"` работает как `/tp Notch`. Плейсхолдер `%astrarp_rpname%`. |
| <kbd>**keepinventory**</kbd> | Smart-KeepInventory только для PvP. RP × RP — фулл лут; NRP-жертва — сохраняет вещи; RP-жертва от NRP — сохраняет (защита от дм-щиков). PvE — ванильно. Полная таблица правил в YAML. |
| <kbd>**frames**</kbd> | Бесконечные рамки. Игрок получает копию предмета с полным NBT (лор, чары, кастомная модель). Лимит «один раз на игрока на рамку». Команды `/ifr`, `/ifr del`, `/ifr reset local|global`, `/ifr check`. |
| <kbd>**gm**</kbd> | GM-инструменты. `/feel <ник|селектор> "<текст>"` — атмосферный текст. `/rpc create` — GUI-визард для NPC. `/rpc <id> "<текст>" [радиус] [игрок]` — реплика NPC локально, глобально или приватно. |

> [!TIP]
> Каждый модуль независим. Любой можно отключить в `modules.yml` без потери остальных.

## Команды

| Команда | Алиасы | Описание | Право |
| --- | --- | --- | --- |
| `/astrarp reload` | `/arp` | Полная перезагрузка конфигов и модулей | `astrarp.admin.reload` |
| `/rp [игрок]` | — | Переключить RP-статус | `astrarp.status.use` / `.admin` |
| `/nrp [игрок]` | — | Переключить NRP-статус | `astrarp.status.use` / `.admin` |
| `/rpname set "<имя>"` | — | Установить РП-имя | `astrarp.name.set` |
| `/rpaname <ник> set "<имя>"` | — | Принудительное РП-имя | `astrarp.name.admin` |
| `/feel <ник|@селектор> "<текст>"` | — | Атмосферный текст игроку/группе | `astrarp.feel.use` |
| `/rpc` `/rpc show` | — | GUI-список NPC-персонажей | `astrarp.rpc.use` |
| `/rpc create [id]` | — | Открыть GUI-визард создания NPC | `astrarp.rpc.use` |
| `/rpc <id> "<текст>" [радиус] [игрок]` | — | Реплика от лица NPC | `astrarp.rpc.use` |
| `/ifr` | `/infiniteitemframe` | Режим создания бесконечной рамки | `astrarp.ifr.admin` |
| `/ifr del` | — | Режим удаления | `astrarp.ifr.admin` |
| `/ifr reset local [ник]` | — | Сброс лимита одной рамки | `astrarp.ifr.admin` |
| `/ifr reset global` + `/ifr confirm` | — | Глобальный сброс с подтверждением | `astrarp.ifr.admin` |
| `/ifr check` | — | Проверка списка забравших | `astrarp.ifr.admin` |

## Плейсхолдеры (PlaceholderAPI)

| Плейсхолдер | Возвращает |
| --- | --- |
| `%astrarp_status%` | Иконка статуса (MiniMessage из `modules/status.yml`) |
| `%astrarp_status_raw%` | Сырое значение: `RP`, `NRP` или пусто |
| `%astrarp_rpname%` | РП-имя или ник, если не задано |
| `%astrarp_rpname_raw%` | РП-имя или пустая строка |

## Установка

1. Сборка из исходников:
   ```bash
   mvn -B package
   # → target/AstraRP-1.0.0.jar
   ```
2. Положите jar в `plugins/` сервера Paper 1.21.8.
3. При первом запуске будут созданы конфиги в `plugins/AstraRP/`.

> [!IMPORTANT]
> Требуется **Java 21** (Paper 1.21+).

### Soft-зависимости

Все опциональны. Отсутствующие плагины пропускаются без ошибок.

| Плагин | Что даёт |
| --- | --- |
| [PlaceholderAPI](https://wiki.placeholderapi.com/) | Плейсхолдеры |
| [LuckPerms](https://luckperms.net/wiki/Home) | Мета-ключи `astrarp_status` и `astrarp_rpname` для шаблонов |
| [FlectonePulse](https://flectone.net/ru/pulse/docs) | Используйте мета-ключи в формате чата |
| [TAB](https://github.com/NEZNAMY/TAB/wiki) | Используйте мета-ключи в формате таблиста |

## Конфигурация

| Файл | Назначение |
| --- | --- |
| `config.yml` | Язык (`ru` / `en`), префикс, баннер, отладка |
| `modules.yml` | Переключатели модулей |
| `modules/status.yml` | Иконки и сырые значения RP/NRP, ключ LuckPerms |
| `modules/names.yml` | Кулдаун, регулярка валидации, формат, переключатель Brigadier-замены |
| `modules/keepinventory.yml` | Опции PvP-only, XP, полная таблица решений |
| `modules/frames.yml` | Защита рамки и предмета |
| `modules/gm.yml` | Стиль `/feel`, формат `/rpc`, превью GUI |
| `messages_ru.yml` / `messages_en.yml` | Все сообщения (MiniMessage) |

> [!NOTE]
> Хардкод запрещён: все значения, тексты, звуки, шансы и тайминги вынесены в YAML. Поддерживается MiniMessage и наследие `&`-цветов.

## Хранилище

`plugins/AstraRP/data.db` — SQLite в WAL-режиме. Таблицы:

- `rp_status` — RP/NRP-статусы игроков
- `rp_names` — РП-имена и таймеры кулдауна
- `infinite_frames` + `infinite_frame_takes` — бесконечные рамки и история взятий
- `rp_characters` — NPC-персонажи

Все чтения/записи идут в выделенном потоке — основной тик сервера не блокируется.

## Примеры интеграций

> **Проверка плейсхолдеров.** `/astrarp debug <ник>` (право `astrarp.admin.reload`) распечатает значения `%astrarp_status%`, `%astrarp_status_raw%`, `%astrarp_rpname%`, `%astrarp_rpname_raw%`, какие интеграции найдены и парсятся ли плейсхолдеры через PlaceholderAPI. Полезно, если что-то рендерится сырым текстом.

### FlectonePulse — РП-имя в чате

FlectonePulse строит ник через шаблон `name.display` (модуль `name`). По умолчанию там стоит `<player>` — т. е. оригинальный никнейм. Чтобы он стал РП-именем, замените `<player>` на `%astrarp_rpname%` в локализации:

📂 `plugins/FlectonePulse/localizations/<lang>.yml → message.format.name_.display`

```yaml
message:
  format:
    name_:
      display: "<click:suggest_command:'/msg <player>'><hover:show_text:'<fcolor:2>Написать <player>'><vault_prefix><stream_prefix><fcolor:2>%astrarp_rpname%<afk_suffix><vault_suffix></hover></click>"
```

После сохранения выполните на сервере `/fp reload` (или `/fpulse reload`). Убедитесь, что в `plugins/FlectonePulse/integration.yml` включён PlaceholderAPI:

```yaml
placeholderapi:
  enable: true
```

Если хочется индикатор RP/NRP перед именем — допишите `%astrarp_status%` в начало того же шаблона.

### TAB — РП-имя в табе и над головой

AstraRP сам регистрирует свои плейсхолдеры в TAB через TAB API, поэтому PlaceholderAPI на стороне TAB не обязателен. Минимальный конфиг группы:

```yaml
groups:
  default:
    tabprefix: "%astrarp_status% "
    customtabname: "%astrarp_rpname%"
    tagprefix: "%astrarp_status% "
    customtagname: "%astrarp_rpname%"
```

`customtabname`/`customtagname` определяют, что увидит игрок в табе и над головой соответственно. Если AstraRP запущен без TAB, плейсхолдеры всё равно доступны через PlaceholderAPI (`%astrarp_rpname%` и т. д.).

### LuckPerms meta-bridge (опционально)

Если хочется собирать имя через мету LuckPerms (например, для совместимости со старыми шаблонами), включите `luckperms.write_meta: true` в `modules/names.yml` — AstraRP запишет ключ `astrarp_rpname` в LuckPerms-мету игрока, и его можно использовать как `%luckperms_meta_astrarp_rpname%`.

---

<a id="english"></a>

# AstraRP (English)

Modular RolePlay toolkit for **Paper 1.21.8**: status flags, custom display
names, smart KeepInventory in PvP, infinite item frames for quest items, and a
complete NPC voice system with a chest GUI.

## Features

| Module | Highlights |
| --- | --- |
| **status** | `/rp` / `/nrp` self-toggle, `/rp <player>` admin force, PAPI placeholders, optional LuckPerms meta bridge for FlectonePulse and TAB. |
| **names** | `/rpname set "First Last"` with cooldown, `/rpaname` admin override, command preprocessor that rewrites quoted RP-names so vanilla and other plugin commands resolve them. |
| **keepinventory** | PvP-only smart KeepInventory honoring victim/killer RP statuses; full configurable rule table; XP toggle; PvE deaths follow vanilla. |
| **frames** | Infinite item frames preserve quest items with full NBT; per-player take limit; admin commands `/ifr`, `/ifr del`, `/ifr reset local|global`, `/ifr check`. |
| **gm** | `/feel` for atmospheric text; `/rpc create` chest-GUI for NPC characters; `/rpc <id> "text" [radius] [player]` for global, local, or private NPC voices. |

## Build & install

```bash
mvn -B package
# → target/AstraRP-1.0.0.jar
```

Drop the jar into `plugins/` of a Paper 1.21.8 server. Default configs are
copied to `plugins/AstraRP/` on first start.

## Soft-dependencies

PlaceholderAPI · LuckPerms · FlectonePulse · TAB — all optional.

## License

MIT — see [LICENSE](LICENSE).
