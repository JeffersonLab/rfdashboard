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
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.jlab.rfd.business.util.CebafNames;

/**
 * This class represents a the available data about a cavity at a specific point
 * in time. This includes MYA data like cavity GSET, CED data like
 * ModAnodeVoltage or CryoModule ModuleType, or custom database data like the
 * ModAnodeHarvester data that shows the difference in trips and GSET with and
 * without any CED ModAnode voltages. Note: This object contains historical data that can easily be cached and doesn't
 * change once set.  CavityResponse should be used to respond to queries of cavity data, espeically if the response 
 * is a value that needs to vary over a date (since the cache is effectively keyed on date).
 *
 * @author adamc
 */
public class CavityDataPoint implements Cloneable {

    private static final Logger LOGGER = Logger.getLogger(CavityDataPoint.class.getName());

    private final Date timestamp;
    private final String cavityName;
    private final String epicsName;
    private final String zoneName;
    private final String epicsZoneName;
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

    /**
     * Copy constructor
     *
     * @param cdp Object to base th copy on
     */
    public CavityDataPoint(CavityDataPoint cdp) {
        this.timestamp = cdp.getTimestamp();
        this.cavityName = cdp.getCavityName();
        this.cryomoduleType = cdp.getCryomoduleType();
        this.modAnodeVoltage = cdp.getModAnodeVoltage();
        this.linacName = cdp.getLinacName();
        this.epicsName = cdp.getEpicsName();
        this.gset = cdp.getGset();
        this.odvh = cdp.getOdvh();
        this.q0 = cdp.getQ0();
        this.qExternal = cdp.getqExternal();
        this.maxGset = cdp.getMaxGset();
        this.opsGsetMax = cdp.getOpsGsetMax();
        this.tripOffset = cdp.getTripOffset();
        this.tripSlope = cdp.getTripSlope();
        this.length = cdp.getLength();
        this.modAnodeHarvesterGsetData = cdp.getModAnodeHarvesterGsetData();
        this.bypassed = cdp.isBypassed();
        this.tunerBad = cdp.isTunerBad();
        this.epicsZoneName = cdp.getEpicsZoneName();
        this.zoneName = cdp.getZoneName();
    }

    public CavityDataPoint(Date timestamp, String cavityName, CryomoduleType cryomoduleType,
            BigDecimal modAnodeVoltage, String epicsName, BigDecimal gset, BigDecimal odvh,
            String q0, String qExternal, BigDecimal maxGset, BigDecimal opsGsetMax, BigDecimal tripOffset, BigDecimal tripSlope,
            BigDecimal length, CavityGsetData modAnodeHarvesterGsetData, boolean bypassed, boolean tunerBad) {

        if (!cavityName.matches("\\dL\\d\\d-\\d")) {
            LOGGER.log(Level.SEVERE, "Improper cavity name format - {0}", cavityName);
            throw new IllegalArgumentException("Invalid cavity name");
        }

        this.timestamp = timestamp;
        this.cavityName = cavityName;
        this.zoneName = cavityName.substring(0, 4);
        this.cryomoduleType = cryomoduleType;
        this.modAnodeVoltage = modAnodeVoltage;
        this.linacName = CebafNames.cedZoneToEnglishLinac(cavityName.substring(0, 4));
        this.epicsName = epicsName;
        this.epicsZoneName = epicsName.substring(0, 3) + "X";
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
    }

    public String getZoneName() {
        return zoneName;
    }

    public String getEpicsZoneName() {
        return epicsZoneName;
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

    public JsonObject toJson() {
        JsonObjectBuilder cavBuilder = Json.createObjectBuilder();
        cavBuilder.add("name", cavityName).add("linac", linacName.toString());
        // The json builder throws an exception on Null or Double.NaN.  This seemed like the smartest way to handle it.
        if (gset != null) {
            cavBuilder.add("gset", gset.doubleValue());
        } else {
            cavBuilder.add("gset", "");
        }
        if (modAnodeVoltage != null) {
            cavBuilder.add("modAnodeVoltage_kv", modAnodeVoltage.doubleValue());
        } else {
            cavBuilder.add("modAnodeVoltage_kv", "");
        }
        if (odvh != null) {
            cavBuilder.add("odvh", odvh.doubleValue());
        } else {
            cavBuilder.add("odvh", "");
        }
        if (q0 != null) {
            cavBuilder.add("q0", q0);
        } else {
            cavBuilder.add("q0", "");
        }

        if (qExternal != null) {
            cavBuilder.add("qExternal", qExternal);
        } else {
            cavBuilder.add("qExternal", "");
        }
        if (maxGset != null) {
            cavBuilder.add("maxGset", maxGset.doubleValue());
        } else {
            cavBuilder.add("maxGset", "");
        }
        if (opsGsetMax != null) {
            cavBuilder.add("opsGsetMax", opsGsetMax.doubleValue());
        } else {
            cavBuilder.add("opsGsetMax", "");
        }
        if (tripOffset != null) {
            cavBuilder.add("tripOffset", tripOffset.doubleValue());
        } else {
            cavBuilder.add("tripOffset", "");
        }
        if (tripSlope != null) {
            cavBuilder.add("tripSlope", tripSlope.doubleValue());
        } else {
            cavBuilder.add("tripSlope", "");
        }
        if (length!= null) {
            cavBuilder.add("length", length.doubleValue());
        } else {
            cavBuilder.add("length", "");
        }
        cavBuilder.add("bypassed", bypassed);
        cavBuilder.add("tunerBad", tunerBad);

        // Some of these will have ModAnodeHarvester data, but definitely not Injector cavities
        if (modAnodeHarvesterGsetData != null) {
            cavBuilder.add("modAnodeHarvester", modAnodeHarvesterGsetData.toJson());
        }

        cavBuilder.add("moduleType", cryomoduleType.toString()).add("epicsName", epicsName);

        return cavBuilder.build();
    }
}
