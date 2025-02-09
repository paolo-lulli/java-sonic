/*
 * This Java source file was generated by the Gradle "init" task.
 */
package com.github.twohou.sonic;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;

public class IntegrationTest {
    @Test
    public void testIntegration() throws IOException {
        String address = "localhost";
        Integer port = 1491;
        String password = "SecretPassword";
        Integer connectionTimeout = 5000;
        Integer readTimeout = 5000;
        String collection = "movies";
        String bucket = "general";

        // init channels
        ChannelFactory factory = new ChannelFactory(address, port, password, connectionTimeout, readTimeout);
        IngestChannel ingest = factory.newIngestChannel();
        SearchChannel search = factory.newSearchChannel();
        ControlChannel control = factory.newControlChannel();

        // index
        ingest.ping();


        ingest.push(collection, bucket, "1", "MPs are starting to debate the process of voting on their preferred Brexit options, as Theresa May prepares to meet Tory backbenchers in an effort to win them over to her agreement.");
        ingest.push(collection, bucket, "2", "A shadowy group committed to ousting North Korea\"s leader Kim Jong-un has claimed it was behind a raid last month at the North Korean embassy in Spain.");
        ingest.push(collection, bucket, "3", "Meng Hongwei, the former Chinese head of Interpol, will be prosecuted in his home country for allegedly taking bribes, China\"s Communist Party says.");
        ingest.push(collection, bucket, "4", "A Chinese student who was violently kidnapped by a stun-gun toting gang of masked men in Canada has been found safe and well, police say.");

        // save to disk
        control.consolidate();

        Integer resp = ingest.count(collection);
        System.out.format("Count collection: %d\n", resp);
        resp = ingest.count(collection, bucket);
        System.out.format("Count bucket: %d\n", resp);
        resp = ingest.count(collection, bucket, "1");
        System.out.format("Count object: %d\n", resp);

        // search
        search.ping();

        ArrayList<String> responses = search.query(collection, bucket, "debate");
        assertEquals("1", responses.get(0));

        responses = search.query(collection, bucket, "Chinese");
        responses.sort(String::compareTo);
        assertEquals("3", responses.get(0));
        assertEquals("4", responses.get(1));

        responses = search.suggest(collection, bucket, "There");
        assertEquals("theresa", responses.get(0));

        responses = search.suggest(collection, bucket, "Hong");
        assertEquals("hongwei", responses.get(0));

        // cleanup
        ingest.flushc(collection);
        ingest.flushb(collection, bucket);
        ingest.flusho(collection, bucket, "1");
        resp = ingest.count(collection);
        assertEquals(Integer.valueOf(0), resp);
        resp = ingest.count(collection, bucket);
        assertEquals(Integer.valueOf(0), resp);
        resp = ingest.count(collection, bucket, "1");
        assertEquals(Integer.valueOf(0), resp);
        ingest.quit();
        search.quit();
        control.quit();

    }
}
