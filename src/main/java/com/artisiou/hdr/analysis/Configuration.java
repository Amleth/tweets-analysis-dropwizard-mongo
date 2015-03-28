package com.artisiou.hdr.analysis;

import com.artisiou.hdr.analysis.corpus.TextFilter;
import com.artisiou.hdr.analysis.mongo.MongoClientFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

public class Configuration extends io.dropwizard.Configuration {
    @Valid
    @NotNull
    private MongoClientFactory mongo;

    @JsonProperty
    public MongoClientFactory getMongo() { return mongo; }

    @JsonProperty
    public void setMongo(MongoClientFactory value) {
        this.mongo = value;
    }

    @NotEmpty
    private List<TextFilter> textfilters;

    @JsonProperty
    public List<TextFilter> getTextfilters() { return textfilters; }

    @JsonProperty
    public void setTextfilters(List<TextFilter> value) { this.textfilters = value; }

    @NotEmpty
    private List<String> links;

    @JsonProperty
    public List<String> getLinks() { return links; }

    @JsonProperty
    public void setLinks(List<String> value) { this.links = value; }
}
