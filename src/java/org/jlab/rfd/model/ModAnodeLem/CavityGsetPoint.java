/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.model.ModAnodeLem;

import java.math.BigDecimal;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.rfd.business.util.CebafNames;
import org.jlab.rfd.model.CryomoduleType;
import org.jlab.rfd.model.LinacName;

/**
 *
 * @author adamc
 */
public class CavityGsetPoint {
    private static final Logger LOGGER = Logger.getLogger(CavityGsetPoint.class.getName());
    private final Date timestamp;
    private final Date epicsDate;
    private final String cavityName;
    private final String epicsName;
    private final BigDecimal modAnodeVoltage;
    private final CryomoduleType cryomoduleType;
    private final LinacName linacName;
    private final BigDecimal gset;
    private final BigDecimal gsetNoMav;
    
    /**
     * Construct a CavityGsetPoint to represent a Cavity GSET data point from the ModAnodeHarvester.
     * @param timestamp The time of the scan
     * @param epicsDate The date of the recreated EPICS control system (see CSUE's LEMSim for more information)
     * @param cavityName The CED cavity name
     * @param cryomoduleType The CED parameter ModuleType of the parent CryoModule
     * @param modAnodeVoltage The value of the CED parameter ModAnode for this Cavity at the time of the scan
     * @param epicsName The EPICS name of the cavity
     * @param gset The cavity GSET assigned by LEM when using all original CED ModAnode values
     * @param gsetNoMav The cavity GSET assigned by LEM when using all zero CED ModAnode values
     */
    public CavityGsetPoint(Date timestamp, Date epicsDate, String cavityName, CryomoduleType cryomoduleType,
            BigDecimal modAnodeVoltage, String epicsName, BigDecimal gset, BigDecimal gsetNoMav) {

        if (!cavityName.matches("\\dL\\d\\d-\\d")) {
            LOGGER.log(Level.SEVERE, "Improper cavity name format - {0}", cavityName);
            throw new IllegalArgumentException("Invalid cavity name");
        }

        this.timestamp = timestamp;
        this.epicsDate = epicsDate;
        this.cavityName = cavityName;
        this.cryomoduleType = cryomoduleType;
        this.modAnodeVoltage = modAnodeVoltage;
        this.linacName = CebafNames.cedZoneToEnglishLinac(cavityName.substring(0, 4));
        this.epicsName = epicsName;
        this.gset = gset;
        this.gsetNoMav = gsetNoMav;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Date getEpicsDate() {
        return epicsDate;
    }

    public String getCavityName() {
        return cavityName;
    }

    public String getEpicsName() {
        return epicsName;
    }

    public BigDecimal getModAnodeVoltage() {
        return modAnodeVoltage;
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

    public BigDecimal getGsetNoMav() {
        return gsetNoMav;
    }
    
}
