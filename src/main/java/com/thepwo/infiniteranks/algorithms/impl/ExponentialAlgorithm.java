package com.thepwo.infiniteranks.algorithms.impl;

import com.thepwo.infiniteranks.InfiniteRanks;
import com.thepwo.infiniteranks.algorithms.Algorithm;

public class ExponentialAlgorithm implements Algorithm {
    private final double a;
    private final double b;

    public ExponentialAlgorithm(InfiniteRanks infiniteRanks) {
        this.a = infiniteRanks.getConfig().getDouble("algorithm.exponential.coefficient");
        this.b = infiniteRanks.getConfig().getDouble("algorithm.exponential.base");
    }

    @Override
    public long getRankupPrice(long rank) {
        return (long) Math.floor(a * Math.pow(this.b, rank));
    }

    @Override
    public long getTotalRankupPrice(long start, long end) {
        return (long) Math.floor(this.a * (Math.pow(this.b, end) - Math.pow(this.b, start)) / (this.b - 1));
    }

    @Override
    public long getMaxRankups(long start, long balance) {
        return (long) Math.floor(Math.log(((this.b * balance) / this.a) -
                (balance / this.a) + Math.pow(this.b, start)) / Math.log(this.b));
    }
}
