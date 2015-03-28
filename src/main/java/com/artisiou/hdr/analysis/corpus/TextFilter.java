package com.artisiou.hdr.analysis.corpus;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.util.List;

public class TextFilter {
    @NotNull
    private String name;

    @JsonProperty
    public String getName() {
        return name;
    }

    @JsonProperty
    public void setName(String value) {
        name = value;
    }

    @NotNull
    private List<String> regex;

    @JsonProperty
    public List<String> getRegex() {
        return regex;
    }

    @JsonProperty
    public void setRegex(List<String> value) { regex = value; }
}
