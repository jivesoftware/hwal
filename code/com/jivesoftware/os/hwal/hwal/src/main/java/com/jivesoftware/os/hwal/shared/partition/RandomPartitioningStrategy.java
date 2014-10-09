package com.jivesoftware.os.hwal.shared.partition;

import java.util.Random;

/**
 *
 * @author jonathan
 */
public class RandomPartitioningStrategy implements WALPartitioningStrategy {

    private final Random random;

    public RandomPartitioningStrategy(Random random) {
        this.random = random;
    }

    @Override
    public int partition(byte[] key, int numberOfPartitions) {
        return random.nextInt(numberOfPartitions);
    }

}
