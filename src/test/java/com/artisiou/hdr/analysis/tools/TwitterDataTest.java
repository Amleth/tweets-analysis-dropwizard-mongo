package com.artisiou.hdr.analysis.tools;

import jdk.nashorn.internal.ir.annotations.Ignore;
import org.junit.Test;

import java.text.ParseException;
import java.time.LocalDateTime;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TwitterDataTest {
    private final String dateString = "Wed Jan 28 20:09:53 +0000 2015";

    @Test
    public void makeDate() throws ParseException {
        LocalDateTime date = TwitterData.makeDate(dateString);
        assertThat(date.getYear(), is(2015));
        assertThat(date.getDayOfMonth(), is(28));
        assertThat(date.getMonthValue(), is(1));
        assertThat(date.getHour(), is(20));
        assertThat(date.getMinute(), is(9));
        assertThat(date.getSecond(), is(53));
    }

    @Test
    @Ignore
    public void formatReadableDate() {
        LocalDateTime date = TwitterData.makeDate(dateString);
        String readableDate = TwitterData.formatReadableDate(date);
    }
}


