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
                size, Text.parse("<dark_purple>AstraRP — РП-персонажи</dark_purple>"));

        for (RpcCharacter ch : characters) {
            inv.addItem(buildHead(ch));
        }
        player.openInventory(inv);
    }

    public void openEditor(Player player, RpcCharacter draft, boolean isNew) {
        Inventory inv = Bukkit.createInventory(new RpcGuiHolder(EDIT_TITLE),
                27, Text.parse(isNew
                        ? "<dark_aqua>Создание персонажа</dark_aqua>"
                        : "<dark_aqua>Редактирование: " + draft.id() + "</dark_aqua>"));

        inv.setItem(10, simple(Material.NAME_TAG, "<gold>ID персонажа</gold>",
                List.of("<gray>Текущий: <white>" + draft.id() + "</white>",
                        "<dark_gray>Клик: ввести в чат")));

        inv.setItem(11, simple(Material.PAPER, "<gold>Отображаемое имя</gold>",
                List.of("<gray>Текущее: <white>" + draft.displayName() + "</white>",
                        "<dark_gray>Клик: ввести в чат (MiniMessage / &-цвета)")));

        inv.setItem(12, simple(Material.WRITABLE_BOOK, "<gold>Стиль текста</gold>",
                List.of("<gray>Текущий: <white>" + draft.style() + "</white>",
                        "<dark_gray>Пример: <gray><i> или <green><b>")));

        ItemStack icon = buildIcon(draft);
        ItemMeta im = icon.getItemMeta();
        if (im != null) {
            im.displayName(Text.parse("<gold>Иконка</gold>"));
            List<Component> lore = new ArrayList<>();
            lore.add(Text.parse("<gray>Текущее: <white>" + (draft.icon() == null ? "—" : draft.icon()) + "</white>"));
            lore.add(Text.parse("<dark_gray>Клик: ник игрока или Base64"));
            im.lore(lore);
            icon.setItemMeta(im);
        }
        inv.setItem(13, icon);

        inv.setItem(14, simple(Material.SPYGLASS, "<gold>Радиус слышимости</gold>",
                List.of("<gray>Текущий: <white>" + draft.radius() + "</white>",
                        "<dark_gray>Клик: ввести число")));

        inv.setItem(15, simple(Material.BOOK, "<aqua>Превью</aqua>",
                List.of("<gray>" + previewLine(draft))));

        inv.setItem(22, simple(Material.LIME_CONCRETE, "<green>Сохранить</green>",
                List.of("<gray>Записывает персонажа.")));
        inv.setItem(18, simple(Material.RED_CONCRETE, "<red>Отмена</red>",
                List.of("<gray>Закрыть без сохранения.")));
        if (!isNew) {
            inv.setItem(26, simple(Material.BARRIER, "<dark_red>Удалить</dark_red>",
                    List.of("<gray>Двойной клик для подтверждения.")));
        }
        player.openInventory(inv);
    }

    public ItemStack buildHead(RpcCharacter ch) {
        ItemStack head = buildIcon(ch);
        ItemMeta meta = head.getItemMeta();
        if (meta != null) {
            meta.displayName(Text.parse(ch.displayName()));
            List<Component> lore = new ArrayList<>();
            lore.add(Text.parse("<dark_gray>id: <white>" + ch.id() + "</white>"));
            lore.add(Text.parse("<dark_gray>стиль: <white>" + ch.style() + "</white>"));
            lore.add(Text.parse("<dark_gray>радиус: <white>" + ch.radius() + "</white>"));
            lore.add(Component.empty());
            lore.add(Text.parse("<gray>" + previewLine(ch)));
            lore.add(Component.empty());
            lore.add(Text.parse("<yellow>ЛКМ — редактировать"));
            lore.add(Text.parse("<yellow>ПКМ — удалить (Shift+ПКМ для подтверждения)"));
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
        return Text.plain(Text.parse("[ " + ch.displayName() + " ] " + style + plugin.configs().gm()
                .getString("preview_text", "Привет, путник."), Map.of()));
    }

    public RpcRepository repository() {
        return repository;
    }
}
