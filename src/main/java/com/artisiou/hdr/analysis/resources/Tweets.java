package com.artisiou.hdr.analysis.resources;

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

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Path("/tweets")
@Produces(MediaType.APPLICATION_JSON)
public class Tweets {
    private ManagedMongoClient mongoClient;

    public Tweets(ManagedMongoClient mongoClient) {
        this.mongoClient = mongoClient;
    }

    @GET
    @Timed
    public void get() {
        DBCursor cursor = mongoClient.getCollection().find(new BasicDBObject());
        try {
            Workbook wb = Excel.makeWorkbook(ImmutableList.of("Feuille_1"));

            // First row
            Row firstRow = wb.getSheetAt(0).createRow(0);
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

            // Rows
            int r = 1;
            while (cursor.hasNext()) {
                Row row = wb.getSheetAt(0).createRow(r);

                // DBObjects
                DBObject t = cursor.next();
                t = (DBObject) t.get("rawJson");
                DBObject u = (DBObject) t.get("user");
                DBObject e = (DBObject) t.get("entities");
                DBObject ee = (DBObject) t.get("extended_entities");
                BasicDBList media = (ee != null && ee.get("media") != null) ? (BasicDBList) ee.get("media") : new BasicDBList();

                // Data
                LocalDateTime createdAt = TwitterData.makeDate(t.get("created_at").toString());

                row.createCell(0).setCellValue(TwitterData.formatReadableDate(TwitterData.makeDate((String) t.get("created_at"))));
                row.createCell(1).setCellValue(TwitterData.processText((String) t.get("text")));
                row.createCell(2).setCellValue((String) u.get("screen_name"));
                row.createCell(3).setCellValue((String) u.get("name"));
                row.createCell(4).setCellValue(String.join(" ", TwitterData.extractHashtags((BasicDBList) e.get("hashtags"))));
                row.createCell(5).setCellValue((String) t.get("in_reply_to_screen_name"));
                row.createCell(6).setCellValue(String.join(" @", TwitterData.extractUserMentions((BasicDBList) e.get("user_mentions"))));
                row.createCell(7).setCellValue((Integer) t.get("retweet_count"));
                row.createCell(8).setCellValue((Integer) t.get("favorite_count"));
                row.createCell(9).setCellValue((String) (t.get("place") != null ? ((DBObject) t.get("place")).get("name") : null));
                row.createCell(10).setCellValue(TwitterData.formatReadableDate(TwitterData.makeDate((String) u.get("created_at"))));
                row.createCell(11).setCellValue((Integer) u.get("followers_count"));
                row.createCell(12).setCellValue((Integer) u.get("friends_count"));
                row.createCell(13).setCellValue((Integer) u.get("statuses_count"));
                row.createCell(14).setCellValue((String) u.get("location"));
                row.createCell(15).setCellValue((String) u.get("description"));
                row.createCell(16).setCellValue((Boolean) u.get("contributors_enabled"));
                row.createCell(17).setCellValue((String) u.get("profile_image_url"));
                row.createCell(18).setCellValue((String) u.get("profile_background_image_url"));
                row.createCell(19).setCellValue((String) u.get("url"));
                row.createCell(20).setCellValue(((String) t.get("source")).replaceAll("(<([^>]+)>)", ""));
                row.createCell(21).setCellValue((String) t.get("id_str"));
                row.createCell(22).setCellValue(String.join(" ", TwitterData.extractExtendedEntitiesMediaExpandedURL(media)));
                row.createCell(23).setCellValue((Boolean) t.get("possibly_sensitive"));
                row.createCell(24).setCellValue(createdAt.toEpochSecond(ZoneOffset.UTC));

                r++;
            }

            // Write
            Excel.write(wb, Paths.get(System.getProperty("user.home"), "Desktop", "HDR-Tweets-" + Instant.now().toEpochMilli() + ".xlsx"));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }
    }
}
