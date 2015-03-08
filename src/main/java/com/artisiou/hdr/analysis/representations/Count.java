package com.artisiou.hdr.analysis.representations;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.Min;

public class Count {
    @Min(0)
    private int count;

    @JsonProperty
    public int getCount() {
        return count;
    }

    public Count(int count) {
        this.count = count;
    }
}
