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

import static org.jlab.rfd.util.TestUtils.clearCache;

public class ModAnodeAjaxTest {

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
        // This was a tricky test case to work out.  The QTR cavities R027,R028 never had R...ModAnodeVolts PVs even
        // though they are a llrf 1.0 type.  When upgrade to llrf 3.0, they got R...KMAS pvs.  This means that after
        // start of 2022 when we switched away from CED and until summer of 2023, the ModAnodeVolts are unknown for
        // those two cavities (+2).  Then, for some reason, R17*KMAS was unarchived until summer 2022, so that's eight more
        // unknown (+8).  R1Q was uninstalled and didn't report in mya for some reason (+8).  Then R2O*KMAS was undefined (+8)
        //
        // R1A (1L10) was in a transitional state in the CED, and both KMAS and ModAnodeVolts PVs were unrecorded. (+8)
        // R04 (0L04) was offline due to network disconnects (+8).
        //
        // Total unknown really is 42.  This needed to be confirmed via the history archiver as some data had
        // aged out of the ops deployment.
        //
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
