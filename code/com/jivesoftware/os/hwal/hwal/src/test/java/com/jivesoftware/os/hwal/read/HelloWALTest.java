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
package com.jivesoftware.os.hwal.read;

import com.jivesoftware.os.hwal.permit.ConstantPermitConfig;
import com.jivesoftware.os.hwal.permit.PermitProvider;
import com.jivesoftware.os.hwal.permit.PermitProviderImplInitializer;
import com.jivesoftware.os.hwal.read.rcvs.RCVSWALCursorStoreInitializer;
import com.jivesoftware.os.hwal.read.rcvs.RCVSWALTopicReaderInitializer;
import com.jivesoftware.os.hwal.read.topic.WALTopics;
import com.jivesoftware.os.hwal.read.topic.WALTopicsInitializer;
import com.jivesoftware.os.hwal.shared.api.SipWALEntry;
import com.jivesoftware.os.hwal.shared.api.SipWALTime;
import com.jivesoftware.os.hwal.shared.api.WALEntry;
import com.jivesoftware.os.hwal.shared.api.WALService;
import com.jivesoftware.os.hwal.shared.filter.IncludeAnyFilter;
import com.jivesoftware.os.hwal.shared.partition.RandomPartitioningStrategy;
import com.jivesoftware.os.hwal.shared.rcvs.RCVSWALStorage;
import com.jivesoftware.os.hwal.shared.rcvs.RCVSWALStorageInitializer;
import com.jivesoftware.os.hwal.write.WALWriter;
import com.jivesoftware.os.hwal.write.rcvs.RCVSWALWriterInitializer;
import com.jivesoftware.os.rcvs.api.RowColumnValueStore;
import com.jivesoftware.os.rcvs.api.SetOfSortedMapsImplInitializer;
import com.jivesoftware.os.rcvs.hbase.HBaseSetOfSortedMapsImplInitializer;
import com.jivesoftware.os.rcvs.inmemory.InMemorySetOfSortedMapsImplInitializer;
import com.jivesoftware.os.rcvs.inmemory.RowColumnValueStoreImpl;
import com.jivesoftware.os.rcvs.tests.EmbeddedHBase;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;
import org.merlin.config.BindInterfaceToConfiguration;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 *
 * @author jonathan
 */
public class HelloWALTest {

    RowColumnValueStore<String, Integer, Long, WALEntry, ? extends RuntimeException> wal;
    RowColumnValueStore<String, Integer, SipWALTime, SipWALEntry, ? extends RuntimeException> sipWAL;
    RowColumnValueStore<String, String, Integer, Long, ? extends RuntimeException> cursors;

    WALService<WALWriter> walWriterService;
    WALWriter walWriter;

    WALService<WALReaders> readersService;
    WALReaders readers;

    WALService<WALTopicReader> walTopicReaderService;
    WALTopicReader walTopicReader;
    WALTopicReader.WALTopicStream walTopicStream;

    List<WALEntry> streamed = new ArrayList<>();

    EmbeddedHBase embeddedHBase;

    @BeforeMethod
    public void setUpMethod() throws Exception {

        RCVSWALStorage storage;
        if (2 + 2 == 5) {
            embeddedHBase = new EmbeddedHBase();
            embeddedHBase.start(false);
            HBaseSetOfSortedMapsImplInitializer.HBaseSetOfSortedMapsConfig hbaseConfig = BindInterfaceToConfiguration.bindDefault(
                HBaseSetOfSortedMapsImplInitializer.HBaseSetOfSortedMapsConfig.class);
            hbaseConfig.setMarshalThreadPoolSize(4);
            SetOfSortedMapsImplInitializer sos = new HBaseSetOfSortedMapsImplInitializer(hbaseConfig, embeddedHBase.getConfiguration());
            RCVSWALStorageInitializer.RCVSWALStorageConfig storageConfig = BindInterfaceToConfiguration
                .bindDefault(RCVSWALStorageInitializer.RCVSWALStorageConfig.class);
            WALService<RCVSWALStorage> storageService = new RCVSWALStorageInitializer().initialize(storageConfig, sos);
            storageService.start();
            storage = storageService.getService();
        } else {
            wal = new RowColumnValueStoreImpl();
            sipWAL = new RowColumnValueStoreImpl();
            cursors = new RowColumnValueStoreImpl();
            storage = new RCVSWALStorage() {

                @Override
                public RowColumnValueStore<String, Integer, Long, WALEntry, ? extends RuntimeException> getWAL() {
                    return wal;
                }

                @Override
                public RowColumnValueStore<String, Integer, SipWALTime, SipWALEntry, ? extends RuntimeException> getSipWAL() {
                    return sipWAL;
                }

                @Override
                public RowColumnValueStore<String, String, Integer, Long, ? extends RuntimeException> getCursors() {
                    return cursors;
                }
            };
        }

        RCVSWALWriterInitializer.RCVSWALWriterConfig writerConfig = BindInterfaceToConfiguration
            .bindDefault(RCVSWALWriterInitializer.RCVSWALWriterConfig.class);
        writerConfig.setNumberOfPartitions(10);
        walWriterService = new RCVSWALWriterInitializer().initialize(writerConfig, storage, new RandomPartitioningStrategy(new Random(1234)));
        walWriterService.start();
        walWriter = walWriterService.getService();

        PermitProviderImplInitializer.PermitProviderConfig readerPermitsConfig = BindInterfaceToConfiguration
            .bindDefault(PermitProviderImplInitializer.PermitProviderConfig.class);
        PermitProvider readersPermitProvider = new PermitProviderImplInitializer()
            .initPermitProvider(readerPermitsConfig, new InMemorySetOfSortedMapsImplInitializer());

        WALReadersInitializer.WALReadersConfig readerConfig = BindInterfaceToConfiguration.bindDefault(WALReadersInitializer.WALReadersConfig.class);
        readersService = new WALReadersInitializer().initialize(readerConfig, readersPermitProvider);
        readersService.start();
        readers = readersService.getService();

        RCVSWALCursorStoreInitializer.RCVSWALCursorStoreConfig walCursorStorageConfig = BindInterfaceToConfiguration
            .bindDefault(RCVSWALCursorStoreInitializer.RCVSWALCursorStoreConfig.class);
        WALService<WALCursorStore> walCursorStorageService = new RCVSWALCursorStoreInitializer().initialize(walCursorStorageConfig, storage);
        walCursorStorageService.start();
        WALCursorStore walCursorStore = walCursorStorageService.getService();

        PermitProviderImplInitializer.PermitProviderConfig cursorPermitsConfig = BindInterfaceToConfiguration
            .bindDefault(PermitProviderImplInitializer.PermitProviderConfig.class);
        PermitProvider cursroPermitProvider = new PermitProviderImplInitializer()
            .initPermitProvider(cursorPermitsConfig, new InMemorySetOfSortedMapsImplInitializer());

        WALTopicsInitializer.WALTopicsConfig walTopicsConfig = BindInterfaceToConfiguration
            .bindDefault(WALTopicsInitializer.WALTopicsConfig.class);
        WALService<WALTopics> walTopcisService = new WALTopicsInitializer()
            .initialize(walTopicsConfig, readers, cursroPermitProvider, new ConstantPermitConfig(0, 10, 1000), walCursorStore);
        walTopcisService.start();
        WALTopics walTopics = walTopcisService.getService();

        walTopicStream = new WALTopicReader.WALTopicStream() {

            @Override
            public void stream(String topic, int partition, List<WALEntry> entries) {
                System.out.println(topic + "." + partition + "Streamed:" + entries);
                streamed.addAll(entries);
            }
        };

        RCVSWALTopicReaderInitializer.RCVSWALTopicReaderConfig topicReaderConfig = BindInterfaceToConfiguration
            .bindDefault(RCVSWALTopicReaderInitializer.RCVSWALTopicReaderConfig.class);
        topicReaderConfig.setTopicId("booya");
        walTopicReaderService = new RCVSWALTopicReaderInitializer().initialize(topicReaderConfig, storage, walTopics, new IncludeAnyFilter(), walTopicStream);
        walTopicReaderService.start();
        walTopicReader = walTopicReaderService.getService();

    }

    @AfterMethod
    public void tearDownMethod() throws Exception {
        walWriterService.stop();
        readersService.stop();
        walTopicReaderService.stop();
        if (embeddedHBase != null) {
            embeddedHBase.stop();
        }
    }

    @Test
    public void testWritesAndRead() throws Exception {
        for (int i = 0; i < 100; i++) {
            walWriter.write("booya", Arrays.asList(makeEntry(i)));
        }

        Thread.sleep(2000);

        for (int i = 50; i < 150; i++) {
            walWriter.write("booya", Arrays.asList(makeEntry(i)));
        }

        Thread.sleep(2000);

        Assert.assertEquals(150, streamed.size());

    }

    private WALEntry makeEntry(int id) {
        SipWALEntry sipWALEntry = new SipWALEntry(id, System.currentTimeMillis(), ("key-" + id).getBytes());
        return new WALEntry(sipWALEntry, ("payload-" + id).getBytes());
    }

}
