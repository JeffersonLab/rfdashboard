/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.model.ModAnodeHarvester;

import java.math.BigDecimal;
import java.util.Date;

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