/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.model.ModAnodeHarvester;

import java.math.BigDecimal;
import java.util.Date;
import org.jlab.rfd.model.LinacName;

/**
 *
 * @author adamc
 */
public class LinacRecord {
    private final Date timestamp;
    private final Date epicsDate;
    private final Double energy;
    private final Double trips;
    private final Double tripsNoMav;
    private final LinacName linacName;
    
    /**
     * Construct a CavityGsetPoint to represent a Cavity GSET data point from the ModAnodeHarvester.
     * @param timestamp The time of the scan
     * @param epicsDate The date of the recreated EPICS control system (see CSUE's LEMSim for more information)
     * @param linacName The name of the Linac represented in this record
     * @param energy The energy (MeV) used in the LEM calculations
     * @param trips The number of trips per hour estimated by LEM when including the CED ModAnode values
     * @param tripsNoMav The number of trips per hour estimated by LEM when zeroing out the CED ModAnode values
     */
    public LinacRecord(Date timestamp, Date epicsDate, LinacName linacName, Double energy, Double trips, Double tripsNoMav) {

        this.linacName = linacName;
        this.timestamp = timestamp;
        this.epicsDate = epicsDate;
        this.energy = energy;
        this.trips = trips;
        this.tripsNoMav = tripsNoMav;
    }

    public LinacName getLinacName() {
        return linacName;
    }

    public Double getEnergy() {
        return energy;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Date getEpicsDate() {
        return epicsDate;
    }

    public Double getTrips() {
        return trips;
    }

    public Double getTripsNoMav() {
        return tripsNoMav;
    }    
}
