/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.business.service;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.rfd.business.util.DateUtil;
import org.jlab.rfd.business.util.SqlUtil;
import org.jlab.rfd.model.LinacName;
import org.jlab.rfd.model.ModAnodeHarvester.CavityGsetData;
import org.jlab.rfd.model.ModAnodeHarvester.GsetRecord;
import org.jlab.rfd.model.ModAnodeHarvester.LinacDataPoint;
import org.jlab.rfd.model.ModAnodeHarvester.LinacDataSpan;
import org.jlab.rfd.model.ModAnodeHarvester.LinacRecord;
import org.jlab.rfd.model.ModAnodeHarvester.ScanRecord;
import org.jlab.rfd.model.TimeUnit;

/**
 * This service class exists to return the results of ModAnodeHarvester.pl,
 * which is a part of the CSUE RFGradTeamTools application. This data
 * contains the LEM-calculated Linac trip rates and Cavity GSETs associated with
 * a standard configuration and one without any applied Mod Anode Voltage.
 *
 * @author adamc
 */
public class ModAnodeHarvesterService {

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat ORA_DATE_FORMATTER = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private static final Logger LOGGER = Logger.getLogger(ModAnodeHarvesterService.class.getName());
    // This will be shown to the user on error.  Better to make it generic and have a log statement just before.
    private final String ERR_STRING = "Error querying data.";

    
    /**
     * Get a LinacDataSpan with mod anode harvester linac level data for the requested dates
     * @param start The first date to include
     * @param end Include no dates after this
     * @param timeUnit The interval between queried dates.  TimeUnit.Day implies on sample per day, Week implies one
     *                 sample every seven days.
     * @return A LinacDataSpan object for the requested dates.
     * @throws java.text.ParseException If error parsing/formatting date strings
     * @throws java.sql.SQLException If error querying the database
     */
    public LinacDataSpan getLinacDataSpan(Date start, Date end, TimeUnit timeUnit) throws ParseException, SQLException {
        int numDays;
        switch (timeUnit) {
            case DAY:
                numDays = 1;
                break;
            case WEEK:
            default:
                numDays = 7;
        }
        Date curr = DateUtil.truncateToDays(start);
        Date e;
        if ( end.after(new Date()) ) {
            e = DateUtil.truncateToDays(new Date());
        } else {
            e = DateUtil.truncateToDays(end);
        }
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(curr);

        Set<Date> dates = new HashSet<>();
        while ( ! curr.after(e) ) {
            dates.add(curr);
            cal.add(Calendar.DAY_OF_MONTH, numDays);
            curr = DateUtil.truncateToDays(cal.getTime());
        }
        
        return this.getLinacDataSpan(dates);
    }

    /**
     * Query the mod anode harvester table for linac level data
     * @param dates The dates to query the data for
     * @return A LinacDataSpan containing data across the requested dates.
     * @throws ParseException If error parsing/formatting date strings
     * @throws SQLException If error querying the database
     */
    public LinacDataSpan getLinacDataSpan(Set<Date> dates) throws ParseException, SQLException {
        LinacDataSpan span = new LinacDataSpan();
        for (Date d: dates) {
            Set<LinacDataPoint> ldps = this.getLinacData(d);
            if ( ldps != null ) {
                span.put(d, ldps);
            }
        }
        return span;
    }
    
    /**
     * This queries the ModAnodeHarvester database tables for linac scan data associated with the first scan of that day.
     * @param timestamp The date of interested
     * @return A map of linac name to linac data for the day requested  or null if no scan data exists for that day
     * @throws ParseException If error parsing/formatting the date strings
     * @throws SQLException If error querying the database
     */
    public Set<LinacDataPoint> getLinacData(Date timestamp) throws ParseException, SQLException {

        ScanRecord sr = getFirstScanRecord(timestamp);
        Set<LinacDataPoint> data = null;
                
        if ( sr != null ) {
            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;

            String linacSql = "SELECT LINAC, ENERGY_MEV, TRIPS_PER_HOUR, TRIPS_PER_HOUR_NO_MAV"
                    + " FROM MOD_ANODE_HARVESTER_LINAC_SCAN"
                    + " WHERE SCAN_ID = ?";

            try {
                conn = SqlUtil.getConnection();
                pstmt = conn.prepareStatement(linacSql);
                pstmt.setLong(1, sr.getScanId());
                rs = pstmt.executeQuery();

                Map<LinacName, LinacRecord> records1050 = new HashMap<>();
                Map<LinacName, LinacRecord> records1090 = new HashMap<>();
                while( rs.next() ) {
                    LinacName linac = LinacName.valueOf(rs.getString("LINAC"));
                    Double energy = rs.getDouble("ENERGY_MEV");
                    Double trips = rs.getDouble("TRIPS_PER_HOUR");
                    if (rs.wasNull()) {
                        trips = null;
                    }
//                    if ( trips != null ) {
//                        trips = trips.setScale(6, RoundingMode.HALF_UP);
//                    }
                    Double tripsNoMav = rs.getDouble("TRIPS_PER_HOUR_NO_MAV");
                    if (rs.wasNull()) {
                        tripsNoMav = null;
                    }
//                    if ( tripsNoMav != null ) {
//                        tripsNoMav = tripsNoMav.setScale(6, RoundingMode.HALF_UP);
//                    }

                    LinacRecord record = new LinacRecord(sr.getTimestamp(), sr.getEpicsDate(), linac, energy, trips, tripsNoMav);
                    switch (record.getEnergy().intValue()) {
                        case 1050:
                            records1050.put(record.getLinacName(), record);
                            break;
                        case 1090:
                            records1090.put(record.getLinacName(), record);
                            break;
                        default:
                            LOGGER.log(Level.SEVERE, "Received record with unexpected energy ''{0}''", record.getEnergy().toString());
                            throw new RuntimeException(ERR_STRING);
                    }
                }

                data = new HashSet<>();
                
                // Combine the two records for each cavity into one LinacDataPoint object and add it to the map.
                if (records1050.size() != records1090.size()) {
                    LOGGER.log(Level.SEVERE, "Database query did not return identical linac sets at different energies");
                    throw new RuntimeException(ERR_STRING);
                }
                for (LinacName linacName : records1050.keySet()) {
                    if (!records1090.containsKey(linacName)) {
                        LOGGER.log(Level.SEVERE, "ModAnodeHarvester database has dissimilar linac sets at different energy levels");
                        throw new RuntimeException(ERR_STRING);
                    }
                    if (linacName == null) {
                        LOGGER.log(Level.SEVERE, "Received Gset record with null EPICS_NAME");
                        throw new RuntimeException(ERR_STRING);
                    }
                    LinacDataPoint linacData = new LinacDataPoint(records1050.get(linacName), records1090.get(linacName));
                    data.add(linacData);
                }
            } finally {
                SqlUtil.close(conn, pstmt, rs);
            }
        }
        
        return data;
    }

    /**
     * Convenience function for querying a single day's worth of CavityGsetData
     * @param timestamp The date for which to query the data
     * @return A map of cavity name to cavity data for the requested day or null if no scan data existed for that day.
     * @throws SQLException If error on querying the database
     * @throws ParseException If error parsing/formatting date strings
     */
    public Map<String, CavityGsetData> getCavityGsetData(Date timestamp) throws SQLException, ParseException {
        Set<Date> dates = new HashSet<>();
        dates.add(timestamp);
        Map<Date, Map<String, CavityGsetData>> outMap = getCavityGsetData(dates);
        if (outMap == null || outMap.isEmpty()) {
            return null;
        }
        return outMap.get(timestamp);
    }

        /**
         * This method queries the ModAnodeHarvester tables for the Cavity GSET data associated with first scan of the given day.
         * @param dates The dates of interest
         * @return A map of cavity name to cavity data for the requested days or null if no scan data exists for those days
         * @throws SQLException If error querying database
         * @throws ParseException If error parsing/formatting dates
         */
    public Map<Date, Map<String, CavityGsetData>> getCavityGsetData(Set<Date> dates) throws SQLException, ParseException {

        Set<ScanRecord> scanRecords = getFirstScanRecord(dates);
        Map<String, CavityGsetData> data;
        Map<Date, Map<String, CavityGsetData>> outMap = new HashMap<>();

        if (scanRecords != null && !scanRecords.isEmpty()) {
            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;

            String gsetSql = "SELECT CAVITY_EPICS, ENERGY_MEV, GSET_MVPM, GSET_NO_MAV_MVPM, MOD_ANODE_KV"
                    + " FROM MOD_ANODE_HARVESTER_GSET"
                    + " WHERE SCAN_ID = ?";

            try {
                conn = SqlUtil.getConnection();
                pstmt = conn.prepareStatement(gsetSql);

                for(ScanRecord sr : scanRecords) {
                    Map<String, GsetRecord> records1090 = new HashMap<>();
                    Map<String, GsetRecord> records1050 = new HashMap<>();
                    pstmt.setLong(1, sr.getScanId());
                    rs = pstmt.executeQuery();
                    while (rs.next()) {
                        String epicsName = rs.getString("CAVITY_EPICS");
                        BigDecimal energy = rs.getBigDecimal("ENERGY_MEV");
                        BigDecimal gset = rs.getBigDecimal("GSET_MVPM");
                        BigDecimal gsetNoMav = rs.getBigDecimal("GSET_NO_MAV_MVPM");
                        BigDecimal modAnode = rs.getBigDecimal("MOD_ANODE_KV");
                        GsetRecord record = new GsetRecord(sr.getTimestamp(), sr.getEpicsDate(), modAnode, epicsName, energy, gset, gsetNoMav);
                        switch (record.getEnergy().intValue()) {
                            case 1050:
                                records1050.put(record.getEpicsName(), record);
                                break;
                            case 1090:
                                records1090.put(record.getEpicsName(), record);
                                break;
                            default:
                                LOGGER.log(Level.SEVERE, "Received record with unexpected energy ''{0}''", record.getEnergy().toString());
                                throw new RuntimeException(ERR_STRING);
                        }
                    }

                    data = new HashMap<>();

                    // Combine the two records for each cavity into one CavityGsetData object and add it to the map.
                    if (records1050.size() != records1090.size()) {
                        LOGGER.log(Level.SEVERE, "Database query did not return identical cavity sets at different energies");
                        throw new RuntimeException(ERR_STRING);
                    }
                    for (String epicsName : records1050.keySet()) {
                        if (!records1090.containsKey(epicsName)) {
                            LOGGER.log(Level.SEVERE, "ModAnodeHarvester database has dissimilar cavity sets at different energy levels");
                            throw new RuntimeException(ERR_STRING);
                        }
                        if (epicsName == null) {
                            LOGGER.log(Level.SEVERE, "Received Gset record with null EPICS_NAME");
                            throw new RuntimeException(ERR_STRING);
                        }
                        data.put(epicsName, new CavityGsetData(records1050.get(epicsName), records1090.get(epicsName)));
                    }
                    outMap.put(DateUtil.truncateToDays(sr.getTimestamp()), data);
                }
            } finally {
                SqlUtil.close(rs, pstmt, conn);
            }
        }
        return outMap;
    }

    /**
     * Get the first scan record on the specified date
     * @param date The date to lookup
     * @return A ScanRecord associated with that date or null
     * @throws SQLException If error querying the database
     * @throws ParseException If error parsing/formatting the date strings
     */
    private static ScanRecord getFirstScanRecord(Date date) throws SQLException, ParseException {
        Set<Date> dates = new HashSet<>();
        dates.add(date);
        Set<ScanRecord> records = getFirstScanRecord(dates);
        if (records == null || records.isEmpty()) {
            return null;
        }
        return (ScanRecord) records.toArray()[0];
    }
    // 
    // Returns the first record from that day or returns null.
    /**
     * This queries the ModAnodeHarvester tables for information on the first scan of the given day
     * @param dates The list dates to query scan records for
     * @return A ScanRecord object representing the first scan of the day or null if no scan data exists for that day.
     * @throws ParseException If error parsing/formatting date strings
     * @throws SQLException  If error querying database
     */
    private static Set<ScanRecord> getFirstScanRecord(Set<Date> dates) throws ParseException, SQLException {

        Set<ScanRecord> records = new HashSet<>();

        String scanSql = "SELECT SCAN_ID, START_TIME, EPICS_DATE"
                + " FROM MOD_ANODE_HARVESTER_SCAN"
                + " WHERE START_TIME BETWEEN TO_DATE(?, 'YYYY/MM/DD HH24:MI:SS') AND"
                + " TO_DATE(?, 'YYYY/MM/DD HH24:MI:SS') AND ROWNUM <= 1 ORDER BY START_TIME";


        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = SqlUtil.getConnection();
            pstmt = conn.prepareStatement(scanSql);
            for(Date date: dates) {
                Date start = DateUtil.truncateToDays(date);
                Date end = DateUtil.getEndOfDay(start);

                pstmt.setString(1, ORA_DATE_FORMATTER.format(start));
                pstmt.setString(2, ORA_DATE_FORMATTER.format(end));
                rs = pstmt.executeQuery();
                if (rs.next()) {
                    long scanId = rs.getLong("SCAN_ID");
                    Date ts = DATE_FORMATTER.parse(rs.getString("START_TIME"));
                    Date ed = DATE_FORMATTER.parse(rs.getString("EPICS_DATE"));
                    records.add(new ScanRecord(scanId, ts, ed));
                }
                pstmt.clearParameters();
            }
        } finally {
            SqlUtil.close(conn, pstmt, rs);
        }

        // Downstream expects null if no data found
        if (records.isEmpty()){
            return null;
        }

        return records;
    }
}
