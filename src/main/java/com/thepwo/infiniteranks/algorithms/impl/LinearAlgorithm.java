package com.thepwo.infiniteranks.algorithms.impl;

import com.thepwo.infiniteranks.InfiniteRanks;
import com.thepwo.infiniteranks.algorithms.Algorithm;

public class LinearAlgorithm implements Algorithm {
    private final double m;
    private final double c;

    public LinearAlgorithm(InfiniteRanks infiniteRanks) {
        this.m = infiniteRanks.getConfig().getDouble("algorithm.linear.increase-per-rank");
        this.c = infiniteRanks.getConfig().getDouble("algorithm.linear.base-cost");
    }

    @Override
    public long getRankupPrice(long rank) {
        return (long) Math.floor(m * rank + c);
    }

    @Override
    public long getTotalRankupPrice(long start, long end) {
        return ((getRankupPrice(start) + getRankupPrice(end - 1)) / 2) * (end - start);
    }

    @Override
    public long getMaxRankups(long start, long balance) {
        return (long) Math.floor(((-2 * getRankupPrice(start) + this.m +
                Math.sqrt(Math.pow((2 * getRankupPrice(start) - this.m), 2) + 8 * this.m * balance)) / (2 * this.m)));
    }
}
