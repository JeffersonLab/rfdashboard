/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.jlab.rfd.business.util.DateUtil;
import org.jlab.rfd.business.util.MathUtil;

/**
 *
 * @author adamc
 */
public class LemSpan {

    private static final Logger LOGGER = Logger.getLogger(LemSpan.class.getName());
    private final TreeMap<Date, SortedMap<LinacName, LemRecord>> dataSpan;

    public LemSpan() {
        dataSpan = new TreeMap<>();
    }

    public int size() {
        return dataSpan.size();
    }

    /**
     * Adds a single data point to the ModAnodeDataSpan. If this is the first
     * datapoint for it's timestamp, the internal TreeMap adds a key/HashSet for
     * that timestamp. Otherwise it is added to the existing HashSet.
     *
     * @param record
     * @return
     */
    public Object add(LemRecord record) {
        if (!dataSpan.containsKey(record.getTimestamp())) {
            dataSpan.put(record.getTimestamp(), new TreeMap<LinacName, LemRecord>());
        }
        return dataSpan.get(record.getTimestamp()).put(record.getLinac(), record);
    }

    /**
     * Adds the list of LemRecords. If entries exist for a given linac/timestamp
     * pair, it is overwritten by the new LemRecord
     *
     * @param recordList
     * @return
     */
    public Object addList(List<LemRecord> recordList) {
        List<LemRecord> replaced = null;
        LemRecord temp;
        for (LemRecord record : recordList) {
            if ((temp = (LemRecord) add(record)) != null) {
                if (replaced == null) {
                    replaced = new ArrayList<>();
                }
                replaced.add(temp);
            }
        }
        return replaced;
    }

    /**
     * This returns a TreeMap keyed on energy (in MeV) valued on a TreeMap keyed
     * on LinacName valued on hourly trip rate.
     *
     * Converts the enum linac names to strings so that it can easily be handled
     * by formatter classes
     *
     * @param timeStamp The time stamp from the dataSpan that is to be fetched
     * @return
     */
    public SortedMap<Integer, SortedMap<String, Double>> getTripRateCurve(Date timeStamp) {

        if (!dataSpan.containsKey(timeStamp)) {
            LOGGER.log(Level.FINEST, "LemSpan does not contain request timestamp - {0}", timeStamp);
            return null;
        }

        SortedMap<Integer, SortedMap<String, Double>> data = new TreeMap<>();

        Map<Integer, Double> north = dataSpan.get(timeStamp).get(LinacName.North).getTripRates();
        Map<Integer, Double> south = dataSpan.get(timeStamp).get(LinacName.South).getTripRates();
        List<Integer> energies = dataSpan.get(timeStamp).get(LinacName.North).getEnergy();
        SortedMap<String, Double> byLinac;

        for (int i = 0; i < energies.size(); i++) {
            byLinac = new TreeMap<>();
            Double total = null;

            byLinac.put(LinacName.North.toString(), north.get(energies.get(i)));
            byLinac.put(LinacName.South.toString(), south.get(energies.get(i)));

            // Null means lem couldn't find valid solution for the linac at that energy or it wasn't scanned.
            if (north.get(energies.get(i)) != null && south.get(energies.get(i)) != null) {
                total = south.get(energies.get(i)) + north.get(energies.get(i));
            }

            byLinac.put("Total", total);
            data.put(energies.get(i), byLinac);
        }
        return data;
    }

    /**
     * This outputs the energy reach data of a LemSpan object into a time
     * series-like format. The outer SortedMap is keyed on the date of the data,
     * the inner SortedMap is keyed on a series name. This object currently has
     * only one series - "Reach."
     *
     * @return
     */
    public SortedMap<Date, SortedMap<String, Double>> getEnergyReach() {

        if (dataSpan.isEmpty()) {
            LOGGER.log(Level.FINEST, "LemSpan does not contain any trip rate data");
            return null;
        }

        SortedMap<Date, SortedMap<String, Double>> data = new TreeMap<>();
        Map<Integer, Double> north;
        Map<Integer, Double> south;
        List<Integer> energy;
        Double[] eight = {8.0};

        // Go through each date in the lem span
        for (Date timestamp : dataSpan.keySet()) {

            SortedMap<String, Double> reach = new TreeMap<>();

            // Grab the trip rates, and the energies for the scans from that date
            north = dataSpan.get(timestamp).get(LinacName.North).getTripRates();
            south = dataSpan.get(timestamp).get(LinacName.South).getTripRates();
            energy = dataSpan.get(timestamp).get(LinacName.North).getEnergy();

            // Make sure that we have scans for both and that they have the same number of entries (different energies imply something weird would have happened)
            if (north != null && south != null && north.size() == south.size()) {
                // Calculate the total trip rate at each energy for that day
                List<Double> total = new ArrayList<>();
                for (int i = 0; i < north.size(); i++) {
                    if (north.get(energy.get(i)) != null && south.get(energy.get(i)) != null) {
                        total.add(north.get(energy.get(i)) + south.get(energy.get(i)));
                    } else {
                        total.add(null);
                    }
                }
                // Convert these lists to arrays for use in our linear interpolation function.  Then convert the resultant array back to list
                // We need to condense this down and remove the nulls.
                List<Double> tempT = new ArrayList<>();
                List<Double> tempE = new ArrayList<>();
                for (int i = 0; i < total.size(); i++) {
                    if (total.get(i) != null) {
                        tempT.add(total.get(i));
                        tempE.add((double) energy.get(i));
                    }
                }
                Double[] totalArray = tempT.toArray(new Double[tempT.size()]);
                Double[] energyArray = tempE.toArray(new Double[tempE.size()]);
                if ((totalArray.length >= 2)
                        && (totalArray[0] < 8)
                        && (totalArray[totalArray.length - 1] > 8)
                        && MathUtil.isSorted(totalArray)) {
                    Double[] temp = MathUtil.interpLinear(totalArray, energyArray, eight);
                    // We're only giving it a single value in the input array, so extract the first element of this vector.
                    // LEM only gives out three decimal places
                    reach.put("Reach", temp[0]);
                }
            } else {
                if (north == null || south == null) {
                    LOGGER.log(Level.WARNING, "One of the linac trip rate vectors for '{0}' is null.  Shouldn't happen.", timestamp.toString());
                } else {
                    LOGGER.log(Level.WARNING, "Linac trip rate vectors for '{0}' are of different lengths.  Shouldn't happen.", timestamp.toString());
                }
                reach.put("Reach", null);
            }
            if (reach.size() > 0) {
                data.put(timestamp, reach);
            }
        }
        return data;
    }

    /**
     * This returns a json object representing the LemSpan. It should have the
     * following format. { "timestamp1" : { North :{ LemRecord.toJson() output
     * }, South :{ LemRecord.toJson() output } }, "timestamp2" : { ... }, ... }
     *
     * @return
     */
    public JsonObject toJson() {
        JsonObjectBuilder job = Json.createObjectBuilder();
        for (Date d : dataSpan.keySet()) {
            SortedMap<LinacName, LemRecord> temp = dataSpan.get(d);
            JsonObjectBuilder jDate = Json.createObjectBuilder();
            for (LinacName linac : temp.keySet()) {
                jDate.add(linac.toString(), temp.get(linac).toJson());
            }
            job.add(DateUtil.formatDateYMDHMS(d), jDate.build());
        }

        return job.build();
    }
}
