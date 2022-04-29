package com.thepwo.infiniteranks.algorithms;

public interface Algorithm {

    /**
     * Gets the next rankup price for the specified rank
     *
     * @param rank ~ Rank: If rank is 1, it will return the price to rankup to rank 2
     * @return The next rankup price
     */
    long getRankupPrice(long rank);

    /**
     * Gets the total rankup price for a range of ranks
     *
     * @param start ~ Start of the range of the rank
     * @param end ~ End of the range of the rank
     * @return Total rankup price for specified range
     */
    long getTotalRankupPrice(long start, long end);

    /**
     * Gets the maximum number of rankups from the player's balance
     *
     * @param start ~ The player's current rank
     * @param balance ~ The player's balance
     * @return The maximum number of rankups
     */
    long getMaxRankups(long start, long balance);
}
