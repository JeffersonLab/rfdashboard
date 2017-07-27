/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.model.ModAnodeHarvester;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import java.util.logging.Logger;
import org.jlab.rfd.model.LinacName;

/**
 * This class represents the data associated with a single cavity from a single
 * ModAnodeHarvester scan.
 *
 * @author adamc
 */
public class LinacData {

    private static final Logger LOGGER = Logger.getLogger(LinacData.class.getName());

    private final LinacName linacName;
    private final Date epicsDate;
    private final BigDecimal trips1050;
    private final BigDecimal trips1090;
    private final BigDecimal tripsNoMav1050;
    private final BigDecimal tripsNoMav1090;

    public LinacData(LinacRecord r1050, LinacRecord r1090) {
        String errString = "Error querying data";
        if (r1050 == null || r1090 == null) {
            LOGGER.log(Level.SEVERE, "Received null GsetRecord");
            throw new RuntimeException(errString);
        }
        if (!r1050.getEpicsDate().equals(r1090.getEpicsDate())) {
            LOGGER.log(Level.SEVERE, "GsetRecords do not use the same EPICS date");
            throw new RuntimeException(errString);
        }
        if (!r1050.getTimestamp().equals(r1090.getTimestamp())) {
            LOGGER.log(Level.SEVERE, "GsetRecords do not have the same timestamp");
            throw new RuntimeException(errString);
        }
        if (!r1050.getLinacName().equals(r1090.getLinacName())) {
            LOGGER.log(Level.SEVERE, "GsetRecords do not have the same EPICS Name");
            throw new RuntimeException(errString);
        }
        this.linacName = r1050.getLinacName();
        this.epicsDate = r1050.getEpicsDate();
        this.trips1050 = r1050.getTrips();
        this.trips1090 = r1090.getTrips();
        this.tripsNoMav1050 = r1050.getTripsNoMav();
        this.tripsNoMav1090 = r1090.getTripsNoMav();
    }

    // Turn this into a JSON object that can be easily output
    public JsonObject toJson() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        DecimalFormat gDF = new DecimalFormat("#.#####");
        DecimalFormat mDF = new DecimalFormat("#.###");
        JsonObjectBuilder job = Json.createObjectBuilder();
        job.add("linacName", linacName.toString())
                .add("epicsDate", sdf.format(epicsDate))
                .add("trips1050", gDF.format(trips1050))
                .add("trips1090", gDF.format(trips1090))
                .add("tripsNoMav1050", gDF.format(tripsNoMav1050))
                .add("tripsNoMav1090", gDF.format(tripsNoMav1090));
        return job.build();
    }

    public LinacName getLinacName() {
        return linacName;
    }

    public Date getEpicsDate() {
        return epicsDate;
    }

    public BigDecimal getGset1050() {
        return trips1050;
    }

    public BigDecimal getGset1090() {
        return trips1090;
    }

    public BigDecimal getTripsNoMav1050() {
        return tripsNoMav1050;
    }

    public BigDecimal getTripsNoMav1090() {
        return tripsNoMav1090;
    }

}
