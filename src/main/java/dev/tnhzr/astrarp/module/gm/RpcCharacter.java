package dev.tnhzr.astrarp.module.gm;

public final class RpcCharacter {
    private final String id;
    private String displayName;
    private String style;
    private String icon;
    private int radius;

    public RpcCharacter(String id, String displayName, String style, String icon, int radius) {
        this.id = id;
        this.displayName = displayName == null ? id : displayName;
        this.style = style == null ? "<gray><i>" : style;
        this.icon = icon;
        this.radius = radius <= 0 ? 32 : radius;
    }

    public String id() { return id; }
    public String displayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String style() { return style; }
    public void setStyle(String style) { this.style = style; }
    public String icon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public int radius() { return radius; }
    public void setRadius(int radius) { this.radius = Math.max(1, radius); }
}
