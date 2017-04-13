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
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

/**
 *
 * @author adamc
 */
public class GsetService {

    private static final Logger LOGGER = Logger.getLogger(GsetService.class.getName());
    public static final String MYSAMPLER_URL = "http://myawebtest.acc.jlab.org/mySampler/data";
    public static final String CED_INVENTORY_URL = "http://ced.acc.jlab.org/inventory";

    public Map<String, BigDecimal> getCavityGsetData(Date timestamp, Map<String, String> name2Epics) throws IOException, ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd");
        Map<String, BigDecimal> gsetData = new HashMap<>();
        
        // Create a reverse lookup map.  name2Epics should be a 1:1 map
        Map<String, String> epics2Name = new HashMap<>();
        for( String name : (Set<String>) name2Epics.keySet()) {
            if (epics2Name.put(name2Epics.get(name), name) != null ) {
                throw new IllegalArgumentException("Found cavity to gset map was not 1:1");
            }
        }

        // Create the channel request string
        String channels = "";        
        for (String epicsName:  name2Epics.values() ) {
            if ( channels.isEmpty() ) {
                channels = epicsName + "GSET";
            } else {
                channels = channels + "+" + epicsName + "GSET";
            }
        }
        
        String mySamplerQuery = "?b=" + sdf.format(timestamp) + "&s=1&n=1&m=&channels=" + channels;
        URL url = new URL(MYSAMPLER_URL + mySamplerQuery);
        InputStream in = url.openStream();
        try (JsonReader reader = Json.createReader(in)) {
            JsonObject json = reader.readObject();
            LOGGER.log(Level.FINEST, "Received mySampler response: {0}", json.toString());
            if (json.containsKey("error")) {
                LOGGER.log(Level.WARNING, "Error querying mySample web service.  Response: {0}", json.toString());
                throw new IOException("Error querying mySampler web service: " + json.getString("error"));
            }
            JsonArray values = json.getJsonArray("data").getJsonObject(0).getJsonArray("values");
            LOGGER.log(Level.FINEST, "Recevied GSET data: {0}", values.toString());
            
            for(JsonObject value: values.getValuesAs(JsonObject.class)) {
                String epicsName = ((String) value.keySet().toArray()[0]).substring(0, 4);
                BigDecimal gset = null;
                if (! value.getString(epicsName + "GSET").startsWith("<") ) {
                    gset = new BigDecimal(value.getString(epicsName + "GSET"));
                }

                LOGGER.log(Level.FINEST, "GSETService Processing value: timestamp{0}, epicsName: {1}, gset: {2}",
                        new Object[] {sdf.format(timestamp), epicsName, gset});

                gsetData.put(epics2Name.get(epicsName), gset);
            }
        }
        
        return gsetData;
    }
}
