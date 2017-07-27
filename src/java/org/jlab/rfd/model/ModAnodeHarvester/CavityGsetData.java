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
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * This class represents the data associated with a single cavity from a single ModAnodeHarvester scan.
 * @author adamc
 */
public class CavityGsetData {

    private final Date epicsDate;
    private final BigDecimal gset1050;
    private final BigDecimal gset1090;
    private final BigDecimal gsetNoMav1050;
    private final BigDecimal gsetNoMav1090;
    private final BigDecimal mav1050;
    private final BigDecimal mav1090;

    public CavityGsetData(GsetRecord r1050, GsetRecord r1090) {
        if ( r1050 == null || r1090 == null ) {
            throw new RuntimeException("Received null GsetRecord");
        }
        if ( ! r1050.getEpicsDate().equals(r1090.getEpicsDate()) ) {
            throw new RuntimeException("GsetRecords do not use the same EPICS date");
        }
        if ( ! r1050.getTimestamp().equals(r1090.getTimestamp()) ) {
            throw new RuntimeException("GsetRecords do not have the same timestamp");
        }
        if ( ! r1050.getEpicsName().equals(r1090.getEpicsName()) ) {
            throw new RuntimeException("GsetRecords do not have the same EPICS Name");
        }
        this.epicsDate = r1050.getEpicsDate();
        this.gset1050 = r1050.getGset();
        this.gset1090 = r1090.getGset();
        this.gsetNoMav1050 = r1050.getGsetNoMav();
        this.gsetNoMav1090 = r1090.getGsetNoMav();
        this.mav1050 = r1050.getModAnodeVoltage();
        this.mav1090 = r1090.getModAnodeVoltage();
    }

    // Turn this into a JSON object that can be easily output
    public JsonObject toJson () {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        DecimalFormat gDF = new DecimalFormat("#.#####");
        DecimalFormat mDF = new DecimalFormat("#.###");
        JsonObjectBuilder job = Json.createObjectBuilder();
        job.add("epicsDate", sdf.format(epicsDate))
                // Round these to something sensible, 5 decimals
                .add("mav1050_kv", mDF.format(mav1050.doubleValue()))
                .add("mav1090_kv", mDF.format(mav1090))
                .add("gset1050", gDF.format(gset1050))
                .add("gset1090", gDF.format(gset1090))
                .add("gsetNoMav1050", gDF.format(gsetNoMav1050))
                .add("gsetNoMav1090", gDF.format(gsetNoMav1090));
        return job.build();
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

    public BigDecimal getMav1050() {
        return mav1050;
    }

    public BigDecimal getMav1090() {
        return mav1090;
    }

}