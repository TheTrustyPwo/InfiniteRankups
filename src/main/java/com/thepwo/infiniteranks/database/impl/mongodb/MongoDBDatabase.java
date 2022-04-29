package com.thepwo.infiniteranks.database.impl.mongodb;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.thepwo.infiniteranks.InfiniteRanks;
import com.thepwo.infiniteranks.database.Database;
import org.bson.Document;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class MongoDBDatabase extends Database {
    private MongoClient mongoClient;
    private MongoCollection<Document> collection;

    public MongoDBDatabase(InfiniteRanks plugin) {
        super(plugin);
    }

    @Override
    public CompletableFuture<Boolean> connect() {
        return CompletableFuture.supplyAsync(() -> {
            String connectionString = this.plugin.getConfig().getString("database.mongodb.mongo-connection-string");
            String databaseName = this.plugin.getConfig().getString("database.mongodb.mongo-database-name");

            this.mongoClient = MongoClients.create(connectionString);
            MongoDatabase mongoDatabase = this.mongoClient.getDatabase(databaseName);
            this.collection = mongoDatabase.getCollection("ranks");
            return true;
        });
    }

    @Override
    public void close() {
        this.mongoClient.close();
    }

    @Override
    public CompletableFuture<Long> getPlayerRank(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            Document document = this.collection.find(Filters.eq("_id", uuid.toString())).first();
            if (document == null) return 1L;
            return document.getLong("rank");
        });
    }

    @Override
    public void setPlayerRank(UUID uuid, long amount) {
        CompletableFuture.runAsync(() -> {
            Document document = new Document()
                    .append("_id", uuid.toString())
                    .append("rank", amount);
            this.collection.insertOne(document);
        });
    }
}
