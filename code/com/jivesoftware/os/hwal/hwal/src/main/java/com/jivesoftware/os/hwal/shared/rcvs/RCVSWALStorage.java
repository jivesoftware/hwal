/*
 * Copyright 2014 Jive Software.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jivesoftware.os.hwal.shared.rcvs;

import com.jivesoftware.os.hwal.shared.api.SipWALEntry;
import com.jivesoftware.os.hwal.shared.api.SipWALTime;
import com.jivesoftware.os.hwal.shared.api.WALEntry;
import com.jivesoftware.os.rcvs.api.RowColumnValueStore;

/**
 *
 * @author jonathan
 */
public interface RCVSWALStorage {

    RowColumnValueStore<String, Integer, Long, WALEntry, ? extends RuntimeException> getWAL();

    RowColumnValueStore<String, Integer, SipWALTime, SipWALEntry, ? extends RuntimeException> getSipWAL();

    RowColumnValueStore<String, String, Integer, Long, ? extends RuntimeException> getCursors();
}
