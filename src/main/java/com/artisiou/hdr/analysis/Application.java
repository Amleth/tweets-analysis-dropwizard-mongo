package com.artisiou.hdr.analysis;

import com.artisiou.hdr.analysis.mongo.ManagedMongoClient;
import com.artisiou.hdr.analysis.mongo.MongoHealthCheck;
import com.artisiou.hdr.analysis.resources.TextMatcher;
import io.dropwizard.setup.Environment;

public class Application extends io.dropwizard.Application<Configuration> {
    public static void main(String[] args) throws Exception {
        new Application().run(args);
    }

    @Override
    public void run(Configuration configuration, Environment environment) throws Exception {
        final ManagedMongoClient mongoClient = configuration.getMongo().build();

        // Register Health Checks
        environment.healthChecks().register("mongo", new MongoHealthCheck(mongoClient));

        // Register Resources
        environment.jersey().register(new TextMatcher(mongoClient, configuration.getTextfilters(), configuration.getLinks()));
    }
}
