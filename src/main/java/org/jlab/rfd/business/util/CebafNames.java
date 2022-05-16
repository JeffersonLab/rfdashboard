/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.business.util;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import org.jlab.rfd.model.LinacName;

/**
 *
 * @author adamc
 */
public class CebafNames {

    // Create lookup tables as needed to convert between the different cavity/zone/linac naming conventions. 
    private static final HashMap<Integer, LinacName> LINAC_NUMBER_TO_NAME = new HashMap<>();

    static {
        LINAC_NUMBER_TO_NAME.put(0, LinacName.Injector);
        LINAC_NUMBER_TO_NAME.put(1, LinacName.North);
        LINAC_NUMBER_TO_NAME.put(2, LinacName.South);
    }

    public static LinacName cedZoneToEnglishLinac(String cedZone) {
        if (cedZone.length() != 4) {
            throw new IllegalArgumentException("cedZones must be four characters in length");
        }

        Integer linacNumber = Integer.parseInt(cedZone.substring(0, 1));
        if (linacNumber > 2) {
            throw new IllegalArgumentException("Improper CED zone");
        }
        return LINAC_NUMBER_TO_NAME.get(linacNumber);
    }

    /**
     * Function for converting a cavity name from EPICS to CED formats
     *
     * @param epicsName The EPICS name of a cavity
     * @return
     */
    public static String epicsCavityToCedCavity(String epicsName) {

        // Make sure we get something close to the standard R\d\w\d scheme
        if (!Pattern.matches("R[0-9][0-9A-Q][0-9]$", epicsName)) {
            throw new IllegalArgumentException("Supplied EPICSName does not comply with naming convention.");
        }

        Map<String, String> eHexToDecimal = new HashMap<>();
        eHexToDecimal.put("A", "10");
        eHexToDecimal.put("B", "11");
        eHexToDecimal.put("C", "12");
        eHexToDecimal.put("D", "13");
        eHexToDecimal.put("E", "14");
        eHexToDecimal.put("F", "15");
        eHexToDecimal.put("G", "16");
        eHexToDecimal.put("H", "17");
        eHexToDecimal.put("I", "18");
        eHexToDecimal.put("J", "19");
        eHexToDecimal.put("K", "20");
        eHexToDecimal.put("L", "21");
        eHexToDecimal.put("M", "22");
        eHexToDecimal.put("N", "23");
        eHexToDecimal.put("O", "24");
        eHexToDecimal.put("P", "25");
        eHexToDecimal.put("Q", "26");

        String linac = epicsName.substring(1, 2);

        String zone = epicsName.substring(2, 3);
        try {
            Integer temp = Integer.parseInt(zone);
            zone = "0" + zone;
        } catch (NumberFormatException e) {
            zone = eHexToDecimal.get(zone);
        }
        String cavity = epicsName.substring(3, 4);

        return linac + "L" + zone + "-" + cavity;
    }

    /**
     * Function for converting a zone or cryomodule name from EPICS to CED formats
     *
     * @param epicsName The EPICS name of a cavity
     * @return
     */
    public static String epicsZoneToCedZone(String epicsName) {
        // Make sure we get something close to the standard R\d\w\d scheme
        if (!Pattern.matches("R[0-9][0-9A-Q]$", epicsName)) {
            throw new IllegalArgumentException("Supplied EPICSName does not comply with naming convention.");
        }

        Map<String, String> eHexToDecimal = new HashMap<>();
        eHexToDecimal.put("A", "10");
        eHexToDecimal.put("B", "11");
        eHexToDecimal.put("C", "12");
        eHexToDecimal.put("D", "13");
        eHexToDecimal.put("E", "14");
        eHexToDecimal.put("F", "15");
        eHexToDecimal.put("G", "16");
        eHexToDecimal.put("H", "17");
        eHexToDecimal.put("I", "18");
        eHexToDecimal.put("J", "19");
        eHexToDecimal.put("K", "20");
        eHexToDecimal.put("L", "21");
        eHexToDecimal.put("M", "22");
        eHexToDecimal.put("N", "23");
        eHexToDecimal.put("O", "24");
        eHexToDecimal.put("P", "25");
        eHexToDecimal.put("Q", "26");

        String linac = epicsName.substring(1, 2);

        String zone = epicsName.substring(2, 3);
        try {
            Integer temp = Integer.parseInt(zone);
            zone = "0" + zone;
        } catch (NumberFormatException e) {
            zone = eHexToDecimal.get(zone);
        }

        return linac + "L" + zone;
    }
}
