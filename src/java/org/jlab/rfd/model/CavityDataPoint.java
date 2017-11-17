/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.model;

import org.jlab.rfd.model.ModAnodeHarvester.CavityGsetData;
import java.math.BigDecimal;
import java.util.Date;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.jlab.rfd.business.util.CebafNames;

/**
 * This class represents a the available data about a cavity at a specific point
 * in time. This includes MYA data like cavity GSET, CED data like
 * ModAnodeVoltage or CryoModule ModuleType, or custom database data like the
 * ModAnodeHarvester data that shows the difference in trips and GSET with and
 * without any CED ModAnode voltages.
 *
 * @author adamc
 */
public class CavityDataPoint {

    private static final Logger LOGGER = Logger.getLogger(CavityDataPoint.class.getName());

    private final Date timestamp;
    private final String cavityName;
    private final String epicsName;
    private final BigDecimal modAnodeVoltage;
    private final CryomoduleType cryomoduleType;
    private final LinacName linacName;
    private final BigDecimal gset;
    private final CavityGsetData modAnodeHarvesterGsetData;
    private final BigDecimal odvh;
    private final String q0;
    private final String qExternal;
    private final BigDecimal maxGset;
    private final BigDecimal opsGsetMax;
    private final BigDecimal tripOffset;
    private final BigDecimal tripSlope;
    private final BigDecimal length;
    private final boolean bypassed;
    private final boolean tunerBad;
    private final Comment comment;
    
    public CavityDataPoint(Date timestamp, String cavityName, CryomoduleType cryomoduleType,
            BigDecimal modAnodeVoltage, String epicsName, BigDecimal gset, BigDecimal odvh,
            String q0, String qExternal, BigDecimal maxGset, BigDecimal opsGsetMax, BigDecimal tripOffset, BigDecimal tripSlope,
            BigDecimal length, CavityGsetData modAnodeHarvesterGsetData, boolean bypassed, boolean tunerBad, Comment comment) {

        if (!cavityName.matches("\\dL\\d\\d-\\d")) {
            LOGGER.log(Level.SEVERE, "Improper cavity name format - {0}", cavityName);
            throw new IllegalArgumentException("Invalid cavity name");
        }

        this.timestamp = timestamp;
        this.cavityName = cavityName;
        this.cryomoduleType = cryomoduleType;
        this.modAnodeVoltage = modAnodeVoltage;
        this.linacName = CebafNames.cedZoneToEnglishLinac(cavityName.substring(0, 4));
        this.epicsName = epicsName;
        this.gset = gset;
        this.odvh = odvh;
        this.q0 = q0;
        this.qExternal = qExternal;
        this.maxGset = maxGset;
        this.opsGsetMax = opsGsetMax;
        this.tripOffset = tripOffset;
        this.tripSlope = tripSlope;
        this.length = length;
        this.modAnodeHarvesterGsetData = modAnodeHarvesterGsetData;
        this.bypassed = bypassed;
        this.tunerBad = tunerBad;
        this.comment = comment;
    }

    public Comment getComment() {
        return comment;
    }

    public String getQ0() {
        return q0;
    }

    public String getqExternal() {
        return qExternal;
    }

    public BigDecimal getMaxGset() {
        return maxGset;
    }

    public BigDecimal getOpsGsetMax() {
        return opsGsetMax;
    }

    public BigDecimal getTripOffset() {
        return tripOffset;
    }

    public BigDecimal getTripSlope() {
        return tripSlope;
    }

    public BigDecimal getLength() {
        return length;
    }

    public BigDecimal getOdvh() {
        return odvh;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getCavityName() {
        return cavityName;
    }

    public CryomoduleType getCryomoduleType() {
        return cryomoduleType;
    }

    public LinacName getLinacName() {
        return linacName;
    }

    public BigDecimal getGset() {
        return gset;
    }

    public String getEpicsName() {
        return epicsName;
    }

    public BigDecimal getModAnodeVoltage() {
        return modAnodeVoltage;
    }

    public CavityGsetData getModAnodeHarvesterGsetData() {
        return modAnodeHarvesterGsetData;
    }

    public boolean isBypassed() {
        return bypassed;
    }

    public boolean isTunerBad() {
        return tunerBad;
    }

}
