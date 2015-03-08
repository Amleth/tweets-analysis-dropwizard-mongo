package com.artisiou.hdr.analysis.mongo;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.net.UnknownHostException;

public class MongoClientFactory {

    public ManagedMongoClient build() throws UnknownHostException {
        return new ManagedMongoClient(host, port, db, collection);
    }

    @NotNull
    private String host;

    @JsonProperty
    public String getHost() {
        return host;
    }

    @JsonProperty
    public void setHost(String value) {
        host = value;
    }

    @Min(1)
    @Max(65535)
    private int port;

    @JsonProperty
    public int getPort() {
        return port;
    }

    @JsonProperty
    public void setPort(int value) {
        port = value;
    }

    @NotNull
    private String db;

    @JsonProperty
    public String getDb() {
        return db;
    }

    @JsonProperty
    public void setDb(String value) {
        db = value;
    }

    @NotNull
    String collection;

    @JsonProperty
    public String getCollection() {
        return collection;
    }

    @JsonProperty
    public void setCollection(String value) {
        collection = value;
    }
}