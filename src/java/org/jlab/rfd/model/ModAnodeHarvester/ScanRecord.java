/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.model.ModAnodeHarvester;

import java.util.Date;

/**
 *
 * @author adamc
 */
public class ScanRecord {

    private final long scanId;
    private final Date timestamp;
    private final Date epicsDate;

    public ScanRecord(long scanId, Date timestamp, Date epicsDate) {
        this.scanId = scanId;
        this.timestamp = timestamp;
        this.epicsDate = epicsDate;
    }

    public long getScanId() {
        return scanId;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public Date getEpicsDate() {
        return epicsDate;
    }

}
