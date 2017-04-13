/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.presentation.util;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Set;
import java.util.TreeMap;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import org.jlab.rfd.model.LinacName;

/**
 *
 * @author adamc
 */
public class DataFormatter {

    private static final Logger LOGGER = Logger.getLogger(DataFormatter.class.getName());

    /**
     * This function is designed to format a "By Linac", time series data
     * structure as in JSON format.
     *
     * @param data A TreeMap keyed on data, with the value being a HashMap keyed
     * on LinacName, valued on some BigDecimal
     * @return A json object structured for direct consumption by Flot as a data
     * object {series1: [[1,2],...], series2:...}
     * @throws java.text.ParseException
     * @throws java.io.IOException
     */
//    public static JsonObject toJson(TreeMap<Date, HashMap<LinacName, BigDecimal>> data) throws ParseException, IOException {
//
//        // Javascript time format is like Unix Time, but in milliseconds.  the getTime() function gives you this by default.
//        HashMap<LinacName, BigDecimal> temp;
//        JsonArrayBuilder iBuilder = Json.createArrayBuilder();
//        JsonArrayBuilder nBuilder = Json.createArrayBuilder();
//        JsonArrayBuilder sBuilder = Json.createArrayBuilder();
//        JsonArrayBuilder tBuilder = Json.createArrayBuilder();
//
//        for (Date curr : (Set<Date>) data.keySet()) {
//            temp = data.get(curr);
//            iBuilder.add(Json.createArrayBuilder()
//                    .add(curr.getTime())
//                    .add(temp.get(LinacName.Injector)));
//
//            nBuilder.add(Json.createArrayBuilder()
//                    .add(curr.getTime())
//                    .add(temp.get(LinacName.North)));
//
//            sBuilder.add(Json.createArrayBuilder()
//                    .add(curr.getTime())
//                    .add(temp.get(LinacName.South)));
//
//            tBuilder.add(Json.createArrayBuilder()
//                    .add(curr.getTime())
//                    .add(temp.get(LinacName.Total)));
//
//            // Add one day
//            curr = new Date(curr.getTime() + 60 * 60 * 24 * 1000L);
//        }
//
//        JsonArray iSeries = iBuilder.build();
//        JsonArray nSeries = nBuilder.build();
//        JsonArray sSeries = sBuilder.build();
//        JsonArray tSeries = tBuilder.build();
//
//        // Make sure to keep the order straight.
//        JsonObject chartData = Json.createObjectBuilder()
//                .add("labels", Json.createArrayBuilder()
//                        .add("Injector")
//                        .add("North")
//                        .add("South")
//                        .add("Total")
//                        .build())
//                .add("data", Json.createArrayBuilder()
//                        .add(iSeries)
//                        .add(nSeries)
//                        .add(sSeries)
//                        .add(tSeries)
//                        .build())
//                .build();
//
//        return chartData;
//    }

    public static JsonObject toJson(TreeMap<Date, HashMap<String, BigDecimal>> data) throws ParseException, IOException {

        // Javascript time format is like Unix Time, but in milliseconds.  the getTime() function gives you this by default.
        HashMap<String, BigDecimal> temp;
        TreeMap<String, JsonArrayBuilder> seriesBuilders = new TreeMap();

        for (Date curr : (Set<Date>) data.keySet()) {
            temp = data.get(curr);

            for (String seriesName : data.get(curr).keySet()) {
                if (!seriesBuilders.containsKey(seriesName)) {
                    seriesBuilders.put(seriesName, Json.createArrayBuilder());
                }

                seriesBuilders.get(seriesName)
                        .add(Json.createArrayBuilder()
                                .add(curr.getTime())
                                .add(temp.get(seriesName)));
            }
        }

        JsonArrayBuilder labelBuilder = Json.createArrayBuilder();
        JsonArrayBuilder dataBuilder = Json.createArrayBuilder();
        
        for (String seriesName : seriesBuilders.keySet()) {
            labelBuilder.add(seriesName);
            dataBuilder.add(seriesBuilders.get(seriesName).build());
        }

        JsonObject chartData = Json.createObjectBuilder()
                .add("labels", labelBuilder.build())
                .add("data", dataBuilder.build())
                .build();

        return chartData;
    }
}
