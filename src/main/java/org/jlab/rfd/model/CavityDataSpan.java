/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.model;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 *
 * @author adamc
 */
public class CavityDataSpan {

    private final TreeMap<Date, Set<CavityResponse>> dataSpan;

    public CavityDataSpan() {
        dataSpan = new TreeMap<>();
    }

    public int size() {
        return dataSpan.size();
    }

    public Set<Date> keySet() {
        return dataSpan.keySet();
    }

    /**
     * Returns the set of CavityDataPoints for a given date
     *
     * @param date The requested date
     * @return A set of CavityDataPoints relating to the requested date
     */
    public Set<CavityResponse> get(Date date) {
        return dataSpan.get(date);
    }

    /**
     * Adds a single data point to the ModAnodeDataSpan. If this is the first
     * datapoint for it's timestamp, the internal TreeMap adds a key/HashSet for
     * that timestamp. Otherwise it is added to the existing HashSet.
     *
     * @param dataPoint
     * @return
     */
    public Object add(CavityResponse dataPoint) {
        if (!dataSpan.containsKey(dataPoint.getTimestamp())) {
            dataSpan.put(dataPoint.getTimestamp(), new HashSet<CavityResponse>());
        }
        return dataSpan.get(dataPoint.getTimestamp()).add(dataPoint);
    }

    /**
     * Adds a dataset for the specified timestamp. If a dataset already exists
     * for that timestamp, it is replaced.
     *
     * @param timestamp
     * @param dataSet
     * @return
     */
    public Object put(Date timestamp, Set<CavityResponse> dataSet) {

        return dataSpan.put(timestamp, dataSet);
    }

    /**
     * Generates a JSON object representing the CavityDataSpan. This should take
     * the format of { data: [ { date: 'YYYY-MM-DD', cavities: [{cavityObj}, {},
     * ... ] }, ... { date: 'YYYY-MM-DD', cavities: [{cavityObj}, {}, ... ] } ]
     * }
     *
     * @return
     */
    public JsonObject toJson() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        JsonArrayBuilder data = Json.createArrayBuilder();
        for (Date d : dataSpan.keySet()) {
            JsonObjectBuilder sample = Json.createObjectBuilder();
            sample.add("date", sdf.format(d));
            JsonArrayBuilder cavities = Json.createArrayBuilder();

            for (CavityDataPoint dp : dataSpan.get(d)) {
                cavities.add(dp.toJson());
            }
            sample.add("cavities", cavities.build());
            data.add(sample.build());
        }

        JsonObject out = Json.createObjectBuilder().add("data", data.build()).build();
        return out;
    }

    /**
     * This returns a TreeMap keyed on date with values being the count of
     * cavities with non-zero modAnodeVoltage by linac on that date.
     *
     * Converts the enum linac names to strings so that it can easily be handled
     * by formatter classes
     *
     * @return
     */
    public SortedMap<Date, SortedMap<String, BigDecimal>> getModAnodeCountByLinac() {
        SortedMap<Date, SortedMap<String, BigDecimal>> data = new TreeMap<>();

        SortedMap<String, BigDecimal> byLinac;
        int total;
        int unknown;
        for (Date date : (Set<Date>) dataSpan.keySet()) {
            byLinac = new TreeMap<>();
            byLinac.put(LinacName.Injector.toString(), new BigDecimal(0));
            byLinac.put(LinacName.North.toString(), new BigDecimal(0));
            byLinac.put(LinacName.South.toString(), new BigDecimal(0));
            total = 0;
            unknown = 0;

            for (CavityResponse cDP : (Set<CavityResponse>) dataSpan.get(date)) {
                if (cDP.getModAnodeVoltage() == null) {
                    unknown++;
                } else if (cDP.getModAnodeVoltage().doubleValue() > 0) {
                    byLinac.put(cDP.getLinacName().toString(), byLinac.get(cDP.getLinacName().toString()).add(new BigDecimal(1)));
                    total++;
                }

            }
            byLinac.put(LinacName.Total.toString(), new BigDecimal(total));
            byLinac.put("Unknown", new BigDecimal(unknown));
            data.put(date, byLinac);
        }
        return data;
    }

    public SortedMap<Date, SortedMap<String, BigDecimal>> getModAnodeCountByCMType() {
        SortedMap<Date, SortedMap<String, BigDecimal>> data = new TreeMap<>();

        SortedMap<String, BigDecimal> byCMType;
        int total;
        int unknown;
        String CMType;

        for (Date date : (Set<Date>) dataSpan.keySet()) {
            byCMType = new TreeMap<>();
            byCMType.put(CryomoduleType.C100.toString(), new BigDecimal(0));
            byCMType.put(CryomoduleType.C50.toString(), new BigDecimal(0));
            byCMType.put(CryomoduleType.C25.toString(), new BigDecimal(0));
            byCMType.put(CryomoduleType.F100.toString(), new BigDecimal(0));
            byCMType.put(CryomoduleType.C75.toString(), new BigDecimal(0));
            byCMType.put(CryomoduleType.C50T.toString(), new BigDecimal(0));
            total = 0;
            unknown = 0;

            for (CavityResponse cDP : (Set<CavityResponse>) dataSpan.get(date)) {

                if (cDP.getModAnodeVoltage() == null) {
                    unknown++;
                } else if (cDP.getModAnodeVoltage().doubleValue() > 0) {
                    if (cDP.getCryomoduleType().equals(CryomoduleType.C100)
                            || cDP.getCryomoduleType().equals(CryomoduleType.C50)
                            || cDP.getCryomoduleType().equals(CryomoduleType.C50T)
                            || cDP.getCryomoduleType().equals(CryomoduleType.C75)
                            || cDP.getCryomoduleType().equals(CryomoduleType.F100)
                            || cDP.getCryomoduleType().equals(CryomoduleType.C25)) {
                        CMType = cDP.getCryomoduleType().toString();
                        byCMType.put(CMType, byCMType.get(CMType).add(new BigDecimal(1)));
                        total++;
                    }
                }

            }
            byCMType.put(LinacName.Total.toString(), new BigDecimal(total));
            byCMType.put("Unknown", new BigDecimal(unknown));
            data.put(date, byCMType);
        }
        return data;
    }

    public SortedMap<Date, SortedMap<String, BigDecimal>> getBypassedCountByCMType() {

        // We want C25, C50, C100, Total, Unknown.  Compare as strings unless both are C*.  Then compare the number.
        SortedMap<Date, SortedMap<String, BigDecimal>> data = new TreeMap<>();

        SortedMap<String, BigDecimal> byCMType;
        int total;
        int unknown;
        String CMType;

        for (Date date : (Set<Date>) dataSpan.keySet()) {
            byCMType = new TreeMap<>(new Comparator<String>() {
                @Override
                public int compare(String o1, String o2) {
                    if (o1.startsWith("C)") && o2.startsWith("C")) {
                        int i1 = Integer.parseInt(o1.substring(1));
                        int i2 = Integer.parseInt(o2.substring(1));
                        return i1 - i2;
                    }
                    return o1.compareTo(o2);
                }
            });
            byCMType.put(CryomoduleType.C25.toString(), new BigDecimal(0));
            byCMType.put(CryomoduleType.C50.toString(), new BigDecimal(0));
            byCMType.put(CryomoduleType.C100.toString(), new BigDecimal(0));
            byCMType.put(CryomoduleType.C50T.toString(), new BigDecimal(0));
            byCMType.put(CryomoduleType.C75.toString(), new BigDecimal(0));
            byCMType.put(CryomoduleType.F100.toString(), new BigDecimal(0));
            total = 0;
            unknown = 0;

            for (CavityResponse cDP : (Set<CavityResponse>) dataSpan.get(date)) {
                if (cDP.getGset() == null) {
                    unknown++;
                } else if (cDP.getGset().doubleValue() == 0) {
                    if (cDP.getCryomoduleType().equals(CryomoduleType.C100)
                            || cDP.getCryomoduleType().equals(CryomoduleType.C50)
                            || cDP.getCryomoduleType().equals(CryomoduleType.C50T)
                            || cDP.getCryomoduleType().equals(CryomoduleType.C75)
                            || cDP.getCryomoduleType().equals(CryomoduleType.F100)
                            || cDP.getCryomoduleType().equals(CryomoduleType.C25)) {
                        CMType = cDP.getCryomoduleType().toString();
                        byCMType.put(CMType, byCMType.get(CMType).add(new BigDecimal(1)));
                        total++;
                    }
                }
            }
            byCMType.put(LinacName.Total.toString(), new BigDecimal(total));
            byCMType.put("Unknown", new BigDecimal(unknown));
            data.put(date, byCMType);
        }
        return data;
    }

    public SortedMap<Date, SortedMap<String, BigDecimal>> getBypassedCountByLinac() {
        SortedMap<Date, SortedMap<String, BigDecimal>> data = new TreeMap<>();

        SortedMap<String, BigDecimal> byLinac;
        int total;
        int unknown;
        for (Date date : (Set<Date>) dataSpan.keySet()) {
            byLinac = new TreeMap<>();
            byLinac.put(LinacName.Injector.toString(), new BigDecimal(0));
            byLinac.put(LinacName.North.toString(), new BigDecimal(0));
            byLinac.put(LinacName.South.toString(), new BigDecimal(0));
            total = 0;
            unknown = 0;

            for (CavityResponse cr : (Set<CavityResponse>) dataSpan.get(date)) {
                if (cr.getGset() == null) {
                    unknown++;
                } else if (cr.getGset().doubleValue() == 0) {
                    byLinac.put(cr.getLinacName().toString(), byLinac.get(cr.getLinacName().toString()).add(new BigDecimal(1)));
                    total++;
                }
            }
            byLinac.put(LinacName.Total.toString(), new BigDecimal(total));
            data.put(date, byLinac);
            byLinac.put("Unknown", new BigDecimal(unknown));
        }
        return data;

    }

    /**
     * Iterate through the dataSpan. For each date, calculate the energy gain of
     * each zone.
     *
     * @param zones List of zone for which to return data. Return data on all
     * zones if null.
     * @return a map, keyed on date, where each date is a map of zone to energy
     * gain
     */
    public SortedMap<Date, SortedMap<String, BigDecimal>> getEnergyGainByZone(List<String> zones) {
        SortedMap<Date, SortedMap<String, BigDecimal>> out = new TreeMap<>();
        SortedMap<String, BigDecimal> byZone;

        for (Date date : dataSpan.keySet()) {
            byZone = new TreeMap<>();
            for (CavityResponse cr : dataSpan.get(date)) {
                String zone = cr.getZoneName();

                if (zones == null || zones.isEmpty() || zones.contains(zone)) {
                    if (!byZone.containsKey(zone)) {
                        byZone.put(zone, BigDecimal.ZERO);
                    }

                    // GSET could be null if the control system was down, but the CED _should_ always have a length.
                    BigDecimal gset = cr.getGset();
                    BigDecimal cavEGain;
                    if (gset == null) {
                        cavEGain = BigDecimal.ZERO;
                    } else {
                        cavEGain = gset.multiply(cr.getLength());
                    }
                    byZone.put(zone, byZone.get(zone).add(cavEGain));
                }
            }
            out.put(date, byZone);
        }

        return out;
    }

    /**
     * Iterate through the dataSpan. For each date, calculate the energy gain of
     * each Cryomodule type.
     *
     * @param zones List of zone for which to return data. Return data on all
     * zones if null.
     * @return a map, keyed on date, where each date is a map of zone to energy
     * gain
     */
    public SortedMap<Date, SortedMap<String, BigDecimal>> getEnergyGainByCMType(List<String> zones) {
        SortedMap<Date, SortedMap<String, BigDecimal>> out = new TreeMap<>();
        SortedMap<String, BigDecimal> byCMType;

        for (Date date : dataSpan.keySet()) {
            byCMType = new TreeMap<>();
            for (CavityResponse cr : dataSpan.get(date)) {
                String zone = cr.getZoneName();
                String cmType = cr.getCryomoduleType().toString();

                if (zones == null || zones.isEmpty() || zones.contains(zone)) {
                    if (!byCMType.containsKey(cmType)) {
                        byCMType.put(cmType, BigDecimal.ZERO);
                    }

                    // GSET could be null if the control system was down, but the CED _should_ always have a length.
                    BigDecimal gset = cr.getGset();
                    BigDecimal cavEGain;
                    if (gset == null) {
                        cavEGain = BigDecimal.ZERO;
                    } else {
                        cavEGain = gset.multiply(cr.getLength());
                    }
                    byCMType.put(cmType, byCMType.get(cmType).add(cavEGain));
                }
            }
            out.put(date, byCMType);
        }

        return out;
    }

    /**
     * Iterate through the dataSpan. For each date, calculate the energy gain of
     * each cavity.
     *
     * @param zones List of zone for which to return data. Return data on all
     * zones if null.
     * @return a map, keyed on date, where each date is a map of zone to energy
     * gain
     */
    public SortedMap<Date, SortedMap<String, BigDecimal>> getEnergyGainByCavity(List<String> zones) {
        SortedMap<Date, SortedMap<String, BigDecimal>> out = new TreeMap<>();
        SortedMap<String, BigDecimal> byCavity;

        for (Date date : dataSpan.keySet()) {
            byCavity = new TreeMap<>();
            for (CavityResponse cr : dataSpan.get(date)) {
                String zone = cr.getZoneName();
                String cavity = cr.getCavityName();

                if (zones == null || zones.isEmpty() || zones.contains(zone)) {
                    if (!byCavity.containsKey(cavity)) {
                        byCavity.put(cavity, BigDecimal.ZERO);
                    }

                    // GSET could be null if the control system was down, but the CED _should_ always have a length.
                    BigDecimal gset = cr.getGset();
                    BigDecimal cavEGain;
                    if (gset == null) {
                        cavEGain = BigDecimal.ZERO;
                    } else {
                        cavEGain = gset.multiply(cr.getLength());
                    }
                    byCavity.put(cavity, byCavity.get(cavity).add(cavEGain));
                }
            }
            out.put(date, byCavity);
        }

        return out;
    }
}
