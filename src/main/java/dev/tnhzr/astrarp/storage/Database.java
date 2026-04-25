package dev.tnhzr.astrarp.storage;

import dev.tnhzr.astrarp.AstraRP;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public final class Database {

    private final AstraRP plugin;
    private Connection connection;
    private ExecutorService executor;

    public Database(AstraRP plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        try {
            File dir = plugin.getDataFolder();
            if (!dir.exists()) dir.mkdirs();
            File db = new File(dir, "data.db");
            try {
                Class.forName("dev.tnhzr.astrarp.libs.sqlite.JDBC");
            } catch (ClassNotFoundException ex) {
                Class.forName("org.sqlite.JDBC");
            }
            connection = DriverManager.getConnection("jdbc:sqlite:" + db.getAbsolutePath());
            try (Statement st = connection.createStatement()) {
                st.execute("PRAGMA journal_mode=WAL;");
                st.execute("PRAGMA foreign_keys=ON;");
            }
            executor = Executors.newSingleThreadExecutor(r -> {
                Thread t = new Thread(r, "AstraRP-DB");
                t.setDaemon(true);
                return t;
            });

            try (Statement st = connection.createStatement()) {
                st.execute("""
                    CREATE TABLE IF NOT EXISTS rp_status (
                        uuid TEXT PRIMARY KEY,
                        status TEXT NOT NULL
                    )
                """);
                st.execute("""
                    CREATE TABLE IF NOT EXISTS rp_names (
                        uuid TEXT PRIMARY KEY,
                        name TEXT NOT NULL,
                        last_changed INTEGER NOT NULL
                    )
                """);
                st.execute("""
                    CREATE TABLE IF NOT EXISTS infinite_frames (
                        frame_uuid TEXT PRIMARY KEY,
                        world TEXT NOT NULL,
                        x INTEGER NOT NULL,
                        y INTEGER NOT NULL,
                        z INTEGER NOT NULL,
                        item_b64 TEXT NOT NULL,
                        created_at INTEGER NOT NULL
                    )
                """);
                st.execute("""
                    CREATE TABLE IF NOT EXISTS infinite_frame_takes (
                        frame_uuid TEXT NOT NULL,
                        player_uuid TEXT NOT NULL,
                        taken_at INTEGER NOT NULL,
                        PRIMARY KEY (frame_uuid, player_uuid)
                    )
                """);
                st.execute("""
                    CREATE TABLE IF NOT EXISTS rp_characters (
                        id TEXT PRIMARY KEY,
                        display_name TEXT NOT NULL,
                        style TEXT NOT NULL,
                        icon TEXT,
                        radius INTEGER NOT NULL DEFAULT 32,
                        created_at INTEGER NOT NULL
                    )
                """);
            }
        } catch (Exception ex) {
            plugin.getLogger().severe("Failed to connect to SQLite: " + ex.getMessage());
            ex.printStackTrace();
        }
    }

    public Connection connection() {
        return connection;
    }

    public void async(Runnable runnable) {
        if (executor == null) {
            runnable.run();
            return;
        }
        executor.submit(() -> {
            try { runnable.run(); }
            catch (Throwable t) { plugin.getLogger().warning("DB async error: " + t.getMessage()); }
        });
    }

    public <T> CompletableFuture<T> asyncSupply(java.util.function.Supplier<T> supplier) {
        if (executor == null) return CompletableFuture.completedFuture(supplier.get());
        return CompletableFuture.supplyAsync(supplier, executor);
    }

    public <T> void asyncThen(java.util.function.Supplier<T> supplier, Consumer<T> sync) {
        asyncSupply(supplier).thenAccept(value ->
            plugin.getServer().getScheduler().runTask(plugin, () -> sync.accept(value)));
    }

    public void close() {
        if (executor != null) {
            executor.shutdown();
        }
        if (connection != null) {
            try { connection.close(); } catch (SQLException ignored) {}
        }
    }
}
