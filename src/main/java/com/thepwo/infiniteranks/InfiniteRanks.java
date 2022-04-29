package com.thepwo.infiniteranks;

import com.thepwo.infiniteranks.algorithms.Algorithm;
import com.thepwo.infiniteranks.algorithms.enums.RankupAlgorithm;
import com.thepwo.infiniteranks.algorithms.impl.ExponentialAlgorithm;
import com.thepwo.infiniteranks.algorithms.impl.LinearAlgorithm;
import com.thepwo.infiniteranks.commands.MaxRankupCommand;
import com.thepwo.infiniteranks.commands.RankupCommand;
import com.thepwo.infiniteranks.database.Database;
import com.thepwo.infiniteranks.database.DatabaseType;
import com.thepwo.infiniteranks.database.impl.mongodb.MongoDBDatabase;
import com.thepwo.infiniteranks.database.impl.yaml.YamlDatabase;
import com.thepwo.infiniteranks.utils.number.NumberUtils;
import com.thepwo.infiniteranks.utils.number.enums.NumberFormatType;
import com.thepwo.infiniteranks.utils.string.StringUtils;
import net.milkbowl.vault.economy.Economy;
import org.apache.commons.lang.math.LongRange;
import org.apache.commons.lang.math.Range;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class InfiniteRanks extends JavaPlugin {
    private Database database;
    private Economy economy;
    private Algorithm algorithm;
    private Map<Range, String> rankDisplays;
    private Map<Long, String> rankRewards;

    @Override
    public void onEnable() {
        log("&2[InfiniteRanks] &aInitializing");

        saveDefaultConfig();
        log("&2[InfiniteRanks] &aSaved default configuration");

        log("&2[InfiniteRanks] &aLoading database...");
        loadDatabase();

        log("&2[InfiniteRanks] &aSetting up economy...");
        setupEconomy();

        log("&2[InfiniteRanks] &aLoading rankup algorithm...");
        loadRankupAlgorithm();

        log("&2[InfiniteRanks] &aLoading rank displays...");
        loadRankDisplays();

        log("&2[InfiniteRanks] &aLoading rank rewards...");
        loadRankRewards();

        log("&2[InfiniteRanks] &aRegistering commands...");
        registerCommands();

        log("&2[InfiniteRanks] &aInitialized");
    }

    private void loadDatabase() {
        DatabaseType databaseType = DatabaseType.valueOf(getConfig().getString("database.type").toUpperCase());

        switch (databaseType) {
            case MONGODB -> this.database = new MongoDBDatabase(this);
            case YAML -> this.database = new YamlDatabase(this);
        }

        this.database.connect().whenComplete((success, throwable) -> {
            if (success) {
                log(String.format("&2[InfiniteRanks] &aSuccessfully established connection with %s database!", databaseType));
            } else {
                log("&c[InfiniteRanks] &4Could not connect to database! Disabling...");
                Bukkit.getPluginManager().disablePlugin(this);
            }
        });
    }

    private void setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            log("&c[InfiniteRanks] &4Vault not found! Disabling...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            log("&c[InfiniteRanks] &4No economy provider detected! Disabling...");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }

        this.economy = rsp.getProvider();
    }

    private void loadRankupAlgorithm() {
        RankupAlgorithm rankupAlgorithm = RankupAlgorithm.valueOf(getConfig().getString("algorithm.type"));
        switch (rankupAlgorithm) {
            case LINEAR -> this.algorithm = new LinearAlgorithm(this);
            case EXPONENTIAL -> this.algorithm = new ExponentialAlgorithm(this);
        }

        log(String.format("&2[InfiniteRanks] &aLoaded %s Algorithm", rankupAlgorithm));
    }

    private void loadRankDisplays() {
        this.rankDisplays = new LinkedHashMap<>();

        ConfigurationSection section = getConfig().getConfigurationSection("displays");
        for (String key : section.getKeys(false)) {
            this.rankDisplays.put(new LongRange(section.getLong(key + ".from"), section.getLong(key + ".to")),
                    section.getString(key + ".display"));
        }

        log(String.format("&2[InfiniteRanks] &aLoaded %d Rank Displays", this.rankDisplays.size()));
    }

    private void loadRankRewards() {
        this.rankRewards = new LinkedHashMap<>();

        ConfigurationSection section = getConfig().getConfigurationSection("rewards");
        for (String key : section.getKeys(false)) {
            if (!NumberUtils.isLong(key)) continue;
            this.rankRewards.put(Long.parseLong(key), section.getString(key));
        }

        log(String.format("&2[InfiniteRanks] &aLoaded %d Rank Rewards", this.rankRewards.size()));
    }

    private void registerCommands() {
        (new RankupCommand(this)).register();
        (new MaxRankupCommand(this)).register();

        log("&2[InfiniteRanks] &aRegistered Commands");
    }

    public void log(String message) {
        Bukkit.getConsoleSender().sendMessage(StringUtils.colorize(message));
    }

    @Override
    public void onDisable() {
        log("&c[InfiniteRanks] &4Terminating...");

        this.database.close();
        log("&c[InfiniteRanks] &4Closed database connection");

        log("&c[InfiniteRanks] &4Terminated");
    }

    public Database getDatabase() {
        return database;
    }

    public Economy getEconomy() {
        return economy;
    }

    public Algorithm getAlgorithm() {
        return algorithm;
    }

    public String getRankDisplay(long rank) {
        String rankDisplay = null;

        for (Range range : this.rankDisplays.keySet()) {
            if (range.containsLong(rank)) {
                rankDisplay = this.rankDisplays.get(range);
                break;
            }
        }

        if (rankDisplay == null) rankDisplay = getConfig().getString("displays.default.display");

        return rankDisplay.replace("%rank-raw%", String.valueOf(rank))
                .replace("%rank-comma%", NumberUtils.format(rank, NumberFormatType.COMMAS))
                .replace("%rank-letter%", NumberUtils.format(rank, NumberFormatType.LETTERS));
    }

    public List<String> getRankRewards(long from, long to) {
        List<String> commands = new ArrayList<>();

        for (Long rank : this.rankRewards.keySet()) {
            if (from <= rank && rank <= to) {
                commands.add(this.rankRewards.get(rank));
            }
        }

        return commands;
    }

    public String getMessage(String identifier) {
        return StringUtils.colorize(getConfig().getStringList("messages." + identifier));
    }
}
