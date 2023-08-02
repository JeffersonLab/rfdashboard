package org.jlab.rfd.presentation.controller.ajax;

import org.jlab.rfd.business.util.DateUtil;
import org.junit.Assert;
import org.junit.Test;

import javax.json.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class CavityCacheTest {

    private static final String CACHE_URL = "http://localhost:8080/RFDashboard/ajax/cavity-cache";
    private static final String CAVITY_URL = "http://localhost:8080/RFDashboard/ajax/cavity";

    private JsonObject primeCache(String date) throws IOException {
        JsonObject json;
        URL url = new URL(CAVITY_URL + "?date=" + date);
        InputStream is = url.openStream();
        try (JsonReader reader = Json.createReader(is)) {
            json = reader.readObject();
        }
        return json;
    }
    private JsonObject clearCache(String date) throws IOException {
        JsonObject json;
        URL url = new URL(CACHE_URL + "?date=" + date + "&action=clear&secret=ayqs");
        InputStream is = url.openStream();
        try (JsonReader reader = Json.createReader(is)) {
            json = reader.readObject();
        }
        return json;
    }

        private JsonObject makeQuery(String query) throws IOException {
        JsonObject json;
        URL url = new URL(CACHE_URL + query);
        InputStream is = url.openStream();
        try (JsonReader reader = Json.createReader(is)) {
            json = reader.readObject();
        }
        return json;
    }
    @Test
    public void testBasicUsageEmptyCache() throws IOException {
        String dateString = "2022-03-15";
        String expString = "{\n" +
                "  \"response\": \"Success\",\n" +
                "  \"data\": {\n" +
                "    \"data\": [\n" +
                "      \n" +
                "    ]\n" +
                "  }\n" +
                "}";
        JsonObject exp;
        try(JsonReader reader = Json.createReader(new StringReader(expString))) {
            exp = reader.readObject();
        }

        clearCache(dateString);
        JsonObject result = makeQuery("?date="+dateString);
        Assert.assertEquals(exp, result);
    }

    @Test
    public void testBasicUsageFullCache() throws IOException {
        String dateString = "2022-03-15";
        // Full expected response is too long for string literal.  This is one of the cavities that should be received.
        String expString = "{\"name\":\"2L03-7\",\"linac\":\"South\",\"gset\":5.55455,\"modAnodeVoltage_kv\":0.0," +
                "\"odvh\":7.0,\"q0\":\"5.74E+09\",\"qExternal\":\"7.63E+06\",\"maxGset\":7.0,\"opsGsetMax\":\"\"," +
                "\"tripOffset\":6.567,\"tripSlope\":1.499,\"length\":0.5,\"bypassed\":false,\"tunerBad\":false," +
                "\"moduleType\":\"C25\",\"epicsName\":\"R237\"}";
        JsonObject exp;
        try(JsonReader reader = Json.createReader(new StringReader(expString))) {
            exp = reader.readObject();
        }

        primeCache(dateString);
        JsonObject result = makeQuery("?date="+dateString);
        // The response string literal is too big, so we can't just compare JsonObject.  Instead, check a few key aspects.
        Assert.assertEquals("Success", result.getString("response"));
        Assert.assertEquals(1, result.getJsonObject("data").getJsonArray("data").size());
        Assert.assertEquals(418, result.getJsonObject("data").getJsonArray("data")
                .getJsonObject(0).getJsonArray("cavities").size());

        // Check that the response has the cavity we want and that the cavity has the data we expect.
        boolean found = false;
        JsonArray cavities = result.getJsonObject("data").getJsonArray("data").getJsonObject(0).getJsonArray("cavities");
        for(int i = 0; i < cavities.size(); i++) {
            JsonObject cavity = cavities.getJsonObject(i);
            if (cavity.get("name") != null && cavity.getString("name").equals("2L03-7")) {
                Assert.assertEquals(exp, cavity);
                found = true;
            }
        }
        Assert.assertTrue(found);

    }

    @Test
    public void testLargeQuery() throws ParseException, IOException {
        // The internal SQL query is now chunked to prevent it from growing too large.  This happens for queries
        // larger than the set chunk size (around a year's worth of data).
        String startString = "2020-01-01";
        Calendar c = Calendar.getInstance();
        c.setTime(DateUtil.parseDateStringYMD(startString));
        Date start = c.getTime();
        int numDates = 190;

        List<Date> dateList = new ArrayList<>();
        Date curr = start;
        dateList.add(start);
        for (int i = 1; i < numDates; i++) {
            c.add(Calendar.DATE, 1);
            curr = c.getTime();
            dateList.add(curr);
        }
        String middleString = "2020-07-01"; // Should be after the chunk break.  No guarantee if someone changes code.
        String endString = DateUtil.formatDateYMD(curr);

        primeCache(startString);
        primeCache(middleString);
        primeCache(endString);
        StringBuilder builder = new StringBuilder("?date=" + startString);
        for(Date date : dateList) {
            builder.append("&date=" + DateUtil.formatDateYMD(date));
        }
        String query = builder.toString();

        JsonObject result = makeQuery(query);

        // This is an array objects that each contain the data for a single date
        JsonArray data = result.getJsonObject("data").getJsonArray("data");
        Assert.assertTrue(data.size() >= 3);

        // Check that we got what we know should be there.
        boolean foundStart = false;
        boolean foundMiddle = false;
        boolean foundEnd = false;
        for (int i = 0; i < data.size(); i++) {
            if (data.getJsonObject(i).getString("date").equals(startString)) {
                foundStart = true;
            }
            if (data.getJsonObject(i).getString("date").equals(middleString)) {
                foundMiddle = true;
            }
            if (data.getJsonObject(i).getString("date").equals(endString)) {
                foundEnd = true;
            }
        }
        Assert.assertTrue(foundStart);
        Assert.assertTrue(foundMiddle);
        Assert.assertTrue(foundEnd);
    }
}
