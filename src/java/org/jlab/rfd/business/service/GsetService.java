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
import java.util.Set;
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

    public HashMap<String, BigDecimal> getCavityGsetData(Date timestamp, HashMap<String, String> name2Epics) throws IOException, ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd");
        HashMap<String, BigDecimal> gsetData = new HashMap();
        
        // Create a reverse lookup map.  name2Epics should be a 1:1 map
        HashMap<String, String> epics2Name = new HashMap();
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
                BigDecimal gset = new BigDecimal(value.getString(epicsName + "GSET"));
                gsetData.put(epics2Name.get(epicsName), gset);
                LOGGER.log(Level.FINEST, "GSETService Processing value: epicsName: {0}, gset: {1}", new Object[] {epicsName, gset});
            }
        }
        
        return gsetData;
    }

//    public HashMap<String, String> getCavityEpicsNames(Date timestamp) throws IOException {
//
//        HashMap <String, String> cavityEpicsNames = new HashMap();
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd");
//        String wrkspc = sdf.format(timestamp);
//        String cavityQuery = "?t=CryoCavity&p=EPICSName&out=json&ced=history&wrkspc=" + wrkspc;
//
//        LOGGER.log(Level.FINEST, "CED Query: {0}", CED_INVENTORY_URL + cavityQuery);
//        URL url = new URL(CED_INVENTORY_URL + cavityQuery);
//        InputStream in = url.openStream();
//        try (JsonReader reader = Json.createReader(in)) {
//            JsonObject json = reader.readObject();
//            String status = json.getString("stat");
//            if (!"ok".equals(status)) {
//                throw new IOException("unable to lookup Cavity Data from CED: response stat: " + status);
//            }
//            JsonObject inventory = json.getJsonObject("Inventory");
//            JsonArray elements = inventory.getJsonArray("elements");
//            for (JsonObject element : elements.getValuesAs(JsonObject.class)) {
//                String name = element.getString("name");
//                JsonObject properties = element.getJsonObject("properties");
//                if (properties.containsKey("EPICSName")) {
//                    cavityEpicsNames.put(name, properties.getString("EPICSName"));
//                } else {
//                    throw new IOException("Element '" + name + "' is missing EPICSName properties.  Can't service request.");
//                }
//            }
//        }
//        return cavityEpicsNames;
//    }
}
