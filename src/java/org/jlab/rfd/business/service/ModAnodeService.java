/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.business.service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.SortedMap;
import java.util.TreeMap;
import org.jlab.rfd.business.util.CebafNames;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.jlab.rfd.model.CryomoduleType;
import org.jlab.rfd.model.LinacName;
import org.jlab.rfd.model.ModAnodeDataPoint;
import org.jlab.rfd.model.ModAnodeDataSpan;

/**
 *
 * @author adamc
 */
public class ModAnodeService {

    public static final Logger LOGGER = Logger.getLogger(ModAnodeService.class.getName());

    // Append the wrkspc argument to the end of this string
    public static final String CED_INVENTORY_URL = "http://ced.acc.jlab.org/inventory";

    public HashSet<ModAnodeDataPoint> getModAnodeData(Date timestamp) throws IOException, ParseException {
        HashMap<String, CryomoduleType> cmTypes = new CryomoduleService().getCryoModuleTypes(timestamp);
        HashSet<ModAnodeDataPoint> data = new HashSet();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd");
        String wrkspc = sdf.format(timestamp);
        String cavityQuery = "?t=CryoCavity&p=ModAnode,Housed_by&out=json&ced=history&wrkspc=" + wrkspc;

        LOGGER.log(Level.FINEST, "CED Query: {0}", CED_INVENTORY_URL + cavityQuery);
        URL url = new URL(CED_INVENTORY_URL + cavityQuery);
        InputStream in = url.openStream();
        try (JsonReader reader = Json.createReader(in)) {
            JsonObject json = reader.readObject();
            String status = json.getString("stat");
            if (!"ok".equals(status)) {
                throw new IOException("unable to lookup Cavity Data from CED: response stat: " + status);
            }
            JsonObject inventory = json.getJsonObject("Inventory");
            JsonArray elements = inventory.getJsonArray("elements");
            CryomoduleType cmType;
            for (JsonObject element : elements.getValuesAs(JsonObject.class)) {
                BigDecimal mav = new BigDecimal(0);
                String cavityName = element.getString("name");
                cmType = cmTypes.get(cavityName.substring(0, 4));

                JsonObject properties = element.getJsonObject("properties");
                if (properties.containsKey("ModAnode")) {
                    mav = mav.add(new BigDecimal(properties.getString("ModAnode")));
                }
                data.add(new ModAnodeDataPoint(timestamp, cavityName, cmType, mav));
            }
        }
        return (data);
    }

    public ModAnodeDataSpan getModAnodeDataSpan(Date start, Date end, String timeUnit) throws ParseException, IOException {

        long timeInt;
        switch (timeUnit) {
            case "day":
                timeInt = 60 * 60 * 24 * 1000L;
                break;
            case "week":
            default:
                timeInt = 60 * 60 * 24 * 7 * 1000L;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd");

        // Convert date objects to have no hh:mm:ss ... portion
        Date curr = sdf.parse(sdf.format(start));
        Date e = sdf.parse(sdf.format(end));

        ModAnodeDataSpan span = new ModAnodeDataSpan();

        while (curr.before(e)) {
            span.put(curr, this.getModAnodeData(curr));
            curr = new Date(curr.getTime() + timeInt);            // Add one day
        }

        return span;
    }
}
