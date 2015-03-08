package com.artisiou.hdr.analysis.tools;

import com.mongodb.DBObject;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class TwitterData {
    public static final String TWITTER_DATE_PATTERN = "EEE MMM dd HH:mm:ss Z yyyy";
    public static final String READABLE_DATE_PATTERN = "EEEE dd/MM/yyyy HH:mm:ss";

    public static final String formatReadableDate(LocalDateTime date) {
        return date.format(DateTimeFormatter.ofPattern(READABLE_DATE_PATTERN));
    }

    public static final LocalDateTime makeDate(String date) {
        return LocalDateTime.parse(date, DateTimeFormatter.ofPattern(TWITTER_DATE_PATTERN).withLocale(Locale.ENGLISH));
    }

    public static final List<String> extractHashtags(List hashtags) {
        return (List<String>) hashtags.stream().map(€ -> ((DBObject) €).get("text")).collect(Collectors.toList());
    }

    public static final List<String> extractUserMentions(List userMentions) {
        return (List<String>) userMentions.stream().map(€ -> ((DBObject) €).get("screen_name")).collect(Collectors.toList());
    }

    public static final List<String> extractExtendedEntitiesMediaExpandedURL(List media) {
        return (List<String>) media.stream().map(€ -> ((DBObject) €).get("expanded_url")).collect(Collectors.toList());
    }

    public static final String processText(String text) {
        return String.join(" ", text.split("\n"));
    }
}
