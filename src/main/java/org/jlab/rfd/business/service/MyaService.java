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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.jlab.rfd.business.util.DateUtil;

/**
 *
 * @author adamc
 */
public class MyaService {

    private static final Logger LOGGER = Logger.getLogger(MyaService.class.getName());
    public static final String MYSAMPLER_URL = "https://myaweb.acc.jlab.org/mySampler/data";

    /*
    * returns null if timestamp is for future date
    * This assumes that PVs are of the structure <EPICSName><postfix>.  E.g. R123GMES or R2A8ODVH
    */
    public Map<String, BigDecimal> getCavityMyaData(Date timestamp, Map<String, String> name2Epics, String postfix) throws IOException, ParseException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if ( timestamp.after(new Date()) ) {
            return null;
        }
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
                channels = epicsName + postfix;
            } else {
                channels = channels + "+" + epicsName + postfix;
            }
        }
        
        String mySamplerQuery = "?b=" + sdf.format(timestamp) + "&s=1&n=1&m=&channels=" + channels;
        URL url = new URL(MYSAMPLER_URL + mySamplerQuery);
        InputStream in = url.openStream();
        try (JsonReader reader = Json.createReader(in)) {
            JsonObject json = reader.readObject();
            //LOGGER.log(Level.FINEST, "Received mySampler response: {0}", json.toString());
            if (json.containsKey("error")) {
                LOGGER.log(Level.WARNING, "Error querying mySampler web service.  Response: {0}", json.toString());
                throw new IOException("Error querying mySampler web service: " + json.getString("error"));
            }
            JsonArray values = json.getJsonArray("data").getJsonObject(0).getJsonArray("values");
            //LOGGER.log(Level.FINEST, "Recevied GSET data: {0}", values.toString());
            
            for(JsonObject value: values.getValuesAs(JsonObject.class)) {
                String epicsName = ((String) value.keySet().toArray()[0]).substring(0, 4);
                BigDecimal gset = null;
                if (! value.getString(epicsName + postfix).startsWith("<") ) {
                    gset = new BigDecimal(value.getString(epicsName + postfix));
                }

                //LOGGER.log(Level.FINEST, "GSETService Processing value: timestamp{0}, epicsName: {1}, gset: {2}",
                //        new Object[] {sdf.format(timestamp), epicsName, gset});

                gsetData.put(epics2Name.get(epicsName), gset);
            }
        }
        
        return gsetData;
    }

    /**
     * Runs a query against the myaweb mySampler service.  This simplified version hands back a single sample for the specified
     * date
     * @param channels A list of PVs
     * @param date The date to sample on
     * @param deployment
     * @return A map of PVs to response
     * @throws java.io.IOException Propogated up or thrown directly if the JSON resonse contains an error key
     */
    public Map<String, String> mySampler(List<String> channels, Date date, String deployment) throws IOException {
        
        Map<String, String> out = new HashMap<>();
        JsonObject response = mySampler(channels, date, 1, 1, deployment);

        if ( response.containsKey("error") ) {
            throw new IOException("Mya Error: " +  response.getString("error"));
        }
        
        JsonArray data = response.getJsonArray("data");
        JsonArray values = data.getJsonObject(0).getJsonArray("values");
        
        // The values array contains objects with a single PVName: Value pair.
        for(JsonObject pv : values.getValuesAs(JsonObject.class)) {
            String[] names = pv.keySet().toArray(new String[pv.size()]);
            out.put(names[0], pv.getString(names[0]));
        }
        
        return out;
    }
    
    /**
     * Runs a query against the myaweb mySampler service.  You probably want to use the simpler two parameter version
     * @param channels A list of PVs
     * @param date The start date
     * @param stepSize mySampler s param
     * @param numSteps mySampler n param
     * @param deployment mySampler m param
     * @return The services JSON response
     * @throws IOException Thrown if issue with URL connection to mySampler service
     */
    public JsonObject mySampler(List<String> channels, Date date, int stepSize, int numSteps, String deployment) throws IOException {
        
        String pvs = String.join("+", channels);
        
        String query = "?b=" + DateUtil.formatDateYMD(date) + "&n=" + numSteps + "&s=" + stepSize + "&m=" + deployment 
                + "&channels=" + pvs;

        URL url = new URL(MYSAMPLER_URL + query);
        InputStream in = url.openStream();
        JsonObject out = null;
        try (JsonReader reader = Json.createReader(in)) {
            out = reader.readObject();
        }

        return out;
    }
    
}
