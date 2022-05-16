/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.model.ModAnodeHarvester;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObjectBuilder;

/**
 *
 * @author adamc
 */
public class LinacDataSpan {

    private final SortedMap<Date, Set<LinacDataPoint>> dataSpan;

    /**
     *
     */
    public LinacDataSpan() {
        dataSpan = new TreeMap<>();
    }

    public int size() {
        return dataSpan.size();
    }

    /**
     *
     * @param ldp
     * @return
     */
    public boolean add(LinacDataPoint ldp) {
        if (!dataSpan.containsKey(ldp.getTimestamp())) {
            dataSpan.put(ldp.getTimestamp(), new HashSet<LinacDataPoint>());
        }
        return dataSpan.get(ldp.getTimestamp()).add(ldp);
    }

    public Set<LinacDataPoint> put(Date timestamp, Set<LinacDataPoint> data) {
        return dataSpan.put(timestamp, data);
    }

    /**
     * This returns a JSON array in the following format [ { "date": yyyy-MM-dd,
     * "linacs": { "North" : { "mav" : { "1050" : #, "1090" : #}, "no_mav" : {
     * "1050" : #, "1090" : #} }, "South" : { ... } } }, ... ]
     *
     * @return A JSON object representing the data of the LinacDataSpan in the
     * above format
     */
    public JsonArray toJson() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        JsonArrayBuilder data = Json.createArrayBuilder();
        for (Date d : dataSpan.keySet()) {

            JsonObjectBuilder point = Json.createObjectBuilder();
            point.add("date", sdf.format(d));

            JsonObjectBuilder linacs = Json.createObjectBuilder();
            for (LinacDataPoint ldp : dataSpan.get(d)) {
                if (ldp != null) {
                    linacs.add(ldp.getLinacName().toString(), Json.createObjectBuilder()
                            .add("mav", Json.createObjectBuilder()
                                    .add("1050", ldp.getTrips1050() == null ? "" : ldp.getTrips1050().toPlainString())
                                    .add("1090", ldp.getTrips1090() == null ? "" : ldp.getTrips1090().toPlainString())
                                    .build())
                            .add("no_mav", Json.createObjectBuilder()
                                    .add("1050", ldp.getTripsNoMav1050() == null ? "" : ldp.getTripsNoMav1050().toPlainString())
                                    .add("1090", ldp.getTripsNoMav1090() == null ? "" : ldp.getTripsNoMav1090().toPlainString())
                                    .build())
                            .build());
                }
            }
            data.add(point.add("linacs", linacs.build()).build());
        }
        return data.build();
    }

    /**
     * This outputs the Linac trip rates as a time series like data structure.
     * The outer Map is sorted on date, the inner is sorted on series label.
     *
     * @return
     */
    public SortedMap<Date, SortedMap<String, BigDecimal>> getTripRates() {

        if (dataSpan.isEmpty()) {
            return null;
        }

        SortedMap<Date, SortedMap<String, BigDecimal>> data = new TreeMap<>();
        for (Date d : dataSpan.keySet()) {
            TreeMap<String, BigDecimal> tmp = new TreeMap<>();
            BigDecimal tnm1050 = BigDecimal.ZERO;
            BigDecimal t1050 = BigDecimal.ZERO;
            BigDecimal tnm1090 = BigDecimal.ZERO;
            BigDecimal t1090 = BigDecimal.ZERO;

            for (LinacDataPoint ldp : dataSpan.get(d)) {

                // Need to make sure that both Linacs had non-null trip rates.  This indicates that both could be run at this energy and
                // is a combined trip rate for CEBAF.
                if (tnm1050 != null) {
                    tnm1050 = ldp.getTripsNoMav1050() == null ? null : tnm1050.add(ldp.getTripsNoMav1050());
                }
                if (tnm1090 != null) {
                    tnm1090 = ldp.getTripsNoMav1090() == null ? null : tnm1090.add(ldp.getTripsNoMav1090());
                }
                if (t1050 != null) {
                    t1050 = ldp.getTrips1050() == null ? null : t1050.add(ldp.getTrips1050());
                }
                if (t1090 != null) {
                    t1090 = ldp.getTrips1090() == null ? null : t1090.add(ldp.getTrips1090());
                }
            }
//            tmp.put("Total 1050 MeV No M.A.V.", tnm1050 == null ? BigDecimal.ZERO : tnm1050);
//            tmp.put("Total 1090 MeV No M.A.V.", tnm1090 == null ? BigDecimal.ZERO: tnm1090);
//            tmp.put("Total 1050 MeV", t1050 == null ? BigDecimal.ZERO : t1050);
//            tmp.put("Total 1090 MeV", t1090 == null ? BigDecimal.ZERO : t1090);
            tmp.put("Total 1050 MeV No M.A.V.", tnm1050);
            tmp.put("Total 1090 MeV No M.A.V.", tnm1090);
            tmp.put("Total 1050 MeV", t1050);
            tmp.put("Total 1090 MeV", t1090);
            data.put(d, tmp);
        }
        return data;
    }
}
