/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.business.util;

import java.util.HashMap;
import org.jlab.rfd.model.LinacName;

/**
 *
 * @author adamc
 */
public class CebafNames {

    // Create lookup tables as needed to convert between the different cavity/zone/linac naming conventions. 
    private static final HashMap<Integer, LinacName> LINAC_NUMBER_TO_NAME = new HashMap();
    static {
        LINAC_NUMBER_TO_NAME.put(0, LinacName.Injector);
        LINAC_NUMBER_TO_NAME.put(1, LinacName.North);
        LINAC_NUMBER_TO_NAME.put(2, LinacName.South);
    }
    
    public static LinacName cedZoneToEnglishLinac(String cedZone) {
        if ( cedZone.length() != 4) {
            throw new IllegalArgumentException("cedZones must be four characters in length");
        }
        
        Integer linacNumber = Integer.parseInt(cedZone.substring(0,1));
        if (linacNumber > 2 ) {
            throw new IllegalArgumentException("Improper CED zone");
        }
        return LINAC_NUMBER_TO_NAME.get(linacNumber);
    }
}
