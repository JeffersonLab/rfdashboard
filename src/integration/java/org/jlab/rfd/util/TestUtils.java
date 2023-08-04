package org.jlab.rfd.util;

import org.jlab.rfd.business.util.DateUtil;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;

public class TestUtils {
    private static final String CAVITY_URL = "http://localhost:8080/RFDashboard/ajax/cavity";
    private static final String CAVITY_CACHE_URL = "http://localhost:8080/RFDashboard/ajax/cavity-cache";

    public static void clearCache(String date) throws IOException, ParseException {
        Date d = DateUtil.parseDateStringYMD(date);
        clearCache(d);
    }
    public static void clearCache(Date date) throws IOException {
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

    public static JsonObject primeCache(String date) throws IOException {
        JsonObject json;
        URL url = new URL(CAVITY_URL + "?date=" + date);
        InputStream is = url.openStream();
        try (JsonReader reader = Json.createReader(is)) {
            json = reader.readObject();
        }
        return json;
    }
}
