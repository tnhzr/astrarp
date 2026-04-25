package dev.tnhzr.astrarp.module.gm;

import dev.tnhzr.astrarp.AstraRP;
import dev.tnhzr.astrarp.util.Text;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.profile.PlayerProfile;
import org.bukkit.profile.PlayerTextures;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class RpcGui {

    public static final String LIST_TITLE = "AstraRP::rpc_list";
    public static final String EDIT_TITLE = "AstraRP::rpc_edit";

    /** Slot map for the editor inventory. Public so listener stays in sync. */
    public static final int SLOT_ID = 1;
    public static final int SLOT_NAME = 2;
    public static final int SLOT_STYLE = 3;
    public static final int SLOT_ICON = 4;
    public static final int SLOT_RADIUS = 5;
    public static final int SLOT_PREVIEW = 7;

    public static final int SLOT_NAME_RESET = 26;
    public static final int SLOT_STYLE_RESET = 44;

    public static final int SLOT_NAME_BOLD = 24;
    public static final int SLOT_NAME_ITALIC = 25;
    public static final int SLOT_STYLE_BOLD = 42;
    public static final int SLOT_STYLE_ITALIC = 43;

    public static final int SLOT_CANCEL = 45;
    public static final int SLOT_SAVE = 49;
    public static final int SLOT_DELETE = 53;

    /** Wool slots and their MiniMessage colour names for the NAME field. */
    public static final int[] NAME_COLOR_SLOTS = {
            9, 10, 11, 12, 13, 14, 15, 16, 17, // row 1: 9 colours
            18, 19, 20, 21, 22, 23             // row 2: 6 colours + 2 toggle slots
    };
    public static final int[] STYLE_COLOR_SLOTS = {
            27, 28, 29, 30, 31, 32, 33, 34, 35,
            36, 37, 38, 39, 40, 41
    };
    public static final String[] COLOR_NAMES = {
            "black", "dark_blue", "dark_green", "dark_aqua",
            "dark_red", "dark_purple", "gold", "gray",
            "dark_gray", "blue", "green", "aqua",
            "red", "light_purple", "yellow"
    };
    public static final Material[] COLOR_WOOLS = {
            Material.BLACK_WOOL, Material.BLUE_WOOL, Material.GREEN_WOOL, Material.CYAN_WOOL,
            Material.RED_WOOL, Material.PURPLE_WOOL, Material.ORANGE_WOOL, Material.LIGHT_GRAY_WOOL,
            Material.GRAY_WOOL, Material.LIGHT_BLUE_WOOL, Material.LIME_WOOL, Material.LIGHT_BLUE_WOOL,
            Material.PINK_WOOL, Material.MAGENTA_WOOL, Material.YELLOW_WOOL
    };

    private final AstraRP plugin;
    private final RpcRepository repository;

    public RpcGui(AstraRP plugin, RpcRepository repository) {
        this.plugin = plugin;
        this.repository = repository;
    }

    public void openList(Player player) {
        var characters = new ArrayList<>(repository.all());
        int rows = Math.max(1, (int) Math.ceil(characters.size() / 9.0));
        int size = Math.min(54, Math.max(9, rows * 9));
        Inventory inv = Bukkit.createInventory(new RpcGuiHolder(LIST_TITLE),
                size, Text.parse("<gradient:#ffb000:#ffe167><b>AstraRP — РП-персонажи</b></gradient>"));

        for (RpcCharacter ch : characters) {
            inv.addItem(buildHead(ch));
        }
        player.openInventory(inv);
    }

    public void openEditor(Player player, RpcCharacter draft, boolean isNew) {
        Inventory inv = Bukkit.createInventory(new RpcGuiHolder(EDIT_TITLE),
                54, Text.parse(isNew
                        ? "<gradient:#ffb000:#ffe167><b>Создание персонажа</b></gradient>"
                        : "<gradient:#ffb000:#ffe167><b>Редактирование: " + draft.id() + "</b></gradient>"));

        // ── Top row: main fields ───────────────────────────────────────────
        inv.setItem(SLOT_ID, simple(Material.NAME_TAG, "<#ffda4a>ID персонажа</#ffda4a>",
                List.of("<gray>Текущий: <white>" + draft.id() + "</white>",
                        "<dark_gray>Клик: ввести в чат")));

        inv.setItem(SLOT_NAME, simple(Material.PAPER, "<#ffda4a>Отображаемое имя</#ffda4a>",
                List.of("<gray>Текущее: <white>" + escape(draft.displayName()) + "</white>",
                        "<dark_gray>Клик: ввести в чат (MiniMessage / &-цвета)",
                        "<dark_gray>Скобки и оформление — ниже кнопками.")));

        inv.setItem(SLOT_STYLE, simple(Material.WRITABLE_BOOK, "<#ffda4a>Стиль текста реплик</#ffda4a>",
                List.of("<gray>Текущий: <white>" + escape(draft.style()) + "</white>",
                        "<dark_gray>Применяется к тексту /rpc <id> \"...\"")));

        ItemStack icon = buildIcon(draft);
        ItemMeta im = icon.getItemMeta();
        if (im != null) {
            im.displayName(Text.parse("<#ffda4a>Иконка</#ffda4a>"));
            List<Component> lore = new ArrayList<>();
            lore.add(Text.parse("<gray>Текущее: <white>" + (draft.icon() == null ? "—" : draft.icon()) + "</white>"));
            lore.add(Text.parse("<dark_gray>Клик: ник игрока или Base64"));
            im.lore(lore);
            icon.setItemMeta(im);
        }
        inv.setItem(SLOT_ICON, icon);

        inv.setItem(SLOT_RADIUS, simple(Material.SPYGLASS, "<#ffda4a>Радиус слышимости</#ffda4a>",
                List.of("<gray>Текущий: <white>" + draft.radius() + "</white>",
                        "<dark_gray>Клик: ввести число")));

        inv.setItem(SLOT_PREVIEW, simple(Material.BOOK, "<aqua>Превью</aqua>",
                List.of("<gray>" + previewLine(draft))));

        // ── NAME colour palette + B/I + reset ──────────────────────────────
        for (int i = 0; i < COLOR_NAMES.length; i++) {
            int slot = NAME_COLOR_SLOTS[i];
            inv.setItem(slot, colorButton(COLOR_NAMES[i], COLOR_WOOLS[i], "имя"));
        }
        inv.setItem(SLOT_NAME_BOLD, simple(Material.GOLDEN_APPLE,
                "<#ffda4a>Имя — <b>Жирный</b></#ffda4a>",
                List.of("<dark_gray>Включает/выключает <b>",
                        "<dark_gray>Текущее: " + (containsTag(draft.displayName(), "b") ? "<green>да" : "<red>нет"))));
        inv.setItem(SLOT_NAME_ITALIC, simple(Material.FEATHER,
                "<#ffda4a>Имя — <i>Курсив</i></#ffda4a>",
                List.of("<dark_gray>Включает/выключает <i>",
                        "<dark_gray>Текущее: " + (containsTag(draft.displayName(), "i") ? "<green>да" : "<red>нет"))));
        inv.setItem(SLOT_NAME_RESET, simple(Material.BARRIER,
                "<red>Имя — сбросить оформление</red>",
                List.of("<dark_gray>Удаляет все MiniMessage-теги")));

        // ── STYLE colour palette + B/I + reset ─────────────────────────────
        for (int i = 0; i < COLOR_NAMES.length; i++) {
            int slot = STYLE_COLOR_SLOTS[i];
            inv.setItem(slot, colorButton(COLOR_NAMES[i], COLOR_WOOLS[i], "стиль"));
        }
        inv.setItem(SLOT_STYLE_BOLD, simple(Material.GOLDEN_APPLE,
                "<#ffda4a>Стиль — <b>Жирный</b></#ffda4a>",
                List.of("<dark_gray>Включает/выключает <b>",
                        "<dark_gray>Текущее: " + (containsTag(draft.style(), "b") ? "<green>да" : "<red>нет"))));
        inv.setItem(SLOT_STYLE_ITALIC, simple(Material.FEATHER,
                "<#ffda4a>Стиль — <i>Курсив</i></#ffda4a>",
                List.of("<dark_gray>Включает/выключает <i>",
                        "<dark_gray>Текущее: " + (containsTag(draft.style(), "i") ? "<green>да" : "<red>нет"))));
        inv.setItem(SLOT_STYLE_RESET, simple(Material.BARRIER,
                "<red>Стиль — сбросить оформление</red>",
                List.of("<dark_gray>Удаляет все MiniMessage-теги")));

        // ── Bottom action row ──────────────────────────────────────────────
        inv.setItem(SLOT_CANCEL, simple(Material.RED_CONCRETE, "<red>Отмена</red>",
                List.of("<gray>Закрыть без сохранения.")));
        inv.setItem(SLOT_SAVE, simple(Material.LIME_CONCRETE, "<green>Сохранить</green>",
                List.of("<gray>Записывает персонажа.")));
        if (!isNew) {
            inv.setItem(SLOT_DELETE, simple(Material.BARRIER, "<dark_red>Удалить</dark_red>",
                    List.of("<gray>Двойной клик для подтверждения.")));
        }
        player.openInventory(inv);
    }

    private ItemStack colorButton(String color, Material wool, String scope) {
        return simple(wool, "<" + color + ">" + color + "</" + color + ">",
                List.of("<gray>Применить цвет к " + scope,
                        "<dark_gray>Цвет ставится в начало строки"));
    }

    private static String escape(String s) {
        if (s == null) return "—";
        // Display the raw string in lore without re-rendering MM
        return s.replace("<", "&lt;");
    }

    public static boolean containsTag(String input, String tag) {
        if (input == null) return false;
        String low = input.toLowerCase();
        return low.contains("<" + tag + ">")
                || low.contains("<" + tag.toLowerCase() + ">")
                || (tag.equals("b") && low.contains("<bold>"))
                || (tag.equals("i") && low.contains("<italic>"));
    }

    public ItemStack buildHead(RpcCharacter ch) {
        ItemStack head = buildIcon(ch);
        ItemMeta meta = head.getItemMeta();
        if (meta != null) {
            meta.displayName(Text.parse(ch.displayName()));
            List<Component> lore = new ArrayList<>();
            lore.add(Text.parse("<dark_gray>id: <white>" + ch.id() + "</white>"));
            lore.add(Text.parse("<dark_gray>стиль: <white>" + escape(ch.style()) + "</white>"));
            lore.add(Text.parse("<dark_gray>радиус: <white>" + ch.radius() + "</white>"));
            lore.add(Component.empty());
            lore.add(Text.parse("<gray>" + previewLine(ch)));
            lore.add(Component.empty());
            lore.add(Text.parse("<#ffda4a>ЛКМ — редактировать"));
            lore.add(Text.parse("<#ffda4a>ПКМ — удалить (Shift+ПКМ для подтверждения)"));
            meta.lore(lore);
            head.setItemMeta(meta);
        }
        return head;
    }

    public ItemStack buildIcon(RpcCharacter ch) {
        ItemStack head = new ItemStack(Material.PLAYER_HEAD);
        if (ch.icon() == null || ch.icon().isBlank()) return head;
        SkullMeta meta = (SkullMeta) head.getItemMeta();
        if (meta == null) return head;

        String icon = ch.icon().trim();
        if (icon.length() <= 16 && icon.matches("[A-Za-z0-9_]+")) {
            OfflinePlayer op = Bukkit.getOfflinePlayer(icon);
            meta.setOwningPlayer(op);
        } else {
            try {
                byte[] decoded = Base64.getDecoder().decode(icon);
                String json = new String(decoded);
                int urlStart = json.indexOf("http");
                if (urlStart >= 0) {
                    int urlEnd = json.indexOf('\"', urlStart);
                    if (urlEnd > urlStart) {
                        String urlStr = json.substring(urlStart, urlEnd);
                        PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), null);
                        PlayerTextures tex = profile.getTextures();
                        try {
                            URL url = URI.create(urlStr).toURL();
                            tex.setSkin(url);
                            profile.setTextures(tex);
                            meta.setOwnerProfile(profile);
                        } catch (MalformedURLException ignored) {}
                    }
                }
            } catch (Exception ignored) {}
        }
        head.setItemMeta(meta);
        return head;
    }

    private ItemStack simple(Material mat, String name, List<String> lore) {
        ItemStack it = new ItemStack(mat);
        ItemMeta m = it.getItemMeta();
        if (m != null) {
            m.displayName(Text.parse(name));
            List<Component> l = new ArrayList<>();
            for (String s : lore) l.add(Text.parse(s));
            m.lore(l);
            it.setItemMeta(m);
        }
        return it;
    }

    private String previewLine(RpcCharacter ch) {
        String style = ch.style() == null ? "" : ch.style();
        return Text.plain(Text.parse(ch.displayName() + " " + style + plugin.configs().gm()
                .getString("preview_text", "Привет, путник."), Map.of()));
    }

    public RpcRepository repository() {
        return repository;
    }
}
