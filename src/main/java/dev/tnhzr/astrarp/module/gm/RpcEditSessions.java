package dev.tnhzr.astrarp.module.gm;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class RpcEditSessions {

    public enum Field { ID, DISPLAY_NAME, STYLE, ICON, RADIUS }

    public static final class Pending {
        public final String draftId;
        public final boolean isNew;
        public final Field field;

        public Pending(String draftId, boolean isNew, Field field) {
            this.draftId = draftId;
            this.isNew = isNew;
            this.field = field;
        }
    }

    private final ConcurrentHashMap<UUID, Pending> awaitingChat = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<UUID, RpcCharacter> drafts = new ConcurrentHashMap<>();

    public Pending awaiting(UUID id) { return awaitingChat.get(id); }

    public void setAwaiting(UUID id, Pending p) {
        if (p == null) awaitingChat.remove(id); else awaitingChat.put(id, p);
    }

    public RpcCharacter draft(UUID id) { return drafts.get(id); }
    public void setDraft(UUID id, RpcCharacter ch) {
        if (ch == null) drafts.remove(id); else drafts.put(id, ch);
    }
    public void clear(UUID id) { drafts.remove(id); awaitingChat.remove(id); }
}
