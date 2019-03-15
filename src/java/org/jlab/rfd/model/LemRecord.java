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
import java.util.Map;
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
    private final Map<Integer, BigDecimal> tripRates;
    private final Date timestamp;
    private final LinacName linac;

    public LemRecord(long scanId, Date timestamp, LinacName linac, Map<Integer, BigDecimal> tripRates) {
        this.scanId = scanId;
        this.timestamp = timestamp;
        this.linac = linac;
        this.tripRates = tripRates;
    }

    public List<Integer> getEnergy() {
        return new ArrayList(tripRates.keySet());
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
    public Map<Integer, BigDecimal> getTripRates() {
        return Collections.unmodifiableMap(tripRates);
    }

    /**
     * Generate a JSON object that represents this object
     *
     * @return A JsonObject representing the LemRecord
     */
    public JsonObject toJson() {

        JsonObjectBuilder trBuilder = Json.createObjectBuilder();
        for(Integer e : tripRates.keySet()) {
            String tr = "";
            if (tripRates.get(e) != null) {
                tr = tripRates.get(e).toPlainString();
            }
            trBuilder.add(e.toString(), tr);
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
