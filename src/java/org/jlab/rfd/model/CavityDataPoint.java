/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.model;

import java.util.Date;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.jlab.rfd.business.util.CebafNames;

/**
 *
 * @author adamc
 */
public class CavityDataPoint {
    private static final Logger LOGGER = Logger.getLogger(CavityDataPoint.class.getName());
    
    private final Date timestamp;
    private final String cavityName;
    private final CryomoduleType cryomoduleType;
    private final LinacName linacName;

    public CavityDataPoint(Date timestamp, String cavityName, CryomoduleType cryomoduleType) {
        
        if ( ! cavityName.matches("\\dL\\d\\d-\\d") ) {
            LOGGER.log(Level.SEVERE, "Improper cavity name format - {0}", cavityName);
            throw new IllegalArgumentException("Invalid cavity name");
        }
        
        this.timestamp = timestamp;
        this.cavityName = cavityName;
        this.cryomoduleType = cryomoduleType;
        this.linacName = CebafNames.cedZoneToEnglishLinac(cavityName.substring(0, 4));
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
        
}
