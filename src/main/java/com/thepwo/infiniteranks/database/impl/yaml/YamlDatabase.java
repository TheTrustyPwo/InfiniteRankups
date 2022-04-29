package com.thepwo.infiniteranks.database.impl.yaml;

import com.thepwo.infiniteranks.InfiniteRanks;
import com.thepwo.infiniteranks.database.Database;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class YamlDatabase extends Database {
    private final String fileName;
    private File file;
    private YamlConfiguration data;

    public YamlDatabase(InfiniteRanks plugin) {
        super(plugin);
        this.fileName = this.plugin.getConfig().getString("database.yaml.file");
    }

    @Override
    public CompletableFuture<Boolean> connect() {
        return CompletableFuture.supplyAsync(() -> {
            this.file = new File(this.plugin.getDataFolder(), this.fileName);

            if (!file.exists()) {
                try {
                    file.createNewFile();
                } catch (IOException e) {
                    return false;
                }
            }

            this.data = YamlConfiguration.loadConfiguration(this.file);
            return true;
        });
    }

    @Override
    public void close() {
        save();
    }

    @Override
    public CompletableFuture<Long> getPlayerRank(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> this.data.getLong(uuid.toString(), 1L));
    }

    @Override
    public void setPlayerRank(UUID uuid, long amount) {
        CompletableFuture.runAsync(() -> {
            this.data.set(uuid.toString(), amount);
            save();
        });
    }

    private void save() {
        try {
            this.data.save(this.file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
