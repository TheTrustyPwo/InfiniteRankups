package com.thepwo.infiniteranks.database;

import com.thepwo.infiniteranks.InfiniteRanks;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public abstract class Database {
    protected InfiniteRanks plugin;

    public Database(InfiniteRanks plugin) {
        this.plugin = plugin;
    }

    public abstract CompletableFuture<Boolean> connect();

    public abstract void close();

    public abstract CompletableFuture<Long> getPlayerRank(UUID uuid);

    public abstract void setPlayerRank(UUID uuid, long amount);
}
