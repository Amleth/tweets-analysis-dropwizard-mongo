package com.artisiou.hdr.analysis.mongo;

import com.mongodb.DBCollection;
import com.mongodb.MongoClient;
import io.dropwizard.lifecycle.Managed;

import java.net.UnknownHostException;

public class ManagedMongoClient extends MongoClient implements Managed {
    private DBCollection collection;

    public DBCollection getCollection() {
        return collection;
    }

    public ManagedMongoClient(final String host, final int port, final String db, final String collection) throws UnknownHostException {
        super(host, port);

        this.collection = getDB(db).getCollection(collection);
    }

    @Override
    public void start() throws Exception {
    }

    @Override
    public void stop() throws Exception {
        close();
    }
}
