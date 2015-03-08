package com.artisiou.hdr.analysis;

import com.artisiou.hdr.analysis.mongo.MongoClientFactory;
import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class Configuration extends io.dropwizard.Configuration {
    @Valid
    @NotNull
    private MongoClientFactory mongo;

    @JsonProperty
    public MongoClientFactory getMongo() {
        return mongo;
    }

    @JsonProperty
    public void setMongo(MongoClientFactory value) {
        this.mongo = value;
    }

}
