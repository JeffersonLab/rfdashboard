package org.jlab.rfd.presentation.controller.ajax;

import org.jlab.rfd.business.util.DateUtil;
import org.junit.Assert;
import org.junit.Test;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;

import static org.jlab.rfd.util.TestUtils.clearCache;

public class LinacAjaxTest {


    private static final String QUERY_URL = "http://localhost:8080/RFDashboard/ajax/linac";
    private JsonObject makeQuery(String query) throws IOException {
        JsonObject json;
        URL url = new URL(QUERY_URL + query);
        InputStream is = url.openStream();

        try (JsonReader reader = Json.createReader(is)) {
            json = reader.readObject();
        }
        return json;
    }

    @Test
    public void testBasicUsage() throws ParseException, IOException {

        String d = "2017-07-25";
        Date date = DateUtil.parseDateStringYMD(d);

        String expString = "{" +
                "\"data\":[" +
                "{\"date\":\"2017-07-25\"," +
                "\"linacs\":{" +
                "\"North\":" +
                "{\"mav\":{\"1050\":\"1.373688\",\"1090\":\"5.620650\"}," +
                "\"no_mav\":{\"1050\":\"1.225401\",\"1090\":\"4.712613\"}}," +
                "\"South\":{" +
                "\"mav\":{\"1050\":\"3.063313\",\"1090\":\"15.256875\"}," +
                "\"no_mav\":{\"1050\":\"2.465463\",\"1090\":\"11.024100\"}" +
                "}}}]}";
        JsonObject exp;
        try (JsonReader reader = Json.createReader(new StringReader(expString))) {
            exp = reader.readObject();
        }

        // Test without the cache
        clearCache(date);
        JsonObject result = makeQuery("?date=2017-07-25");
        Assert.assertEquals(exp, result);

        // Test with the cache
        result = makeQuery("?date=2017-07-25");
        Assert.assertEquals(exp, result);
    }
}
