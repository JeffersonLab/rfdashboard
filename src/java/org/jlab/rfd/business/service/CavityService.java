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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.jlab.rfd.model.CavityDataPoint;
import org.jlab.rfd.model.CavityDataSpan;
import org.jlab.rfd.model.CryomoduleType;

/**
 *
 * @author adamc
 */
public class CavityService {

    private static final Logger LOGGER = Logger.getLogger(CavityService.class.getName());
    
    // Append the wrkspc argument to the end of this string
    public static final String CED_INVENTORY_URL = "http://ced.acc.jlab.org/inventory";

    
    public HashSet<CavityDataPoint> getCavityData(Date timestamp) throws IOException, ParseException {
        HashMap<String, CryomoduleType> cmTypes = new CryomoduleService().getCryoModuleTypes(timestamp);
        HashSet<CavityDataPoint> data = new HashSet();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd");
        String wrkspc = sdf.format(timestamp);
        String cavityQuery = "?t=CryoCavity&p=EPICSName,ModAnode,Housed_by&out=json&ced=history&wrkspc=" + wrkspc;

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

            // Construct a map of ced names to epics names
            String epicsName;
            HashMap<String, String> name2Epics = new HashMap();
            for (JsonObject element : elements.getValuesAs(JsonObject.class)) {
                String cavityName = element.getString("name");
                JsonObject properties = element.getJsonObject("properties");
                if (properties.containsKey("EPICSName")) {
                    epicsName = properties.getString("EPICSName");
                } else {
                    LOGGER.log(Level.WARNING, "Cryocavity '{0}' is missing EPICSName in ced history '{1}'.  Cannot process request.",
                            new Object[]{cavityName, wrkspc});
                    throw new IOException("Cryocavity '" + cavityName + "' missing EPICSName in ced history '" + wrkspc);
                }
                name2Epics.put(cavityName, epicsName);
            }
            GsetService gs = new GsetService();
            HashMap<String, BigDecimal> gsets = gs.getCavityGsetData(timestamp, name2Epics);

            for (JsonObject element : elements.getValuesAs(JsonObject.class)) {
                BigDecimal mav = new BigDecimal(0);
                String cavityName = element.getString("name");
                cmType = cmTypes.get(cavityName.substring(0, 4));

                JsonObject properties = element.getJsonObject("properties");
                if (properties.containsKey("ModAnode")) {
                    mav = mav.add(new BigDecimal(properties.getString("ModAnode")));
                }
                if (properties.containsKey("EPICSName")) {
                    epicsName = properties.getString("EPICSName");
                } else {
                    LOGGER.log(Level.WARNING, "Cryocavity '{0}' is missing EPICSName in ced history '{1}'.  Cannot process request.",
                            new Object[]{cavityName, wrkspc});
                    throw new IOException("Cryocavity '" + cavityName + "' missing EPICSName in ced history '" + wrkspc);
                }
                data.add(new CavityDataPoint(timestamp, cavityName, cmType, mav, epicsName, gsets.get(cavityName)));
            }
        }

        return (data);
    }

    public CavityDataSpan getCavityDataSpan(Date start, Date end, String timeUnit) throws ParseException, IOException {

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

        CavityDataSpan span = new CavityDataSpan();

        while (curr.before(e)) {
            span.put(curr, this.getCavityData(curr));
            curr = new Date(curr.getTime() + timeInt);            // Add one day
        }

        return span;
    }
}