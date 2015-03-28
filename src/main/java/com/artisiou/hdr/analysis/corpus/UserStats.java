package com.artisiou.hdr.analysis.corpus;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;

public class UserStats {
    public UserStats() {
        tweets = 0;
    }

    @NotNull
    private String screenName;

    public String getScreenName() { return screenName; }

    public void setScreenName(String value) { screenName = value; }

    @NotNull
    @Min(0)
    private int tweets;

    public int getTweets() { return tweets; }

    public void setTweets(int value) { this.tweets = value; }
}
