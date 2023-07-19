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
import java.util.Date;

/**
 * This is a test of the CavityAjax (i.e. /ajax/cavity) endpoint.  The expected result should have a structure like this
 * {
 *   "data":[
 *     {
 *       "date":"2020-02-02",
 *       "cavities":[
 *         {
 *           "name":"1L10-5","linac":"North","gset":6.7835,"modAnodeVoltage_kv":0.0,"odvh":7.6,"q0":"5.31E+09",
 *            "qExternal":"2.73E+06","maxGset":7.6,"opsGsetMax":"","tripOffset":7.255,"tripSlope":2.156,"length":0.5,
 *            "bypassed":false,"tunerBad":false,"moduleType":"C25","epicsName":"R1A5"
 *         }, ...
 *       ]
 *     }
 *   ]
 * }
 *
 */
public class CavityAjaxTest {
    static final String CAVITY_URL = "http://localhost:8080/RFDashboard/ajax/cavity";
    static final String CAVITY_CACHE_URL = "http://localhost:8080/RFDashboard/ajax/cavity-cache";

    private JsonObject makeQuery(String query) throws IOException {
        JsonObject json;
        URL url = new URL(CAVITY_URL + query);
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

    private void basicUsage(String date) throws IOException {
        // Date is YYYY-mm-dd
        String query = "?t=day&date=" + date + "&out=json";

        String expString = "{\"name\":\"2L19-3\",\"linac\":\"South\",\"gset\":5.18681,\"modAnodeVoltage_kv\":0.3," +
                "\"odvh\":9.0,\"q0\":\"3.36E+09\",\"qExternal\":\"4.7E+06\",\"maxGset\":9.0,\"opsGsetMax\":\"\"," +
                "\"tripOffset\":8.292,\"tripSlope\":0.924,\"length\":0.5,\"bypassed\":false,\"tunerBad\":false," +
                "\"moduleType\":\"C25\",\"epicsName\":\"R2J3\"}";

        JsonObject exp;
        try (JsonReader reader = Json.createReader(new StringReader(expString))) {
            exp = reader.readObject();
        }

        JsonObject json = makeQuery(query);

        JsonObject result = null;
        JsonArray cavities = json.getJsonArray("data").getJsonObject(0).getJsonArray("cavities");
        for(JsonObject cav : cavities.getValuesAs(JsonObject.class)) {
            if (cav.getString("name").equals("2L19-3")){
                result = cav;
            }
        }

        // Compare a single cavity in depth
        Assert.assertEquals(exp, result);

        // Did we get only a single result?
        Assert.assertEquals(1, json.getJsonArray("data").size());

        // Did we get the full complement of 418 CryoCavities?
        Assert.assertEquals(418, cavities.size());
    }

    @Test
    public void testBasicUsageNoCache() throws IOException, ParseException {
        // Test querying a single date with and without the cache
        String date = "2020-02-02";
        clearCache(DateUtil.parseDateStringYMD(date));
        basicUsage(date);
    }

    @Test
    public void testBasicUsageWithCache() throws IOException {
        // Test querying a single date with and without the cache
        String date = "2020-02-02";
        basicUsage(date);
    }


    @Test
    public void testWeeklyQuery() throws IOException {
        String query = "?t=day&start=2020-02-02&end=2020-02-09&timeUnit=week&out=json";

        JsonObject json = makeQuery(query);
        JsonArray data = json.getJsonArray("data");

        // Should return data from 2020-02-02 and 2020-02-09
        Assert.assertEquals(2, data.size());
        Assert.assertEquals("2020-02-02", data.getJsonObject(0).getString("date"));
        Assert.assertEquals("2020-02-09", data.getJsonObject(1).getString("date"));

        // Did we get all the cavities?
        JsonArray cavities0 = data.getJsonObject(0).getJsonArray("cavities");
        JsonArray cavities1 = data.getJsonObject(1).getJsonArray("cavities");
        Assert.assertEquals(418, cavities0.size());
        Assert.assertEquals(418, cavities1.size());
    }

    @Test
    public void testMultiDateQuery() throws IOException {
        String query = "?t=day&date=2020-02-02&date=2020-02-09&out=json";

        JsonObject json = makeQuery(query);
        JsonArray data = json.getJsonArray("data");

        // Should return data from 2020-02-02 and 2020-02-09
        Assert.assertEquals(2, data.size());
        Assert.assertEquals("2020-02-02", data.getJsonObject(0).getString("date"));
        Assert.assertEquals("2020-02-09", data.getJsonObject(1).getString("date"));

        // Did we get all the cavities?
        JsonArray cavities0 = data.getJsonObject(0).getJsonArray("cavities");
        JsonArray cavities1 = data.getJsonObject(1).getJsonArray("cavities");
        Assert.assertEquals(418, cavities0.size());
        Assert.assertEquals(418, cavities1.size());
    }

    private void throwBadOut() throws IOException {
        makeQuery("?t=day&date=2020-02-02&out=JUNK");
    }

    @Test
    public void testBadOut() {
        Assert.assertThrows(IOException.class, this::throwBadOut);
    }
}