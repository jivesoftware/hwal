package com.jivesoftware.os.hwal.shared.partition;

/**
 *
 * @author jonathan.colt
 */
public interface WALPartitioningStrategy {

    int partition(byte[] key, int numberOfPartitions);
}
