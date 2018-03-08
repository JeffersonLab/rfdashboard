/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.jlab.rfd.business.util.DateUtil;

/**
 *
 * @author adamc
 */
public class LemRecord {

    private static final Logger LOGGER = Logger.getLogger(LemRecord.class.getName());

    private final long scanId;
    private final ArrayList<BigDecimal> tripRates;
    private final Date timestamp;
    private final LinacName linac;

    // Scan energies in MeV - may not be needed
    private static final List<Integer> ENERGY;

    static {
        List<Integer> temp = new ArrayList<>();
        for (int i = 1000; i <= 1190; i = i + 5) {
            temp.add(i);
        }
        ENERGY = Collections.unmodifiableList(temp);
    }

    public LemRecord(long scanId, Date timestamp, LinacName linac, ArrayList<BigDecimal> tripRates) {
        this.scanId = scanId;
        this.timestamp = timestamp;
        this.linac = linac;
        this.tripRates = tripRates;
    }

    public List<Integer> getEnergy() {
        return ENERGY;
    }

    public long getScanId() {
        return scanId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public LinacName getLinac() {
        return linac;
    }

    /**
     * @return Unmodifiable list of trip rates
     */
    public List<BigDecimal> getTripRates() {
        return Collections.unmodifiableList(tripRates);
    }

    /**
     * Generate a JSON object that represents this object
     *
     * @return A JsonObject representing the LemRecord
     */
    public JsonObject toJson() {

        JsonObjectBuilder trBuilder = Json.createObjectBuilder();
        for (int i = 0; i < ENERGY.size(); i++) {
            if (tripRates.get(i) != null) {
                trBuilder.add(ENERGY.get(i).toString(), tripRates.get(i).toPlainString());
            } else {
                trBuilder.add(ENERGY.get(i).toString(), "");
            }
        }
        JsonObject tr = trBuilder.build();

        JsonObject job = Json.createObjectBuilder()
                .add("scanId", scanId)
                .add("timestamp", DateUtil.formatDateYMDHMS(timestamp))
                .add("linac", linac.toString())
                .add("tripRates", tr)
                .build();

        return job;
    }
}
