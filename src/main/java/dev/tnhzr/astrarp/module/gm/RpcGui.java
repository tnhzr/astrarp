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
import java.util.UUID;

public final class RpcGui {

    public static final String LIST_TITLE = "AstraRP::rpc_list";
    public static final String EDIT_TITLE = "AstraRP::rpc_edit";

    /** Compact 27-slot editor layout. Public so the listener can route clicks. */
    public static final int SLOT_ID = 1;
    public static final int SLOT_NAME = 2;
    public static final int SLOT_STYLE = 3;
    public static final int SLOT_ICON = 4;
    public static final int SLOT_RADIUS = 5;
    public static final int SLOT_PREVIEW = 7;

    public static final int SLOT_NAME_COLOR = 10;
    public static final int SLOT_NAME_FORMAT = 11;
    public static final int SLOT_NAME_RESET = 12;

    public static final int SLOT_STYLE_COLOR = 14;
    public static final int SLOT_STYLE_FORMAT = 15;
    public static final int SLOT_STYLE_RESET = 16;

    public static final int SLOT_CANCEL = 18;
    public static final int SLOT_SAVE = 22;
    public static final int SLOT_DELETE = 26;

    /** MiniMessage colour names cycled by the colour buttons. Order matches COLOR_WOOLS. */
    public static final String[] COLOR_NAMES = {
            "black", "dark_blue", "dark_green", "dark_aqua",
            "dark_red", "dark_purple", "gold", "gray",
            "dark_gray", "blue", "green", "aqua",
            "red", "light_purple", "yellow", "white"
    };
    public static final Material[] COLOR_WOOLS = {
            Material.BLACK_WOOL, Material.BLUE_WOOL, Material.GREEN_WOOL, Material.CYAN_WOOL,
            Material.RED_WOOL, Material.PURPLE_WOOL, Material.ORANGE_WOOL, Material.LIGHT_GRAY_WOOL,
            Material.GRAY_WOOL, Material.LIGHT_BLUE_WOOL, Material.LIME_WOOL, Material.HEART_OF_THE_SEA,
            Material.PINK_WOOL, Material.MAGENTA_WOOL, Material.YELLOW_WOOL, Material.WHITE_WOOL
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
                27, Text.parse(isNew
                        ? "<gradient:#ffb000:#ffe167><b>Создание персонажа</b></gradient>"
                        : "<gradient:#ffb000:#ffe167><b>Редактирование: " + draft.id() + "</b></gradient>"));

        // Row 1 ─ fields ──────────────────────────────────────────────────
        inv.setItem(SLOT_ID, simple(Material.NAME_TAG, "<#ffda4a>ID персонажа</#ffda4a>",
                List.of("<gray>Текущий: <white>" + draft.id() + "</white>",
                        "<dark_gray>Клик: ввести в чат")));

        inv.setItem(SLOT_NAME, simple(Material.PAPER, "<#ffda4a>Отображаемое имя</#ffda4a>",
                List.of("<gray>Текущее: <white>" + escape(draft.displayName()) + "</white>",
                        "<dark_gray>Клик: ввести в чат сырой MiniMessage",
                        "<dark_gray>(можно <#ffda4a>цвет, градиент, теги</#ffda4a>)")));

        inv.setItem(SLOT_STYLE, simple(Material.WRITABLE_BOOK, "<#ffda4a>Стиль текста реплик</#ffda4a>",
                List.of("<gray>Текущий: <white>" + escape(draft.style()) + "</white>",
                        "<dark_gray>Применяется к тексту /rpc <id> \"...\"",
                        "<dark_gray>Клик: ввести сырой MiniMessage")));

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

        inv.setItem(SLOT_PREVIEW, buildPreview(draft));

        // Row 2 ─ formatting cyclers ──────────────────────────────────────
        inv.setItem(SLOT_NAME_COLOR, colorCycler(draft.displayName(), "имя"));
        inv.setItem(SLOT_NAME_FORMAT, formatCycler(draft.displayName(), "имя"));
        inv.setItem(SLOT_NAME_RESET, simple(Material.BARRIER,
                "<red>Имя — сбросить оформление</red>",
                List.of("<dark_gray>Удаляет все MiniMessage-теги")));

        inv.setItem(SLOT_STYLE_COLOR, colorCycler(draft.style(), "стиль"));
        inv.setItem(SLOT_STYLE_FORMAT, formatCycler(draft.style(), "стиль"));
        inv.setItem(SLOT_STYLE_RESET, simple(Material.BARRIER,
                "<red>Стиль — сбросить оформление</red>",
                List.of("<dark_gray>Удаляет все MiniMessage-теги")));

        // Row 3 ─ actions ─────────────────────────────────────────────────
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

    private ItemStack colorCycler(String input, String scope) {
        int idx = StyleEdit.currentColorIndex(input, COLOR_NAMES);
        Material wool = idx >= 0 ? COLOR_WOOLS[idx] : Material.LIGHT_GRAY_DYE;
        String currentLabel = idx >= 0
                ? "<" + COLOR_NAMES[idx] + ">" + COLOR_NAMES[idx] + "</" + COLOR_NAMES[idx] + ">"
                : "<gray>—</gray>";
        return simple(wool, "<#ffda4a>Цвет — " + scope + "</#ffda4a>",
                List.of("<gray>Текущий: " + currentLabel,
                        "",
                        "<dark_gray>ЛКМ — следующий цвет",
                        "<dark_gray>ПКМ — предыдущий цвет",
                        "<dark_gray>Shift+ЛКМ — убрать цвет"));
    }

    private ItemStack formatCycler(String input, String scope) {
        boolean bold = StyleEdit.containsTag(input, "b");
        boolean italic = StyleEdit.containsTag(input, "i");
        Material mat = bold && italic ? Material.GOLDEN_APPLE
                : bold ? Material.GOLD_INGOT
                : italic ? Material.FEATHER
                : Material.BOOK;
        return simple(mat, "<#ffda4a>Оформление — " + scope + "</#ffda4a>",
                List.of("<gray>Жирный: " + (bold ? "<green>да" : "<red>нет"),
                        "<gray>Курсив: " + (italic ? "<green>да" : "<red>нет"),
                        "",
                        "<dark_gray>ЛКМ — переключить жирность",
                        "<dark_gray>ПКМ — переключить курсив"));
    }

    private ItemStack buildPreview(RpcCharacter draft) {
        ItemStack it = new ItemStack(Material.BOOK);
        ItemMeta m = it.getItemMeta();
        if (m == null) return it;
        m.displayName(Text.parse("<#ffda4a><b>Превью</b></#ffda4a>"));

        String text = plugin.configs().gm().getString("preview_text", "Привет, путник.");
        String dn = draft.displayName() == null ? "" : draft.displayName();
        String style = draft.style() == null ? "" : draft.style();
        Component nameLine = Text.parse(dn);
        Component styleLine = Text.parse(style + text);

        List<Component> lore = new ArrayList<>();
        lore.add(Component.empty());
        lore.add(Text.parse("<dark_gray>Имя:"));
        lore.add(nameLine);
        lore.add(Component.empty());
        lore.add(Text.parse("<dark_gray>Реплика:"));
        lore.add(styleLine);
        m.lore(lore);
        it.setItemMeta(m);
        return it;
    }

    private static String escape(String s) {
        if (s == null) return "—";
        // Display the raw string in lore without re-rendering MM. We use
        // MiniMessage's backslash escape (\<) — `&lt;` won't work because
        // Text.parse() runs legacy code conversion first and `&l` would be
        // promoted to <bold>, mangling the rest of the lore.
        return s.replace("<", "\\<");
    }

    public static boolean containsTag(String input, String tag) {
        return StyleEdit.containsTag(input, tag);
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
            String text = plugin.configs().gm().getString("preview_text", "Привет, путник.");
            String style = ch.style() == null ? "" : ch.style();
            lore.add(Text.parse(style + text));
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

    public RpcRepository repository() {
        return repository;
    }
}
