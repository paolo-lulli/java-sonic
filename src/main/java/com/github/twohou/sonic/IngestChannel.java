package com.github.twohou.sonic;

import java.io.IOException;

public class IngestChannel extends Channel {
    public IngestChannel(String address, Integer port, String password,
                         Integer connectionTimeout, Integer readTimeout)
            throws IOException {
        super(address, port, password, connectionTimeout, readTimeout);
        this.start(Mode.ingest);
    }

    public void push(String collection, String bucket, String object, String text)
            throws IOException {
        this.send(String.format(
                "PUSH %s %s %s \"%s\"",
                collection,
                bucket,
                object,
                text
        ));
        this.assertOK();
    }

    public void pop(String collection, String bucket, String object, String text)
            throws IOException {
        this.send(String.format(
                "POP %s %s %s \"%s\"",
                collection,
                bucket,
                object,
                text
        ));
        this.assertOK();
    }

    public Integer count(String collection, String bucket, String object) throws IOException {
        if (bucket == null && object != null) {
            throw new IllegalArgumentException("bucket is required for counting an object");
        }

        this.send(String.format(
                "COUNT %s%s%s",
                collection,
                bucket == null ? "" : " " + bucket,
                object == null ? "" : " " + object
        ));
        return this.assertResult();
    }

    public Integer count(String collection, String bucket) throws IOException {
        return this.count(collection, bucket, null);
    }

    public Integer count(String collection) throws IOException {
        return this.count(collection, null);
    }

    public Integer flushc(String collection) throws IOException {
        this.send(String.format("FLUSHC %s", collection));
        return this.assertResult();
    }

    public Integer flushb(String collection, String bucket) throws IOException {
        this.send(String.format("FLUSHB %s %s", collection, bucket));
        return this.assertResult();
    }

    public Integer flusho(String collection, String bucket, String object)
            throws IOException {
        this.send(String.format("FLUSHO %s %s %s", collection, bucket, object));
        return this.assertResult();
    }
}
