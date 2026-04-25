package dev.tnhzr.astrarp.module;

public interface AstraModule {

    String id();

    String defaultConfigName();

    void onEnable();

    void onDisable();
}
