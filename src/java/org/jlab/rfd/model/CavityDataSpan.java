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

    private final TreeMap<Date, Set<CavityDataPoint>> dataSpan;

    public CavityDataSpan() {
        dataSpan = new TreeMap<>();
    }

    public int size() {
        return dataSpan.size();
    }
    
    /**
     * Returns the set of CavityDataPoints for a given date
     * @param date The requested date
     * @return A set of CavityDataPoints relating to the requested date
     */
    public Set<CavityDataPoint> get(Date date) {
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
    public Object add(CavityDataPoint dataPoint) {
        if (!dataSpan.containsKey(dataPoint.getTimestamp())) {
            dataSpan.put(dataPoint.getTimestamp(), new HashSet<>());
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
    public Object put(Date timestamp, Set<CavityDataPoint> dataSet) {

        return dataSpan.put(timestamp, dataSet);
    }

    public JsonObject toJson() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        JsonArrayBuilder data = Json.createArrayBuilder();
        for ( Date d : dataSpan.keySet() ) {
            JsonObjectBuilder sample = Json.createObjectBuilder();
            sample.add("date", sdf.format(d));
            
            JsonArrayBuilder cavities = Json.createArrayBuilder();
            for (CavityDataPoint dp : dataSpan.get(d)) {
                JsonObjectBuilder cavBuilder = Json.createObjectBuilder();
                cavBuilder.add("name", dp.getCavityName()).add("linac", dp.getLinacName().toString());
                // The json builder throws an exception on Null or Double.NaN.  This seemed like the smartest way to handle it.
                if (dp.getGset() != null) {
                    cavBuilder.add("gset", dp.getGset().doubleValue());
                } else {
                    cavBuilder.add("gset", "");
                }
                if (dp.getModAnodeVoltage() != null) {
                    cavBuilder.add("modAnodeVoltage_kv", dp.getModAnodeVoltage().doubleValue());
                } else {
                    cavBuilder.add("modAnodeVoltage_kv", "");
                }
                if (dp.getOdvh() != null) {
                    cavBuilder.add("odvh", dp.getOdvh().doubleValue());
                } else {
                    cavBuilder.add("odvh", "");
                }
                if (dp.getQ0() != null) {
                    cavBuilder.add("q0", dp.getQ0());
                } else {
                    cavBuilder.add("q0", "");
                }

                if (dp.getqExternal() != null) {
                    cavBuilder.add("qExternal", dp.getqExternal());
                } else {
                    cavBuilder.add("qExternal", "");
                }
                if (dp.getMaxGset() != null) {
                    cavBuilder.add("maxGset", dp.getMaxGset().doubleValue());
                } else {
                    cavBuilder.add("maxGset", "");
                }
                if (dp.getOpsGsetMax() != null) {
                    cavBuilder.add("opsGsetMax", dp.getOpsGsetMax().doubleValue());
                } else {
                    cavBuilder.add("opsGsetMax", "");
                }
                if (dp.getTripOffset() != null) {
                    cavBuilder.add("tripOffset", dp.getTripOffset().doubleValue());
                } else {
                    cavBuilder.add("tripOffset", "");
                }
                if (dp.getTripSlope() != null) {
                    cavBuilder.add("tripSlope", dp.getTripSlope().doubleValue());
                } else {
                    cavBuilder.add("tripSlope", "");
                }
                if (dp.getLength() != null) {
                    cavBuilder.add("length", dp.getLength().doubleValue());
                } else {
                    cavBuilder.add("length", "");
                }
                cavBuilder.add("bypassed", dp.isBypassed());
                cavBuilder.add("tunerBad", dp.isTunerBad());
                cavBuilder.add("comments", dp.getComments());
                

                // Some of these will have ModAnodeHarvester data, but definitely not Injector cavities
                if (dp.getModAnodeHarvesterGsetData() != null) {
                    cavBuilder.add("modAnodeHarvester", dp.getModAnodeHarvesterGsetData().toJson());
                }

                // Add the last couple of properties, build the builder, and add it to the cavities object
                cavities.add(cavBuilder
                        .add("moduleType", dp.getCryomoduleType().toString())
                        .add("epicsName", dp.getEpicsName()).build());
            }
            sample.add("cavities", cavities.build());
            data.add(sample.build());
        }

    JsonObject out = Json.createObjectBuilder().add("data", data.build()).build();
    return out ;
}
    
    /**
     * This returns a TreeMap keyed on date with values being the count of cavities with non-zero modAnodeVoltage by linac
     * on that date.
     * 
     * Converts the enum linac names to strings so that it can easily be handled by formatter classes
     * @return
     */
    public SortedMap<Date, SortedMap<String, BigDecimal>> getModAnodeCountByLinac() {
        SortedMap<Date, SortedMap<String, BigDecimal>> data = new TreeMap<>();
        
        SortedMap<String, BigDecimal> byLinac;
        int total;
        int unknown;
        for ( Date date : (Set<Date>) dataSpan.keySet() ) {
            byLinac = new TreeMap<>();
            byLinac.put(LinacName.Injector.toString(), new BigDecimal(0));
            byLinac.put(LinacName.North.toString(), new BigDecimal(0));
            byLinac.put(LinacName.South.toString(), new BigDecimal(0));
            total = 0;
            unknown = 0;
            
            for (CavityDataPoint cDP : (Set<CavityDataPoint>) dataSpan.get(date)) {
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

        for ( Date date : (Set<Date>) dataSpan.keySet() ) {
            byCMType = new TreeMap<>();
            byCMType.put(CryomoduleType.C100.toString(), new BigDecimal(0));
            byCMType.put(CryomoduleType.C50.toString(), new BigDecimal(0));
            byCMType.put(CryomoduleType.C25.toString(), new BigDecimal(0));
            total = 0;
            unknown = 0;
            
            for (CavityDataPoint cDP : (Set<CavityDataPoint>) dataSpan.get(date)) {
                
                if (cDP.getModAnodeVoltage() == null) {
                    unknown++;
                } else if (cDP.getModAnodeVoltage().doubleValue() > 0) {
                    if (cDP.getCryomoduleType().equals(CryomoduleType.C100)
                            || cDP.getCryomoduleType().equals(CryomoduleType.C50)
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
            total = 0;
            unknown = 0;

            for (CavityDataPoint cDP : (Set<CavityDataPoint>) dataSpan.get(date)) {
                if (cDP.getGset() == null) {
                    unknown++;
                } else if (cDP.getGset().doubleValue() == 0) {
                    if (cDP.getCryomoduleType().equals(CryomoduleType.C100)
                            || cDP.getCryomoduleType().equals(CryomoduleType.C50)
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
        for ( Date date : (Set<Date>) dataSpan.keySet() ) {
            byLinac = new TreeMap<>();
            byLinac.put(LinacName.Injector.toString(), new BigDecimal(0));
            byLinac.put(LinacName.North.toString(), new BigDecimal(0));
            byLinac.put(LinacName.South.toString(), new BigDecimal(0));
            total = 0;
            unknown = 0;

            for (CavityDataPoint cDP : (Set<CavityDataPoint>) dataSpan.get(date)) {
                if (cDP.getGset() == null) {
                    unknown++;
                } else if (cDP.getGset().doubleValue() == 0) {
                    byLinac.put(cDP.getLinacName().toString(), byLinac.get(cDP.getLinacName().toString()).add(new BigDecimal(1)));
                    total++;
                }
            }
            byLinac.put(LinacName.Total.toString(), new BigDecimal(total));
            data.put(date, byLinac);
            byLinac.put("Unknown", new BigDecimal(unknown));
        }
        return data;
       
    }
}
