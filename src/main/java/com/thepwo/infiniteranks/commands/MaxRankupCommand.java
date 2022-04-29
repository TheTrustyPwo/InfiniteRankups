package com.thepwo.infiniteranks.commands;

import com.thepwo.infiniteranks.InfiniteRanks;
import com.thepwo.infiniteranks.utils.number.NumberUtils;
import com.thepwo.infiniteranks.utils.number.enums.NumberFormatType;
import org.bukkit.Bukkit;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class MaxRankupCommand implements CommandExecutor, TabCompleter {
    private final InfiniteRanks plugin;

    public MaxRankupCommand(InfiniteRanks plugin) {
        this.plugin = plugin;
    }

    public void register() {
        PluginCommand command = this.plugin.getCommand("maxrankup");
        command.setExecutor(this);
        command.setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(this.plugin.getMessage("invalid-executor"));
            return false;
        }

        if (args.length != 0) {
            player.sendMessage(this.plugin.getMessage("invalid-usage")
                    .replace("%usage%", "/maxrankup"));
            return false;
        }

        this.plugin.getDatabase().getPlayerRank(player.getUniqueId()).whenComplete((rank, throwable) -> {
            long amount = this.plugin.getAlgorithm().getMaxRankups(rank, (long) this.plugin.getEconomy().getBalance(player));
            long cost = this.plugin.getAlgorithm().getTotalRankupPrice(rank, rank + amount);

            if (amount <= 0) {
                player.sendMessage(this.plugin.getMessage("not-enough-money")
                        .replace("%amount%", NumberUtils.format(this.plugin.getAlgorithm().getRankupPrice(rank),
                                NumberFormatType.COMMAS)));
                return;
            }

            this.plugin.getEconomy().withdrawPlayer(player, cost);
            this.plugin.getDatabase().setPlayerRank(player.getUniqueId(), rank + amount);

            player.sendMessage(this.plugin.getMessage("maxrankup")
                    .replace("%rank-before%", this.plugin.getRankDisplay(rank))
                    .replace("%rank-after%", this.plugin.getRankDisplay(rank + amount)));

            this.plugin.getRankRewards(rank, rank + amount)
                    .forEach(reward -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(),
                            reward.replace("%player%", player.getName())));
        });

        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return null;
    }
}