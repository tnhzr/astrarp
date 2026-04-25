package dev.tnhzr.astrarp.integration;

import dev.tnhzr.astrarp.AstraRP;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.node.NodeType;
import net.luckperms.api.node.types.MetaNode;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public final class LuckPermsBridge {

    private final AstraRP plugin;
    private final LuckPerms api;

    public LuckPermsBridge(AstraRP plugin) {
        this.plugin = plugin;
        this.api = LuckPermsProvider.get();
    }

    public CompletableFuture<Void> setMeta(UUID uuid, String key, String value) {
        return api.getUserManager().loadUser(uuid).thenAcceptAsync(user -> {
            user.data().clear(NodeType.META.predicate(node -> key.equals(node.getMetaKey())));
            user.data().add(MetaNode.builder(key, value).build());
            api.getUserManager().saveUser(user);
        });
    }

    public CompletableFuture<Void> clearMeta(UUID uuid, String key) {
        return api.getUserManager().loadUser(uuid).thenAcceptAsync(user -> {
            user.data().clear(NodeType.META.predicate(node -> key.equals(node.getMetaKey())));
            api.getUserManager().saveUser(user);
        });
    }

    public String prefix(User user) {
        return user.getCachedData().getMetaData().getPrefix();
    }

    public String suffix(User user) {
        return user.getCachedData().getMetaData().getSuffix();
    }

    public LuckPerms api() {
        return api;
    }

    public AstraRP plugin() {
        return plugin;
    }
}
