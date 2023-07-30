package org.jlab.rfd.presentation.controller.ajax;

import org.jlab.rfd.business.util.DateUtil;
import org.junit.Assert;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.*;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;

public class ModAnodeAjaxTest {

    static final String CAVITY_CACHE_URL = "http://localhost:8080/RFDashboard/ajax/cavity-cache";
    private static final String MOD_ANODE_URL = "http://localhost:8080/RFDashboard/ajax/mod-anode";
    private JsonObject makeQuery(String query) throws IOException {
        JsonObject json;
        URL url = new URL(MOD_ANODE_URL + query);
        InputStream is = url.openStream();

        try (JsonReader reader = Json.createReader(is)) {
            json = reader.readObject();
        }
        return json;
    }

    private void clearCache(Date date) throws IOException {
        JsonObject json;
        URL url = new URL(CAVITY_CACHE_URL + "?date=" + DateUtil.formatDateYMD(date) + "&secret=ayqs&action=clear");
        InputStream is = url.openStream();
        try (JsonReader reader = Json.createReader(is)) {
            json = reader.readObject();
        }
        if (json.get("rowsCleared") == null) {
            throw new RuntimeException(("Error processing cache clear response"));
        }
    }
    @Test
    public void testBasicUsageCEDData () throws IOException, ParseException {
        String d1 = "2021-12-15";
        String d2 = "2021-12-16";
        String d3 = "2021-12-17";
        Date date1 = DateUtil.parseDateStringYMD(d1);
        Date date2 = DateUtil.parseDateStringYMD(d2);
        Date date3 = DateUtil.parseDateStringYMD(d3);
        String query = "?start=" + d1 + "&end=" + d3 + "&timeUnit=day";
        String expString = "{" +
                "\"labels\":[\"Injector\",\"North\",\"South\",\"Total\"]," +
                "\"data\":[" +
                "[[1639526400000,\"0\"],[1639612800000,\"0\"],[1639699200000,\"0\"]]," +
                "[[1639526400000,\"20\"],[1639612800000,\"20\"],[1639699200000,\"20\"]]," +
                "[[1639526400000,\"43\"],[1639612800000,\"43\"],[1639699200000,\"43\"]]," +
                "[[1639526400000,\"63\"],[1639612800000,\"63\"],[1639699200000,\"63\"]]" +
                "]" +
                "}";
        JsonObject exp;
        try (JsonReader reader = Json.createReader(new StringReader(expString))) {
            exp = reader.readObject();
        }

        // Check without cache
        clearCache(date1);
        clearCache(date2);
        clearCache(date3);
        JsonObject res = makeQuery(query);
        Assert.assertEquals(exp, res);

        // Check with cache
        res = makeQuery(query);
        Assert.assertEquals(exp, res);
    }

    @Test
    public void testBasicUsageMYAData () throws IOException, ParseException {
        String d = "2022-03-15";
        Date date = DateUtil.parseDateStringYMD(d);
        String query = "?start=" + d + "&end=" + d + "&timeUnit=day";
        String expString = "{" +
                "\"labels\":[\"Injector\",\"North\",\"South\",\"Total\",\"Unknown\"]," +
                "\"data\":[" +
                "[[1647302400000,\"0\"]]," +
                "[[1647302400000,\"18\"]]," +
                "[[1647302400000,\"42\"]]," +
                "[[1647302400000,\"60\"]]," +
                "[[1647302400000,\"42\"]]" +
                "]" +
                "}";
        JsonObject exp;
        try (JsonReader reader = Json.createReader(new StringReader(expString))) {
            exp = reader.readObject();
        }

        // Check without the cache
        clearCache(date);
        JsonObject res = makeQuery(query);
        Assert.assertEquals(exp, res);

        // Check with cache
        res = makeQuery(query);
        Assert.assertEquals(exp, res);
    }

}
