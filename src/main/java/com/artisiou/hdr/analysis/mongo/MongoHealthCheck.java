package com.artisiou.hdr.analysis.mongo;

import com.codahale.metrics.health.HealthCheck;
import com.mongodb.MongoClientException;

public class MongoHealthCheck extends HealthCheck {
    private final ManagedMongoClient mongoClient;

    public MongoHealthCheck(ManagedMongoClient mongoClient) {
        super();
        this.mongoClient = mongoClient;
    }

    @Override
    protected Result check() throws Exception {

        try {
            mongoClient.getDB("system").getStats();
        } catch (MongoClientException ex) {
            return Result.unhealthy(ex.getMessage());
        }

        return Result.healthy();
    }
}
