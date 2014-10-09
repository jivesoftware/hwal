package com.jivesoftware.os.hwal.read.rcvs;

import com.jivesoftware.os.hwal.read.WALCursorStore;
import com.jivesoftware.os.rcvs.api.RowColumnValueStore;

/**
 *
 * @author jonathan
 */
public class RCVSWALCursorStore implements WALCursorStore {

    private final RowColumnValueStore<String, String, Integer, Long, ? extends RuntimeException> cursors;

    public RCVSWALCursorStore(RowColumnValueStore<String, String, Integer, Long, ? extends RuntimeException> cursors) {
        this.cursors = cursors;
    }

    @Override
    public void set(String readerGroup, String topic, int partition, long offset) {
        cursors.add(readerGroup, topic, partition, offset, null, null);
    }

    @Override
    public long get(String readerGroup, String topic, int partition) {
        Long got = cursors.get(readerGroup, topic, partition, null, null);
        if (got == null) {
            got = 0L;
        }
        return got;
    }
}
