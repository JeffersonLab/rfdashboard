/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.model;

/**
 *
 * @author adamc
 */
public class CryomoduleDataPoint {

    private final String name;                        // CED Name
    private final String epicsName;               // EPICS Name
    private final LinacName linac;
    private final CryomoduleType cmType;    // Should be from CED
    private final Double[] gsets;                   // Array of cavity GSETs
    private final Double[] lengths;                 // Array of cavity lengths.  _SHOULD_ be identical.
    private final Double heat;                       // The heat requested by LEM (PV, R<LINAC><ZONE>XHTPLEM 

    public CryomoduleDataPoint(String name, String epicsName, LinacName linac, CryomoduleType cmType, Double[] gsets, Double[] lengths, Double heat) {
        if (gsets.length != 8) {
            throw new RuntimeException("Error: gsets.length != 8 for " + name);
        }
        if (lengths.length != 8) {
            throw new RuntimeException("Error: lengths.length != 8 " + name);
        }
        this.name = name;
        this.epicsName = epicsName;
        this.linac = linac;
        this.cmType = cmType;
        this.gsets = new Double[8];
        this.lengths = new Double[8];
        for (int i = 0; i < this.gsets.length; i++) {
            this.gsets[i] = gsets[i];
        }
        for (int i = 0; i < this.lengths.length; i++) {
            this.lengths[i] = lengths[i];
        }
        this.heat = heat;
    }

    public String getName() {
        return name;
    }

    public String getEpicsName() {
        return epicsName;
    }

    public LinacName getLinac() {
        return linac;
    }

    public CryomoduleType getCmType() {
        return cmType;
    }

    public Double getHeat() {
        return heat;
    }
    
    /**
     * Calculates the energy of the Cryomodule using a simple formula
     * SUM_cav(GSET*Length)
     *
     * @return The energy gain or null if there was a problem with the data
     */
    public Double getEGain() {
        double energy = 0;
        for (int i = 0; i < 8; i++) {
            if (gsets[i] != null && lengths[i] != null) {
            energy += gsets[i] * lengths[i];
            } else {
                energy = Double.NaN;
                return energy;
            }
        }
        return energy;
    }

    /**
     * Return the nominal energy of this Cryomodule
     * @return
     */
    public double getNominalEGain() {
        return getNominalEGain(cmType);
    }

    /**
     * Returns the nominal energy of a Cryomodule based on CryomoduleType (E.g.,
     * C25 -> 25 MEV)
     *
     * @return The nominal energy of a cryomodule type
     */
    public static double getNominalEGain(CryomoduleType cmType) {
        double energy;

        // Logic according to Jay - ignore the QTR and everything after it is sufficiently relativistic that length*gset = energy gain
        // Since everything is after the QTR (0L02), nominal energy gain should be based on the type of cryomodule
        switch (cmType) {
            case C25:
                energy = 25.;
                break;
            case C50:
                energy = 50.;
                break;
            case C100:
                energy = 100.;
                break;
            case C50T:
                energy = 50.;
                break;
            case C75:
                energy = 75.;
                break;
            case F100:
                energy = 100.;
                break;
            case Booster:
                energy = Double.NaN;
                break;
            case QTR:
                energy = Double.NaN;
                break;
            default:
                energy = Double.NaN;
        }
        
        return energy;
    }
    
    public double getEGainPerformance() {
        return getEGain() / getNominalEGain() * 100.;
    }
}
