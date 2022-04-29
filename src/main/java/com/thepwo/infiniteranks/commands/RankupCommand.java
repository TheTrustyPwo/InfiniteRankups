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

public class RankupCommand implements CommandExecutor, TabCompleter {
    private final InfiniteRanks plugin;

    public RankupCommand(InfiniteRanks plugin) {
        this.plugin = plugin;
    }

    public void register() {
        PluginCommand command = this.plugin.getCommand("rankup");
        command.setExecutor(this);
        command.setTabCompleter(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(this.plugin.getMessage("invalid-executor"));
            return false;
        }

        if (args.length > 1) {
            player.sendMessage(this.plugin.getMessage("invalid-usage")
                    .replace("%usage%", "/rankup [amount]"));
            return false;
        }

        if (args.length == 1 && !NumberUtils.isLong(args[0])) {
            player.sendMessage(this.plugin.getMessage("invalid-number")
                    .replace("%number%", args[0]));
            return false;
        }

        long amount = args.length == 1 ? Long.parseLong(args[0]) : 1;

        this.plugin.getDatabase().getPlayerRank(player.getUniqueId()).whenComplete((rank, throwable) -> {
            long cost = this.plugin.getAlgorithm().getTotalRankupPrice(rank, rank + amount);

            if (!this.plugin.getEconomy().has(player, cost)) {
                player.sendMessage(this.plugin.getMessage("not-enough-money")
                        .replace("%amount%", NumberUtils.format(cost, NumberFormatType.COMMAS)));
                return;
            }

            this.plugin.getEconomy().withdrawPlayer(player, cost);
            this.plugin.getDatabase().setPlayerRank(player.getUniqueId(), rank + amount);

            player.sendMessage(this.plugin.getMessage("rankup")
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
        if (args.length == 1) return List.of("1", "5", "10", "50");
        return null;
    }
}
