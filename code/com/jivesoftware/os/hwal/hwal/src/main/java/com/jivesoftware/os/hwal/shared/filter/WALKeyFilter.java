package com.jivesoftware.os.hwal.shared.filter;

/**
 *
 * @author jonathan
 */
public interface WALKeyFilter {

    boolean include(byte[] key);
}
