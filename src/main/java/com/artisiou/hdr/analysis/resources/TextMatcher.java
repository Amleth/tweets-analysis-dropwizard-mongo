package com.artisiou.hdr.analysis.resources;

import com.artisiou.hdr.analysis.corpus.TextFilter;
import com.artisiou.hdr.analysis.mongo.ManagedMongoClient;
import com.artisiou.hdr.analysis.tools.Excel;
import com.artisiou.hdr.analysis.tools.TwitterData;
import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.ImmutableList;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBCursor;
import com.mongodb.DBObject;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

@Path("/textfilter")
@Produces(MediaType.APPLICATION_JSON)
public class TextMatcher {
    private ManagedMongoClient mongoClient;

    private List<TextFilter> textfilters;
    private List<String> linksRegex;

    public TextMatcher(ManagedMongoClient mongoClient, List<TextFilter> textfilters, List<String> linksRegex) {
        this.mongoClient = mongoClient;
        this.textfilters = textfilters;
        this.linksRegex = linksRegex;
    }

    @GET
    @Timed
    public int get(@QueryParam("textfilter") String textfilter,
                   @DefaultValue("false") @QueryParam("iramuteq") Boolean iramuteq,
                   @DefaultValue("false") @QueryParam("excel") Boolean excel,
                   @DefaultValue("false") @QueryParam("hashtags") Boolean hashtags,
                   @DefaultValue("false") @QueryParam("users") Boolean users
    ) {
        int n = 0;

        // Fetching all tweets from database
        DBCursor cursor = mongoClient.getCollection().find(new BasicDBObject());

        // Setting the text matching regex for the requested textfilter
        List<String> textRegex = new ArrayList<>();
        for (TextFilter t : this.textfilters) if (textfilter.equals(t.getName())) textRegex = t.getRegex();

        // Now!
        Long now = Instant.now().toEpochMilli();

        //
        // Files & data structures helpers declarations
        //

        Optional<java.nio.file.Path> excelPath = Optional.empty();
        Optional<HashMap<String, Integer>> userStats = Optional.of(new HashMap<>());
        Optional<Workbook> wb = Optional.empty();
        Optional<BufferedWriter> iramuteqWriter = Optional.empty();
        Optional<BufferedWriter> usersStatsWriter = Optional.empty();

        try {
            //
            // Files initializations
            //

            if (iramuteq) {
                java.nio.file.Path iramuteqPath = Paths.get(System.getProperty("user.home"), "Desktop", "HDR-Iramuteq-" + now + ".txt");
                Files.createFile(iramuteqPath);
                iramuteqWriter = Optional.of(Files.newBufferedWriter(iramuteqPath, StandardCharsets.UTF_8, StandardOpenOption.WRITE));
            }
            if (users) {
                java.nio.file.Path userStatsPath = Paths.get(System.getProperty("user.home"), "Desktop", "HDR-Contributors-" + now + ".txt");

                wb = Optional.of(Excel.makeWorkbook(ImmutableList.of("Feuille_1")));

                // First row
                Row firstRow = wb.get().getSheetAt(0).createRow(0);
                firstRow.createCell(0).setCellValue("screen name");
                firstRow.createCell(1).setCellValue("tweets");
            }
            if (excel) {
                excelPath = Optional.of(Paths.get(System.getProperty("user.home"), "Desktop", "HDR-Tweets-" + Instant.now().toEpochMilli() + ".xlsx"));

                wb = Optional.of(Excel.makeWorkbook(ImmutableList.of("Feuille_1")));

                // First row
                Row firstRow = wb.get().getSheetAt(0).createRow(0);
                firstRow.createCell(0).setCellValue("date");
                firstRow.createCell(1).setCellValue("texte");
                firstRow.createCell(2).setCellValue("[user] utilisateur");
                firstRow.createCell(3).setCellValue("[user] nom du compte");
                firstRow.createCell(4).setCellValue("hashtags");
                firstRow.createCell(5).setCellValue("réponse à utilisateur");
                firstRow.createCell(6).setCellValue("mentions d'utilisateurs");
                firstRow.createCell(7).setCellValue("retweets");
                firstRow.createCell(8).setCellValue("favoris");
                firstRow.createCell(9).setCellValue("lieu de publication");
                firstRow.createCell(10).setCellValue("[user] date de création");
                firstRow.createCell(11).setCellValue("[user] followers");
                firstRow.createCell(12).setCellValue("[user] following");
                firstRow.createCell(13).setCellValue("[user] tweets publiés");
                firstRow.createCell(14).setCellValue("[user] lieu déclaré");
                firstRow.createCell(15).setCellValue("[user] description");
                firstRow.createCell(16).setCellValue("[user] contributors_enabled");
                firstRow.createCell(17).setCellValue("[user] profile_image_url");
                firstRow.createCell(18).setCellValue("[user] profile_background_image_url");
                firstRow.createCell(19).setCellValue("[user] url");
                firstRow.createCell(20).setCellValue("source");
                firstRow.createCell(21).setCellValue("mid");
                firstRow.createCell(22).setCellValue("extended_entities");
                firstRow.createCell(23).setCellValue("contenu sensible");
                firstRow.createCell(24).setCellValue("date pour le tri");
            }

            try {

                //
                // Iteration over database records
                //

                while (cursor.hasNext()) {

                    // Extracting data...
                    DBObject doc = cursor.next();
                    DBObject tweet = (DBObject) doc.get("rawJson");
                    String text = (String) tweet.get("text");

                    // Does the tweet match the textfilter regex?
                    Boolean textmatch = false;
                    for (String r : textRegex) {
                        if (text.matches(r)) textmatch = true;
                    }
                    if (!textmatch) continue;

                    // Extracting data...
                    DBObject user = (DBObject) tweet.get("user");
                    String userScreenName = (String) user.get("screen_name");
                    LocalDateTime date = TwitterData.makeDate((String) tweet.get("created_at"));

                    if (users) {
                        if (!userStats.get().containsKey(userScreenName)) userStats.get().put(userScreenName, 0);
                        userStats.get().put(userScreenName, userStats.get().get(userScreenName) + 1);
                    }

                    // Replace HTML Entities
                    text = text.replace("&gt;", ">");
                    text = text.replace("&lt;", "<");
                    text = text.replace("&amp;", "&");
                    text = text.replace("&#8217;", "'");

                    //
                    // Iramuteq
                    //

                    if (iramuteq) {
                        String iramuteqText = text;

                        // Delete links
                        Boolean containsLink = false;
                        for (String r : this.linksRegex) {
                            Boolean match = iramuteqText.matches(".*" + r + ".*");
                            containsLink = containsLink || match;
                            if (match) iramuteqText = iramuteqText.replaceAll(r, "");
                        }

                        // Retweet
                        Boolean retweeted = iramuteqText.matches("^RT @.*");
                        if (retweeted) iramuteqText = iramuteqText.replaceAll("^RT @", "@");
                        iramuteqText.replace(" RT ", "");

                        // Kill _
                        iramuteqText = iramuteqText.replace("_", "");

                        // Write
                        iramuteqWriter.get().write("****");
                        iramuteqWriter.get().write(" *auteur_" + userScreenName);
                        iramuteqWriter.get().write(" *retweet_" + (retweeted ? "oui" : "non"));
                        iramuteqWriter.get().write(" *date_" + date.getYear() + String.format("%02d", date.getMonthValue()) + String.format("%02d", date.getDayOfMonth()));
                        iramuteqWriter.get().write(" *contientlien_" + (containsLink ? "oui" : "non"));
                        iramuteqWriter.get().newLine();
                        iramuteqWriter.get().write(iramuteqText);
                        iramuteqWriter.get().newLine();
                        iramuteqWriter.get().newLine();
                    }

                    //
                    // Excel
                    //

                    if (excel) {
                        Row row = wb.get().getSheetAt(0).createRow(n + 1);

                        // Extracting data...
                        DBObject e = (DBObject) tweet.get("entities");
                        DBObject ee = (DBObject) tweet.get("extended_entities");
                        BasicDBList media = (ee != null && ee.get("media") != null) ? (BasicDBList) ee.get("media") : new BasicDBList();

                        row.createCell(0).setCellValue(TwitterData.formatReadableDate(TwitterData.makeDate((String) tweet.get("created_at"))));
                        row.createCell(1).setCellValue(TwitterData.processText((String) tweet.get("text")));
                        row.createCell(2).setCellValue((String) user.get("screen_name"));
                        row.createCell(3).setCellValue((String) user.get("name"));
                        row.createCell(4).setCellValue(String.join(" ", TwitterData.extractHashtags((BasicDBList) e.get("hashtags"))));
                        row.createCell(5).setCellValue((String) tweet.get("in_reply_to_screen_name"));
                        row.createCell(6).setCellValue(String.join(" @", TwitterData.extractUserMentions((BasicDBList) e.get("user_mentions"))));
                        row.createCell(7).setCellValue((Integer) tweet.get("retweet_count"));
                        row.createCell(8).setCellValue((Integer) tweet.get("favorite_count"));
                        row.createCell(9).setCellValue((String) (tweet.get("place") != null ? ((DBObject) tweet.get("place")).get("name") : null));
                        row.createCell(10).setCellValue(TwitterData.formatReadableDate(TwitterData.makeDate((String) user.get("created_at"))));
                        row.createCell(11).setCellValue((Integer) user.get("followers_count"));
                        row.createCell(12).setCellValue((Integer) user.get("friends_count"));
                        row.createCell(13).setCellValue((Integer) user.get("statuses_count"));
                        row.createCell(14).setCellValue((String) user.get("location"));
                        row.createCell(15).setCellValue((String) user.get("description"));
                        row.createCell(16).setCellValue((Boolean) user.get("contributors_enabled"));
                        row.createCell(17).setCellValue((String) user.get("profile_image_url"));
                        row.createCell(18).setCellValue((String) user.get("profile_background_image_url"));
                        row.createCell(19).setCellValue((String) user.get("url"));
                        row.createCell(20).setCellValue(((String) tweet.get("source")).replaceAll("(<([^>]+)>)", ""));
                        row.createCell(21).setCellValue((String) tweet.get("id_str"));
                        row.createCell(22).setCellValue(String.join(" ", TwitterData.extractExtendedEntitiesMediaExpandedURL(media)));
                        row.createCell(23).setCellValue((Boolean) tweet.get("possibly_sensitive"));
                        row.createCell(24).setCellValue(date.toEpochSecond(ZoneOffset.UTC));
                    }

                    n++;
                    // System.out.println(n);
                }

                //
                // Writing & closing files
                //

                if (iramuteq) {
                    iramuteqWriter.get().close();
                }

                if (users) {
                    userStats.get().forEach((k, v) -> {
//                        try {
//                            // String.format("%03d", v)
//                            // k
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
                    });
                    usersStatsWriter.get().close();
                }

                if (excel) {
                    Excel.write(wb.get(), excelPath.get());
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        cursor.close();

        System.out.println();
        System.out.println(n);
        System.out.println();

        return n;
    }
}
