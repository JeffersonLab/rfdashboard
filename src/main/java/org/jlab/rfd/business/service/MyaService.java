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
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.jlab.rfd.business.util.DateUtil;
import org.jlab.rfd.config.AppConfig;

/**
 *
 * @author adamc
 */
public class MyaService {

    private static final Logger LOGGER = Logger.getLogger(MyaService.class.getName());
    public static final String MYSAMPLER_URL = AppConfig.getAppConfig().getMyqueryUrl() + "/myquery/mysampler";

    /**
     * A convenience method for getCavityMyaData when there is only a single PV postfix.
     * @param timestamp What date are we sampling the data for
     * @param name2Epics A map of cavity name (CED) to EPICSName.  This is used to map results back to cavity name.
     * @param postfix The single postfix to be appended to every EPICSName
     * @return A map of cavity names (CED) to BigDecimal values of the PVs at that time or null if timestamp is in the
     * future.
     * @throws IOException If problem arise while contacting the mya web service.
     */
    public Map<String, BigDecimal> getCavityMyaData(Date timestamp, Map<String, String> name2Epics, String postfix) throws IOException {
        Map<String, List<String>> postfixes = new HashMap<>();
        for (String name : name2Epics.keySet()) {
            postfixes.putIfAbsent(name, new ArrayList<>());
            postfixes.get(name).add(postfix);
        }
        return getCavityMyaData(timestamp, name2Epics, postfixes);
    }


    /**
     * Query the MYA mysampler webservice for a collection of RF cavity PVs.  This assumes that PVs follow the standard
     * pattern of EPICSName<postfix>.  All values are assumed to be numeric values represented by BigDecimals.
     * @param timestamp What date are we sampling the data for
     * @param name2Epics A map of cavity name (CED) to EPICSName.  This is used to map results back to cavity name.
     * @param postfixes A map of cavity name (CED) to a list of PV postfixes that are to be queried.
     * @return A map of cavity names (CED) to BigDecimal values of the PVs at that time or null if timestamp is in the
     * future.
     * @throws IOException If problem arise while contacting the mya web service.
     */
    public Map<String, BigDecimal> getCavityMyaData(Date timestamp, Map<String, String> name2Epics, Map<String, List<String>> postfixes) throws IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if ( timestamp.after(new Date()) ) {
            return null;
        }

        // Switch to the history deployment if we are querying more than 180 days (~6 months) in the past
        String deployment = "ops";
        if (DateUtil.getDifferenceInDays(timestamp, new Date()) > 180) {
            deployment = "history";
        }
        Map<String, BigDecimal> gsetData = new HashMap<>();
        
        // Create a reverse lookup map.  name2Epics should be a 1:1 map
        Map<String, String> epics2Name = new HashMap<>();
        for( String name : name2Epics.keySet()) {
            if (epics2Name.put(name2Epics.get(name), name) != null ) {
                throw new IllegalArgumentException("Found cavity to gset map was not 1:1");
            }
        }

        // Create the channel request string
        boolean empty = true;
        StringBuilder builder = new StringBuilder();
        for (String name:  name2Epics.keySet() ) {
            String epicsName = name2Epics.get(name);
            for (String postfix : postfixes.get(name)) {
                if (!empty) {
                    builder.append(",");
                } else {
                    empty = false;
                }
                builder.append(epicsName);
                builder.append(postfix);
            }
        }
        String channels = builder.toString();

        String mySamplerQuery = "?b=" + sdf.format(timestamp) + "&m=" + deployment + "&s=1&n=1&m=&c=" + channels;
        URL url = new URL(MYSAMPLER_URL + mySamplerQuery);
        InputStream in = url.openStream();
        try (JsonReader reader = Json.createReader(in)) {
            JsonObject json = reader.readObject();
            if (json.containsKey("error")) {
                LOGGER.log(Level.WARNING, "Error querying mySampler web service.  Response: {0}", json.toString());
                throw new IOException("Error querying mySampler web service: " + json.getString("error"));
            }

            JsonObject chan = json.getJsonObject("channels");
            for (String pv : chan.keySet()) {
                if (pv == null || pv.isEmpty()) {
                    continue;
                } else if (chan.getJsonObject(pv).containsKey("error")) {
                    throw new IOException("Error querying PV '" + pv + "'.  " + chan.getJsonObject(pv).getString("error"));
                }
                String epicsName = pv.substring(0, 4);
                BigDecimal gset = null;
                JsonObject sample = chan.getJsonObject(pv).getJsonArray("data").get(0).asJsonObject();

                if (sample.containsKey("v")) {
                    gset = sample.getJsonNumber("v").bigDecimalValue();
                }
                gsetData.put(epics2Name.get(epicsName), gset);
            }
        }
        return gsetData;
    }

    /**
     * A simplified interface for a single date query.  Will use the history deployment if the request is older than
     * about six months.
     * @param channels The channels to be sampled
     * @param date The time at which to sample them.
     * @return A map of PVs to mySampler result
     */
    public Map<String, String> mySampler(List<String> channels, Date date) throws IOException {
        String deployment = "ops";
        if (DateUtil.getDifferenceInDays(date, new Date()) > 180) {
            deployment = "history";
        }
        return mySampler(channels, date, deployment);
    }


    /**
     * Runs a query against the myquery mySampler service.  This simplified version hands back a single sample for the specified
     * date
     * @param channels A list of PVs
     * @param date The date to sample on
     * @param deployment The MYA deployment to query
     * @return A map of PVs to response
     * @throws java.io.IOException Propagated up or thrown directly if the JSON response contains an error key
     */
    public Map<String, String> mySampler(List<String> channels, Date date, String deployment) throws IOException {
        
        Map<String, String> out = new HashMap<>();
        JsonObject response = mySampler(channels, date, 1, 1, deployment);

        if ( response.containsKey("error") ) {
            throw new IOException("Mya Error: " +  response.getString("error"));
        }

        JsonObject chan = response.getJsonObject("channels");
        for (String pv : chan.keySet()) {
            JsonObject sample = chan.getJsonObject(pv).getJsonArray("data").get(0).asJsonObject();

            if (sample.get("v") != null) {
                out.put(pv, sample.getJsonNumber("v").toString());
            } else if (sample.getString("t") !=null) {
                out.put(pv, sample.getString("t"));
            } else {
                throw new IOException("Unexpected mySampler format for '" + pv + "': " + sample);
            }
        }

        return out;
    }
    
    /**
     * Runs a query against the myquery mySampler service.  You probably want to use the simpler two parameter version
     * @param channels A list of PVs
     * @param date The start date
     * @param stepSize mySampler s param
     * @param numSteps mySampler n param
     * @param deployment mySampler m param
     * @return The services JSON response
     * @throws IOException Thrown if issue with URL connection to mySampler service
     */
    public JsonObject mySampler(List<String> channels, Date date, int stepSize, int numSteps, String deployment) throws IOException {

        String pvs = String.join(",", channels);
        String query = "?b=" + DateUtil.formatDateYMD(date) + "&n=" + numSteps + "&s=" + stepSize * 1000L + "&m=" + deployment
                + "&c=" + pvs;

        URL url = new URL(MYSAMPLER_URL + query);
        InputStream in = url.openStream();
        JsonObject out;
        try (JsonReader reader = Json.createReader(in)) {
            out = reader.readObject();
        }

        return out;
    }
    
}
