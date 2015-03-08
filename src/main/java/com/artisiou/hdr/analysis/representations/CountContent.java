package com.artisiou.hdr.analysis.representations;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.Length;

public class CountContent extends Count {
    @Length(min = 3)
    private String content;

    @JsonProperty
    public String getContent() {
        return content;
    }

    public CountContent(int count, String content) {
        super(count);

        this.content = content;
    }
}
