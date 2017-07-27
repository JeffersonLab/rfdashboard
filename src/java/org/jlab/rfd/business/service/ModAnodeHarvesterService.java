/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.business.service;

import java.io.IOException;
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
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.rfd.business.util.DateUtil;
import org.jlab.rfd.business.util.SqlUtil;
import org.jlab.rfd.model.LinacName;
import org.jlab.rfd.model.ModAnodeHarvester.CavityGsetData;
import org.jlab.rfd.model.ModAnodeHarvester.GsetRecord;
import org.jlab.rfd.model.ModAnodeHarvester.LinacData;
import org.jlab.rfd.model.ModAnodeHarvester.LinacRecord;
import org.jlab.rfd.model.ModAnodeHarvester.ScanRecord;

/**
 * This service class exists to return the results of ModAnodeHarvester.pl,
 * which is a part of the the CSUE RFGradTeamTools application. This data
 * contains the LEM-calculated Linac trip rates and Cavity GSETs associated with
 * a standard configuration and one without any applied Mod Anode Voltage.
 *
 * @author adamc
 */
public class ModAnodeHarvesterService {

    private static final SimpleDateFormat DATE_FORMATER = new SimpleDateFormat("yyyy-MM-dd");
    private static final SimpleDateFormat ORA_DATE_FORMATER = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    private static final Logger LOGGER = Logger.getLogger(ModAnodeHarvesterService.class.getName());
    // This will be shown to the user on error.  Better to make it generic and have a log statement just before.
    private final String ERR_STRING = "Error querying data.";

    /**
     * This queries the ModAnodeHarvester database tables for linac scan data associated with the first scan of that day.
     * @param timestamp The date of interested
     * @return A map of linac name to linac data for the day requested  or null if no scan data exists for that day
     * @throws ParseException
     * @throws SQLException 
     */
    public Map<LinacName, LinacData> getLinacData(Date timestamp) throws ParseException, SQLException {

        ScanRecord sr =getFirstScanRecord(timestamp);
        Map<LinacName, LinacData> data = null;
        
        if ( sr != null ) {
            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;

            String linacSql = "SELECT LINAC, ENERGY_MEV, TRIPS_PER_HOUR, TRIP_PER_HOUR_NO_MAV"
                    + "FROM MOD_ANODE_HARVESTER_LINAC_SCAN"
                    + "WHERE SCAN_ID = ?";

            try {
                conn = SqlUtil.getConnection();
                pstmt = conn.prepareStatement(linacSql);
                pstmt.setLong(1, sr.getScanId());
                rs = pstmt.executeQuery();

                Map<LinacName, LinacRecord> records1050 = new HashMap<>();
                Map<LinacName, LinacRecord> records1090 = new HashMap<>();
                while( rs.next() ) {
                    LinacName linac = LinacName.valueOf(rs.getString("LINAC"));
                    BigDecimal energy = new BigDecimal(rs.getDouble("ENERGY_MEV"));
                    BigDecimal trips = new  BigDecimal(rs.getDouble("TRIPS_PER_HOUR"));
                    BigDecimal tripsNoMav = new  BigDecimal(rs.getDouble("TRIPS_PER_HOUR_NO_MAV"));
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

                data = new HashMap<>();
                
                // Combine the two records for each cavity into one LinacData object and add it to the map.
                if (records1050.size() != records1090.size()) {
                    LOGGER.log(Level.SEVERE, "Database query did not return identical linac sets at different energies");
                    throw new RuntimeException(ERR_STRING);
                }
                for (LinacName linacName : records1050.keySet()) {
                    if (!records1090.containsKey(linacName)) {
                        LOGGER.log(Level.SEVERE, "ModAnodeHarvester database has disimilar linac sets at different energy levels");
                        throw new RuntimeException(ERR_STRING);
                    }
                    if (linacName == null) {
                        LOGGER.log(Level.SEVERE, "Received Gset record with null EPICS_NAME");
                        throw new RuntimeException(ERR_STRING);
                    }
                    LinacData linacData = new LinacData(records1050.get(linacName), records1090.get(linacName));
                    if (linacData == null) {
                        throw new RuntimeException(ERR_STRING);
                    } else {
                        data.put(linacName, linacData);
                    }
                }
            } finally {
                SqlUtil.close(conn, pstmt, rs);
            }
        }
        
        return data;
    }
    
    /**
     * This method queries the ModAnodeHarvester tables for the Cavity GSET data associated with first scan of the given day.
     * @param timestamp The date of interest
     * @return A map of cavity name to cavity data for the requested day or null if no scan data exists for that day
     * @throws SQLException
     * @throws ParseException
     * @throws IOException 
     */
    public Map<String, CavityGsetData> getCavityGsetData(Date timestamp) throws SQLException, ParseException, IOException {

        ScanRecord sr = getFirstScanRecord(timestamp);
        Map<String, CavityGsetData> data = null;

        if (sr != null) {
            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;

            String gsetSql = "SELECT CAVITY_EPICS, ENERGY_MEV, GSET_MVPM, GSET_NO_MAV_MVPM, MOD_ANODE_KV"
                    + " FROM MOD_ANODE_HARVESTER_GSET"
                    + " WHERE SCAN_ID = ?";

            Map<String, GsetRecord> records1090 = new HashMap<>();
            Map<String, GsetRecord> records1050 = new HashMap<>();
            try {
                conn = SqlUtil.getConnection();

                pstmt = conn.prepareStatement(gsetSql);
                pstmt.setLong(1, sr.getScanId());
                rs = pstmt.executeQuery();
                while (rs.next()) {
                    String epicsName = rs.getString("CAVITY_EPICS");
                    BigDecimal energy = new BigDecimal(rs.getDouble("ENERGY_MEV"));
                    BigDecimal gset = new BigDecimal(rs.getDouble("GSET_MVPM"));
                    BigDecimal gsetNoMav = new BigDecimal(rs.getDouble("GSET_NO_MAV_MVPM"));
                    BigDecimal modAnode = new BigDecimal(rs.getDouble("MOD_ANODE_KV"));
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
                        LOGGER.log(Level.SEVERE, "ModAnodeHarvester database has disimilar cavity sets at different energy levels");
                        throw new RuntimeException(ERR_STRING);
                    }
                    if (epicsName == null) {
                        LOGGER.log(Level.SEVERE, "Received Gset record with null EPICS_NAME");
                        throw new RuntimeException(ERR_STRING);
                    }
                    CavityGsetData cgd = new CavityGsetData(records1050.get(epicsName), records1090.get(epicsName));
                    if (cgd == null) {
                        throw new RuntimeException(ERR_STRING);
                    } else {
                        data.put(epicsName, cgd);
                    }
                }
            } finally {
                SqlUtil.close(rs, pstmt, conn);
            }
        }
        return data;
    }

    // 
    // Returns the first record from that day or returns null.
    /**
     * This queries the ModAnodeHarvester tables for information on the first scan of the given day
     * @param date The date of interested
     * @return A ScanRecord object representing the fisrt scan of the day or null if no scan data exists for that day.
     * @throws ParseException
     * @throws SQLException 
     */
    private static ScanRecord getFirstScanRecord(Date date) throws ParseException, SQLException {

        Date start = DateUtil.truncateToDays(date);
        Date end = DateUtil.getEndOfDay(start);
        ScanRecord sr = null;

        String scanSql = "SELECT SCAN_ID, START_TIME, EPICS_DATE"
                + " FROM MOD_ANODE_HARVESTER_SCAN"
                + " WHERE START_TIME BETWEEN TO_DATE(?, 'YYYY/MM/DD HH24:MI:SS') AND"
                + " TO_DATE(?, 'YYYY/MM/DD HH24:MI:SS') AND ROWNUM <= 1 ORDER BY START_TIME ASC";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            conn = SqlUtil.getConnection();
            pstmt = conn.prepareStatement(scanSql);
            pstmt.setString(1, ORA_DATE_FORMATER.format(start));
            pstmt.setString(2, ORA_DATE_FORMATER.format(end));
            rs = pstmt.executeQuery();
            if (rs.next()) {
                long scanId = rs.getLong("SCAN_ID");
                Date ts = DATE_FORMATER.parse(rs.getString("START_TIME"));
                Date ed = DATE_FORMATER.parse(rs.getString("EPICS_DATE"));
                pstmt.close();
                rs.close();

                sr = new ScanRecord(scanId, ts, ed);
            }
        } finally {
            SqlUtil.close(conn, pstmt, rs);
        }

        return sr;
    }
}
