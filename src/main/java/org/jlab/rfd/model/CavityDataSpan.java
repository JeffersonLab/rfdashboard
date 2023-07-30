/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.model;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
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
            if (dataSpan.get(d) != null) {
                for (CavityDataPoint dp : dataSpan.get(d)) {
                    cavities.add(dp.toJson());
                }
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
    public SortedMap<Date, SortedMap<String, BigDecimal>> getModAnodeCountByLinac(boolean includeInjector) {
        SortedMap<Date, SortedMap<String, BigDecimal>> data = new TreeMap<>();

        SortedMap<String, BigDecimal> byLinac;
        int total;
        int unknown;
        for (Date date : (Set<Date>) dataSpan.keySet()) {
            byLinac = new TreeMap<>();
            if (includeInjector) {
                byLinac.put(LinacName.Injector.toString(), new BigDecimal(0));
            }
            byLinac.put(LinacName.North.toString(), new BigDecimal(0));
            byLinac.put(LinacName.South.toString(), new BigDecimal(0));
            total = 0;
            unknown = 0;

            for (CavityResponse cDP : dataSpan.get(date)) {
                if (!includeInjector && cDP.getLinacName() == LinacName.Injector) {
                    continue;
                }
                if (cDP.getModAnodeVoltage() == null) {
                    unknown++;
                } else if (cDP.getModAnodeVoltage().doubleValue() > 0) {
                    byLinac.put(cDP.getLinacName().toString(), byLinac.get(cDP.getLinacName().toString()).add(new BigDecimal(1)));
                    total++;
                }

            }
            byLinac.put(LinacName.Total.toString(), new BigDecimal(total));
            if (unknown > 0) {
                byLinac.put("Unknown", new BigDecimal(unknown));
            }
            data.put(date, byLinac);
        }
        return data;
    }

    public SortedMap<Date, SortedMap<String, BigDecimal>> getModAnodeCountByCMType(Map<String, String> typeMapper) {
        SortedMap<Date, SortedMap<String, BigDecimal>> data = new TreeMap<>();

        if (typeMapper == null) {
            typeMapper = new HashMap<>();
            typeMapper.put(CryomoduleType.C100.toString(), CryomoduleType.C100.toString());
            typeMapper.put(CryomoduleType.F100.toString(), CryomoduleType.C100.toString());
            typeMapper.put(CryomoduleType.C25.toString(), CryomoduleType.C25.toString());
            typeMapper.put(CryomoduleType.C50.toString(), CryomoduleType.C50.toString());
            typeMapper.put(CryomoduleType.C50T.toString(), CryomoduleType.C50.toString());
            typeMapper.put(CryomoduleType.C75.toString(), CryomoduleType.C75.toString());
        }

        SortedMap<String, BigDecimal> byCMType;
        int total;
        int unknown;
        String CMType;

        for (Date date : dataSpan.keySet()) {
            byCMType = new TreeMap<>();
            byCMType.put(typeMapper.get(CryomoduleType.C100.toString()), new BigDecimal(0));
            byCMType.put(typeMapper.get(CryomoduleType.C50.toString()), new BigDecimal(0));
            byCMType.put(typeMapper.get(CryomoduleType.C25.toString()), new BigDecimal(0));
            byCMType.put(typeMapper.get(CryomoduleType.F100.toString()), new BigDecimal(0));
            byCMType.put(typeMapper.get(CryomoduleType.C75.toString()), new BigDecimal(0));
            byCMType.put(typeMapper.get(CryomoduleType.C50T.toString()), new BigDecimal(0));
            total = 0;
            unknown = 0;

            for (CavityResponse cDP : dataSpan.get(date)) {

                if (cDP.getModAnodeVoltage() == null) {
                    unknown++;
                } else if (cDP.getModAnodeVoltage().doubleValue() > 0) {
                    if (cDP.getCryomoduleType().equals(CryomoduleType.C100)
                            || cDP.getCryomoduleType().equals(CryomoduleType.C50)
                            || cDP.getCryomoduleType().equals(CryomoduleType.C50T)
                            || cDP.getCryomoduleType().equals(CryomoduleType.C75)
                            || cDP.getCryomoduleType().equals(CryomoduleType.F100)
                            || cDP.getCryomoduleType().equals(CryomoduleType.C25)) {
                        CMType = typeMapper.get(cDP.getCryomoduleType().toString());
                        byCMType.put(CMType, byCMType.get(CMType).add(new BigDecimal(1)));
                        total++;
                    }
                }

            }
            byCMType.put(LinacName.Total.toString(), new BigDecimal(total));
            if (unknown > 0) {
                byCMType.put("Unknown", new BigDecimal(unknown));
            }
            data.put(date, byCMType);
        }
        return data;
    }

    public SortedMap<Date, SortedMap<String, BigDecimal>> getBypassedCountByCMType(Map<String, String> typeMapper){
        return getBypassedCountByCMType(typeMapper, false);
    }

    public SortedMap<Date, SortedMap<String, BigDecimal>> getBypassedCountByCMType(Map<String, String> typeMapper,
                                                                                   boolean includeInjector) {

        if (typeMapper == null) {
            typeMapper = new HashMap<>();
            typeMapper.put(CryomoduleType.C100.toString(), CryomoduleType.C100.toString());
            typeMapper.put(CryomoduleType.F100.toString(), CryomoduleType.C100.toString());
            typeMapper.put(CryomoduleType.C25.toString(), CryomoduleType.C25.toString());
            typeMapper.put(CryomoduleType.C50.toString(), CryomoduleType.C50.toString());
            typeMapper.put(CryomoduleType.C50T.toString(), CryomoduleType.C50.toString());
            typeMapper.put(CryomoduleType.C75.toString(), CryomoduleType.C75.toString());
        }

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
            byCMType.put(typeMapper.get(CryomoduleType.C100.toString()), new BigDecimal(0));
            byCMType.put(typeMapper.get(CryomoduleType.C50.toString()), new BigDecimal(0));
            byCMType.put(typeMapper.get(CryomoduleType.C25.toString()), new BigDecimal(0));
            byCMType.put(typeMapper.get(CryomoduleType.F100.toString()), new BigDecimal(0));
            byCMType.put(typeMapper.get(CryomoduleType.C75.toString()), new BigDecimal(0));
            byCMType.put(typeMapper.get(CryomoduleType.C50T.toString()), new BigDecimal(0));
            total = 0;
            unknown = 0;

            int bypassed;
            for (CavityResponse cDP : dataSpan.get(date)) {
                if (!includeInjector && cDP.getLinacName() == LinacName.Injector) {
                    continue;
                }
                if (cDP.getCryomoduleType().equals(CryomoduleType.C100)
                        || cDP.getCryomoduleType().equals(CryomoduleType.C50)
                        || cDP.getCryomoduleType().equals(CryomoduleType.C50T)
                        || cDP.getCryomoduleType().equals(CryomoduleType.C75)
                        || cDP.getCryomoduleType().equals(CryomoduleType.F100)
                        || cDP.getCryomoduleType().equals(CryomoduleType.C25)) {

                    bypassed = checkCavityBypassed(cDP);
                    switch (bypassed) {
                        case 1:
                            CMType = cDP.getCryomoduleType().toString();
                            byCMType.put(typeMapper.get(CMType), byCMType.get(typeMapper.get(CMType)).add(new BigDecimal(1)));
                            total++;
                            break;
                        case 0:
                            break;
                        case -1:
                            unknown++;
                            break;
                        default:
                            throw new RuntimeException("Unexpected response from checkCavityBypassed");
                    }
                }
            }

            byCMType.put(LinacName.Total.toString(), new BigDecimal(total));
            if (unknown > 0) {
                byCMType.put("Unknown", new BigDecimal(unknown));
            }
            data.put(date, byCMType);
        }
        return data;
    }

    public SortedMap<Date, SortedMap<String, BigDecimal>> getBypassedCountByLinac() {
        return getBypassedCountByLinac(false);
    }


    public SortedMap<Date, SortedMap<String, BigDecimal>> getBypassedCountByLinac(boolean includeInjector) {
        SortedMap<Date, SortedMap<String, BigDecimal>> data = new TreeMap<>();

        SortedMap<String, BigDecimal> byLinac;
        int total;
        int unknown;
        for (Date date : dataSpan.keySet()) {
            byLinac = new TreeMap<>();
            if (includeInjector) {
                byLinac.put(LinacName.Injector.toString(), new BigDecimal(0));
            }
            byLinac.put(LinacName.North.toString(), new BigDecimal(0));
            byLinac.put(LinacName.South.toString(), new BigDecimal(0));
            total = 0;
            unknown = 0;

            int bypassed;
            for (CavityResponse cr : dataSpan.get(date)) {
                if (!includeInjector && cr.getLinacName() == LinacName.Injector) {
                    continue;
                }
                bypassed = checkCavityBypassed(cr);
                switch(bypassed) {
                    case 1:
                        byLinac.put(cr.getLinacName().toString(), byLinac.get(cr.getLinacName().toString()).add(new BigDecimal(1)));
                        total++;
                        break;
                    case 0:
                        break;
                    case -1:
                        unknown++;
                        break;
                    default:
                        throw new RuntimeException("Unexpected response from checkCavityBypassed");
                }
            }
            byLinac.put(LinacName.Total.toString(), new BigDecimal(total));
            data.put(date, byLinac);
            if (unknown > 0) {
                byLinac.put("Unknown", new BigDecimal(unknown));
            }
        }
        return data;

    }

    /**
     * Check if a cavity is bypassed.  Return -1 if we can't tell for sure ('unknown')
     * @param cr The cavity to check
     * @return 1 if bypassed, 0 if not, -1 if we can't tell conclusively
     */
    private int checkCavityBypassed(CavityResponse cr) {
        // There are two ways to be bypassed.  GSET is put to 0 only, or CED parameter Bypassed is set to true
        int bypassed = 0;
        if (cr.isBypassed()) {
            bypassed  = 1;
        } else if (cr.getGset() != null && cr.getGset().doubleValue() == 0) {
            bypassed = 1;
        }

        // Update counts.  If we were bypassed explicitly, but GSET is null then we don't know for certain if we
        // were actually bypassed.
        if (bypassed == 1) {
            return 1;
        } else if (cr.getGset() == null) {
            return -1;
        } else {
            return 0;
        }
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
    public SortedMap<Date, SortedMap<String, BigDecimal>> getEnergyGainByCMType(List<String> zones, Map<String, String> typeMapper) {
        if (typeMapper == null) {
            typeMapper = new HashMap<>();
            typeMapper.put("C100", "C100");
            typeMapper.put("F100", "F100");
            typeMapper.put("C50", "C50");
            typeMapper.put("C50T", "C50T");
            typeMapper.put("C25", "C25");
            typeMapper.put("C75", "C75");
            typeMapper.put("QTR", "QTR");
        }
        SortedMap<Date, SortedMap<String, BigDecimal>> out = new TreeMap<>();
        SortedMap<String, BigDecimal> byCMType;

        for (Date date : dataSpan.keySet()) {
            byCMType = new TreeMap<>();
            for (CavityResponse cr : dataSpan.get(date)) {
                String zone = cr.getZoneName();
                String cmType = cr.getCryomoduleType().toString();

                if (zones == null || zones.isEmpty() || zones.contains(zone)) {
                    if (!byCMType.containsKey(cmType)) {
                        byCMType.put(typeMapper.get(cmType), BigDecimal.ZERO);
                    }

                    // GSET could be null if the control system was down, but the CED _should_ always have a length.
                    BigDecimal gset = cr.getGset();
                    BigDecimal cavEGain;
                    if (gset == null) {
                        cavEGain = BigDecimal.ZERO;
                    } else {
                        cavEGain = gset.multiply(cr.getLength());
                    }
                    byCMType.put(typeMapper.get(cmType), byCMType.get(typeMapper.get(cmType)).add(cavEGain));
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
