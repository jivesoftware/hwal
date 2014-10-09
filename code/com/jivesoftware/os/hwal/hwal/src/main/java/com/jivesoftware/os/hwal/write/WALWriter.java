package com.jivesoftware.os.hwal.write;

import com.jivesoftware.os.hwal.shared.api.WALEntry;
import java.util.Collection;

/**
 * @author jonathan
 */
public interface WALWriter {

    void write(String topicId, Collection<WALEntry> entries) throws Exception;

}
