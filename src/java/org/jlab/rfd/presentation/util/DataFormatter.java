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
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
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
     * @param data A SortedMap keyed on data, with the value being a Map keyed
     * on LinacName, valued on some BigDecimal
     * @return A json object structured for direct consumption by Flot as a data
     * object {series1: [[1,2],...], series2:...}
     * @throws java.text.ParseException
     * @throws java.io.IOException
     */

    public static JsonObject toJson(SortedMap<Date, SortedMap<String, BigDecimal>> data) throws ParseException, IOException {

        // Javascript time format is like Unix Time, but in milliseconds.  the getTime() function gives you this by default.
        Map<String, BigDecimal> temp;
        // The tree map keeps the series sorted in a reliable fashion.  Needed to coordinate the alignment of labels and data... well maybe not...
        SortedMap<String, JsonArrayBuilder> seriesBuilders = new TreeMap<>();

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
