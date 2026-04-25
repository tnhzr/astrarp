package dev.tnhzr.astrarp.config;

import dev.tnhzr.astrarp.AstraRP;
import dev.tnhzr.astrarp.util.Text;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;

public final class MessageManager {

    private final AstraRP plugin;
    private YamlConfiguration messages;
    private YamlConfiguration fallback;
    private String prefixRaw = "";

    public MessageManager(AstraRP plugin) {
        this.plugin = plugin;
    }

    public void reload() {
        String lang = plugin.configs().root().getString("language", "ru");

        messages = ensure("messages_" + lang + ".yml");
        if (messages == null || messages.getKeys(true).isEmpty()) {
            messages = ensure("messages_ru.yml");
        }
        fallback = ensure("messages_en.yml");
        prefixRaw = plugin.configs().root().getString("prefix", "<gradient:#9d4edd:#3a86ff>[AstraRP]</gradient> ");
    }

    private YamlConfiguration ensure(String resource) {
        File target = new File(plugin.getDataFolder(), resource);
        if (!target.exists()) {
            try (InputStream in = plugin.getResource(resource)) {
                if (in != null) Files.copy(in, target.toPath(), StandardCopyOption.REPLACE_EXISTING);
            } catch (IOException ignored) {}
        }
        YamlConfiguration cfg = new YamlConfiguration();
        if (target.exists()) {
            try { cfg.load(target); } catch (Exception ignored) {}
        }
        try (InputStream in = plugin.getResource(resource)) {
            if (in != null) {
                YamlConfiguration def = YamlConfiguration.loadConfiguration(
                        new InputStreamReader(in, StandardCharsets.UTF_8));
                cfg.setDefaults(def);
                cfg.options().copyDefaults(true);
                try { cfg.save(target); } catch (IOException ignored) {}
            }
        } catch (Exception ignored) {}
        return cfg;
    }

    public String raw(String key) {
        String v = messages != null ? messages.getString(key) : null;
        if (v == null && fallback != null) v = fallback.getString(key);
        return v == null ? key : v;
    }

    public Component get(String key) {
        return Text.parse(prefixRaw + raw(key));
    }

    public Component get(String key, Map<String, String> placeholders) {
        String v = raw(key);
        for (Map.Entry<String, String> e : placeholders.entrySet()) {
            v = v.replace("{" + e.getKey() + "}", e.getValue());
        }
        return Text.parse(prefixRaw + v);
    }

    public Component plain(String key) {
        return Text.parse(raw(key));
    }

    public Component plain(String key, Map<String, String> placeholders) {
        String v = raw(key);
        for (Map.Entry<String, String> e : placeholders.entrySet()) {
            v = v.replace("{" + e.getKey() + "}", e.getValue());
        }
        return Text.parse(v);
    }

    public void send(Audience to, String key) {
        to.sendMessage(get(key));
    }

    public void send(Audience to, String key, Map<String, String> placeholders) {
        to.sendMessage(get(key, placeholders));
    }
}
