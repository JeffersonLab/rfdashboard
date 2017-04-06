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

//    /**
//     * This queries the CED history workspaces for historical data containing
//     * the mod anode voltage per cavity.
//     *
//     * @param time
//     * @throws java.io.IOException
//     * @return Returns a data structure containing the count of cavities with
//     * non-zero mod anode voltage by linac at the given time.
//     */
//    public HashMap getModAnodeVoltageByLinac(Date time) throws IOException {
//        String cavityQuery = "?t=CryoCavity&p=ModAnode,Housed_by&out=json&";
//        String cmQuery = "?t=CryoModule&p=ModuleType&out=json";
//
//        // These get reused throughout
//        URL url;
//        InputStream in;
//        HashMap data = new HashMap();
//        data.put("INJECTOR", new BigDecimal(0));
//        data.put("NORTH", new BigDecimal(0));
//        data.put("SOUTH", new BigDecimal(0));
//
//        LOGGER.log(Level.FINEST, "CED Query: {0}", CED_INVENTORY_URL + cavityQuery);
//        url = new URL(CED_INVENTORY_URL + cavityQuery);
//        in = url.openStream();
//        try (JsonReader reader = Json.createReader(in)) {
//            JsonObject json = reader.readObject();
//            String status = json.getString("stat");
//            if (!"ok".equals(status)) {
//                throw new IOException("unable to lookup Cavity Data from CED: response stat: " + status);
//            }
//            JsonObject inventory = json.getJsonObject("Inventory");
//            JsonArray elements = inventory.getJsonArray("elements");
//            String linac;
//            for (JsonObject element : elements.getValuesAs(JsonObject.class)) {
//                BigDecimal mav = new BigDecimal(0);
//                String name = element.getString("name");
//                linac = CebafNames.cedZoneToEnglishLinac(name.substring(0, 4));
//                JsonObject properties = element.getJsonObject("properties");
//                if (properties.containsKey("ModAnode")) {
//                    mav = mav.add(new BigDecimal(properties.getString("ModAnode")));
//                }
//                data.put(linac, ((BigDecimal) data.get(linac)).add(mav));
//            }
//        }
//
//        // Add the total to the data
//        data.put("TOTAL", ((BigDecimal) data.get("INJECTOR"))
//                .add((BigDecimal) data.get("NORTH"))
//                .add((BigDecimal) data.get("SOUTH")));
//        return (data);
//    }
//
//    /**
//     *
//     * @param date Corresponds to the CED history workspace queried
//     * @return A HashMap that contains total counts of cavities with non-zero
//     * mod anode voltage by linac
//     * @throws IOException
//     */
//    public HashMap getModAnodeCountsByLinac(Date date) throws IOException {
//
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/M/dd");
//        String wrkspc = sdf.format(date);
//
//        // Keep this here while Theo is wokring on the history web API feature
//        //String cavityQuery = "?t=CryoCavity&p=ModAnode&Ex=ModAnode&ced=history&wrkspc=" + wrkspc + "&out=json";
//        String cavityQuery = "?t=CryoCavity&p=ModAnode&Ex=ModAnode&out=json";
//
//        HashMap data = new HashMap();
//        data.put("INJECTOR", 0);
//        data.put("NORTH", 0);
//        data.put("SOUTH", 0);
//
//        URL url = new URL(CED_INVENTORY_URL + cavityQuery);
//        InputStream in = url.openStream();
//        try (JsonReader reader = Json.createReader(in)) {
//            JsonObject json = reader.readObject();
//            String status = json.getString("stat");
//            if (!"ok".equals(status)) {
//                throw new IOException("unable to lookup cavity data from CED: response stat: " + status);
//            }
//            JsonObject inventory = json.getJsonObject("Inventory");
//            JsonArray elements = inventory.getJsonArray("elements");
//            String linac;
//            for (JsonObject element : elements.getValuesAs(JsonObject.class)) {
//                JsonObject properties = element.getJsonObject("properties");
//                linac = CebafNames.cedZoneToEnglishLinac(element.getString("name").substring(0, 4));
//                data.put(linac, (Integer) data.get(linac) + 1);
//            }
//        }
//
//        // Add the total count to the data structure
//        data.put("TOTAL", (Integer) data.get("INJECTOR") + (Integer) data.get("NORTH") + (Integer) data.get("SOUTH"));
//        return (data);
//    }
//
//    /**
//     *
//     * @param start The start date used for querying data from CED history
//     * @param end The last date (inclusive) used fro querying data from CED
//     * history
//     * @return A HashMap keyed on
//     * @throws java.text.ParseException
//     * @throws java.io.IOException
//     */
//    public TreeMap getCavityCountData(Date start, Date end) throws ParseException, IOException {
//
//        if (!start.before(end)) {
//            LOGGER.log(Level.SEVERE, "start '{0}' must be earlier than end '{1}'", new Object[]{start, end});
//            throw new IllegalArgumentException("start must be earlier than end");
//        }
//
//        // Make sure we're using consistent times.  Remove hh:mm:ss... from date objects
//        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd");
//        Date s;
//        Date e;
//
//        try {
//            s = sdf.parse(sdf.format(start));
//            e = sdf.parse(sdf.format(end));
//        } catch (ParseException ex) {
//            LOGGER.log(Level.SEVERE, "Error parsing start/end dates for CED query: {0}", ex);
//            throw ex;
//        }
//        Date curr = (Date) s.clone();
//        TreeMap data = new TreeMap();
//
//        do {
//            data.put(curr, getModAnodeCountsByLinac(curr));
//
//            // Add one day
//            curr = new Date(curr.getTime() + 60 * 60 * 24 * 1000L);
//        } while (!curr.after(e));
//
//        return data;
//    }
    
    public HashSet<ModAnodeDataPoint> getModAnodeData(Date timestamp) throws IOException, ParseException {
        HashMap<String, CryomoduleType> cmTypes = new CryomoduleService().getCryoModuleTypes(timestamp);
        HashSet<ModAnodeDataPoint> data = new HashSet();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd");
        String wrkspc = sdf.format(timestamp);
        //String cavityQuery = "?t=CryoCavity&p=ModAnode,Housed_by&out=json&ced=history&wrkspc=" + wrkspc;
        String cavityQuery = "?t=CryoCavity&p=ModAnode,Housed_by&out=json&";

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
                cmType = cmTypes.get(cavityName.substring(0,4));
                
                JsonObject properties = element.getJsonObject("properties");
                if (properties.containsKey("ModAnode")) {
                    mav = mav.add(new BigDecimal(properties.getString("ModAnode")));
                }
                data.add(new ModAnodeDataPoint(timestamp, cavityName, cmType,mav));
            }
        }
        return (data);
    }
    
    public ModAnodeDataSpan getModAnodeDataSpan(Date start, Date end) throws ParseException, IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd");
        
        // Convert date objects to have no hh:mm:ss ... portion
        Date curr = sdf.parse(sdf.format(start));
        Date e = sdf.parse(sdf.format(end));

        ModAnodeDataSpan span = new ModAnodeDataSpan();
        
        while ( curr.before(e) ) {
            span.put(curr, this.getModAnodeData(curr));
            curr = new Date(curr.getTime() + 60 * 60 * 24 * 1000L);            // Add one day
        }
        
        return span;
    }
}