package com.jivesoftware.os.hwal.shared.filter;

/**
 *
 * @author jonathan
 */
public class IncludeAnyFilter implements WALKeyFilter {

    @Override
    public boolean include(byte[] key) {
        return true;
    }

}
