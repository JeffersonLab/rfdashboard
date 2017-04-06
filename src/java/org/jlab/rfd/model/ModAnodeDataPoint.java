/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.model;

import java.math.BigDecimal;
import java.util.Date;

/**
 *
 * @author adamc
 */
public class ModAnodeDataPoint extends CavityDataPoint{
    private final BigDecimal modAnodeVoltage;
 
    public ModAnodeDataPoint (Date timestamp, String cavityName, CryomoduleType cryomoduleType,
            BigDecimal modAnodeVoltage) {
        super(timestamp, cavityName, cryomoduleType);
        this.modAnodeVoltage = modAnodeVoltage;
    }

    public BigDecimal getModAnodeVoltage() {
        return modAnodeVoltage;
    }
    
}
