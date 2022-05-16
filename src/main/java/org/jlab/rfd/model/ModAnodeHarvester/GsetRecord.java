/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.model.ModAnodeHarvester;

import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author adamc
 */
public class GsetRecord {
    private final Date timestamp;
    private final Date epicsDate;
    private final String epicsName;
    private final BigDecimal modAnodeVoltage;
    private final BigDecimal energy;
    private final BigDecimal gset;
    private final BigDecimal gsetNoMav;
    
    /**
     * Construct a CavityGsetPoint to represent a Cavity GSET data point from the ModAnodeHarvester.
     * @param timestamp The time of the scan
     * @param epicsDate The date of the recreated EPICS control system (see CSUE's LEMSim for more information)
     * @param modAnodeVoltage The value of the CED parameter ModAnode for this Cavity at the time of the scan
     * @param epicsName The EPICS name of the cavity
     * @param energy The energy (MeV) used in the LEM calculations
     * @param gset The cavity GSET assigned by LEM when using all original CED ModAnode values
     * @param gsetNoMav The cavity GSET assigned by LEM when using all zero CED ModAnode values
     */
    public GsetRecord(Date timestamp, Date epicsDate, BigDecimal modAnodeVoltage, String epicsName,
            BigDecimal energy, BigDecimal gset, BigDecimal gsetNoMav) {

        this.timestamp = timestamp;
        this.epicsDate = epicsDate;
        this.modAnodeVoltage = modAnodeVoltage;
        this.epicsName = epicsName;
        this.energy = energy;
        this.gset = gset;
        this.gsetNoMav = gsetNoMav;
    }

    public BigDecimal getEnergy() {
        return energy;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Date getEpicsDate() {
        return epicsDate;
    }

    public String getEpicsName() {
        return epicsName;
    }

    public BigDecimal getModAnodeVoltage() {
        return modAnodeVoltage;
    }

    public BigDecimal getGset() {
        return gset;
    }

    public BigDecimal getGsetNoMav() {
        return gsetNoMav;
    }
    
}
