package com.jivesoftware.os.hwal.read;

import com.jivesoftware.os.hwal.read.partitions.TopicLag;
import com.jivesoftware.os.hwal.shared.api.WALEntry;
import com.jivesoftware.os.hwal.shared.filter.WALKeyFilter;
import java.util.List;

/**
 * @author jonathan
 */
public interface WALTopicReader {

    interface WALTopicStream {

        /**
         *
         * @param topic
         * @param partition
         * @param entries
         */
        void stream(String topic, int partition, List<WALEntry> entries);
    }

    void stream(WALKeyFilter filter, int batchSize, WALTopicStream stream) throws Exception;

    List<TopicLag> getTopicLags() throws Exception;

}
