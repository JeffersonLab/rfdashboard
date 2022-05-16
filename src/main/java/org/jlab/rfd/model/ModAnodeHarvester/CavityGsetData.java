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

/**
 * This class represents the data associated with a single cavity from a single
 * ModAnodeHarvester scan.
 *
 * @author adamc
 */
public class CavityGsetData {

    private static final Logger LOGGER = Logger.getLogger(CavityGsetData.class.getName());

    private final String epicsName;
    private final Date epicsDate;
    private final BigDecimal gset1050;
    private final BigDecimal gset1090;
    private final BigDecimal gsetNoMav1050;
    private final BigDecimal gsetNoMav1090;
    private final BigDecimal modAnodeVoltage;

    public CavityGsetData(GsetRecord r1050, GsetRecord r1090) {
        String errString = "Error querying data";
        if (r1050 == null || r1090 == null) {
            LOGGER.log(Level.SEVERE, "Received null GsetRecord");
            throw new RuntimeException(errString);
        }
        // If we got valid runs at both energy levels, then make sure that both have the same Mod Anode Voltages
        if (r1050.getModAnodeVoltage() != null && r1090.getModAnodeVoltage() != null) {
            if (!r1050.getModAnodeVoltage().equals(r1090.getModAnodeVoltage())) {
                LOGGER.log(Level.SEVERE, "GsetRecords for cavity {0} from {1} do not have the same mod anode voltage", new Object[]{r1050.getEpicsName(), r1050.getTimestamp()});
                throw new RuntimeException(errString);
            }
        }
        if (!r1050.getEpicsDate().equals(r1090.getEpicsDate())) {
            LOGGER.log(Level.SEVERE, "GsetRecords do not use the same EPICS date");
            throw new RuntimeException(errString);
        }
        if (!r1050.getTimestamp().equals(r1090.getTimestamp())) {
            LOGGER.log(Level.SEVERE, "GsetRecords do not have the same timestamp");
            throw new RuntimeException(errString);
        }
        if (!r1050.getEpicsName().equals(r1090.getEpicsName())) {
            LOGGER.log(Level.SEVERE, "GsetRecords do not have the same EPICS Name");
            throw new RuntimeException(errString);
        }
        this.epicsName = r1050.getEpicsName();
        this.epicsDate = r1050.getEpicsDate();
        this.gset1050 = r1050.getGset();
        this.gset1090 = r1090.getGset();
        this.gsetNoMav1050 = r1050.getGsetNoMav();
        this.gsetNoMav1090 = r1090.getGsetNoMav();
        this.modAnodeVoltage = r1050.getModAnodeVoltage();
    }

    // Turn this into a JSON object that can be easily output
    public JsonObject toJson() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        DecimalFormat gDF = new DecimalFormat("#.#####");
        DecimalFormat mDF = new DecimalFormat("#.###");
        JsonObjectBuilder job = Json.createObjectBuilder();
        job.add("epicsName", epicsName)
                .add("epicsDate", sdf.format(epicsDate))
                .add("modAnodeVoltage_kv", modAnodeVoltage == null ? "" : mDF.format(modAnodeVoltage.doubleValue()))
                .add("gset1050", gset1050 == null ? "" : gDF.format(gset1050))
                .add("gset1090", gset1090 == null ? "" : gDF.format(gset1090))
                .add("gsetNoMav1050", gsetNoMav1050 == null ? "" : gDF.format(gsetNoMav1050))
                .add("gsetNoMav1090", gsetNoMav1090 == null ? "" : gDF.format(gsetNoMav1090));
        return job.build();
    }

    public String getEpicsName() {
        return epicsName;
    }

    public Date getEpicsDate() {
        return epicsDate;
    }

    public BigDecimal getGset1050() {
        return gset1050;
    }

    public BigDecimal getGset1090() {
        return gset1090;
    }

    public BigDecimal getGsetNoMav1050() {
        return gsetNoMav1050;
    }

    public BigDecimal getGsetNoMav1090() {
        return gsetNoMav1090;
    }

    public BigDecimal getModAnodeVoltage() {
        return modAnodeVoltage;
    }
}
