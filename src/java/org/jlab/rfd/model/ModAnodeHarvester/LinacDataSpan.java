/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.model.ModAnodeHarvester;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.jlab.rfd.model.LinacName;

/**
 *
 * @author adamc
 */

public class LinacDataSpan {
    private final SortedMap<Date, Map<LinacName, LinacDataPoint>> dataSpan;
    
    
    /**
     * 
     */
    public LinacDataSpan () {
        dataSpan = new TreeMap<>();
    }
    
    /**
     * 
     * @param ldp
     * @return 
     */
    public LinacDataPoint add(LinacDataPoint ldp) {
        if ( ! dataSpan.containsKey(ldp.getTimestamp()) ) {
            dataSpan.put(ldp.getTimestamp(), new HashMap<>());
        }
        return dataSpan.get(ldp.getTimestamp()).put(ldp.getLinacName(), ldp);
    }
    
    public Map<LinacName, LinacDataPoint> put(Date timestamp, Map<LinacName, LinacDataPoint> data) {
        return dataSpan.put(timestamp, data);
    }
    
    /**
     * This returns a JSON array in the following format
     * [ 
     *   { 
     *     "date": yyyy-MM-dd,
     *     "linacs": {
     *                 "North" : {
     *                                "mav" : { "1050" : #, "1090" : #},
     *                                "no_mav" : { "1050" : #, "1090" : #}
     *                              },
     *                  "South" : { ... }
     *               }
     *    },
     *    ...
     * ]
     *                                         
     * @return A JSON object representing the data of the LinacDataSpan in the above format
     */
    public JsonArray toJson() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        JsonArrayBuilder data = Json.createArrayBuilder();
        for ( Date d : dataSpan.keySet() ) {
            JsonObjectBuilder point = Json.createObjectBuilder();
            point.add("date", sdf.format(d));

            JsonObjectBuilder linacs = Json.createObjectBuilder();
            for ( LinacName linacName : dataSpan.get(d).keySet() ) {
                LinacDataPoint ldp = dataSpan.get(d).get(linacName);

                linacs.add(linacName.toString(), Json.createObjectBuilder()
                        .add("mav", Json.createObjectBuilder()
                                .add("1050", ldp.getTrips1050())
                                .add("1090", ldp.getTrips1050())
                                .build())
                        .add("no_mav", Json.createObjectBuilder()
                                .add("1050", ldp.getTripsNoMav1050())
                                .add("1090", ldp.getTripsNoMav1090())
                                .build())
                        .build());
            }
            data.add(point.add("linacs", linacs.build()).build());
        }
        return data.build();
    }
}