/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.model;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeMap;

/**
 *
 * @author adamc
 */
public class CavityDataSpan {

    private final TreeMap<Date, HashSet<CavityDataPoint>> dataSpan;

    public CavityDataSpan() {
        dataSpan = new TreeMap();
    }

    public int size() {
        return dataSpan.size();
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
            dataSpan.put(dataPoint.getTimestamp(), new HashSet());
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
    public Object put(Date timestamp, HashSet<CavityDataPoint> dataSet) {

        return dataSpan.put(timestamp, dataSet);
    }

    /**
     * This returns a TreeMap keyed on date with values being the count of cavities with non-zero modAnodeVoltage by linac
     * on that date.
     * 
     * Converts the enum linac names to strings so that it can easily be handled by formatter classes
     * @return
     */
    public TreeMap<Date, HashMap<String, BigDecimal>> getModAnodeCountByLinac() {
        TreeMap<Date, HashMap<String, BigDecimal>> data = new TreeMap();
        
        HashMap<String, BigDecimal> byLinac;
        int total;
        for ( Date date : (Set<Date>) dataSpan.keySet() ) {
            byLinac = new HashMap();
            byLinac.put(LinacName.Injector.toString(), new BigDecimal(0));
            byLinac.put(LinacName.North.toString(), new BigDecimal(0));
            byLinac.put(LinacName.South.toString(), new BigDecimal(0));
            total = 0;
            
            for (CavityDataPoint cDP : (HashSet<CavityDataPoint>) dataSpan.get(date)) {
                if (cDP.getModAnodeVoltage().doubleValue() > 0) {
                    byLinac.put(cDP.getLinacName().toString(), byLinac.get(cDP.getLinacName().toString()).add(new BigDecimal(1)));
                    total++;
                }
            }
            byLinac.put(LinacName.Total.toString(), new BigDecimal(total));
            data.put(date, byLinac);
        }
        return data;
    }
}
