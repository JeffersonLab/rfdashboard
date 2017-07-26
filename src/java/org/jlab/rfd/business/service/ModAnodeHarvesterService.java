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
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.rfd.business.util.SqlUtil;
import org.jlab.rfd.model.CryomoduleType;
import org.jlab.rfd.model.ModAnodeHarvester.CavityGsetData;
import org.jlab.rfd.model.ModAnodeHarvester.GsetRecord;

/**
 * This service class exists to return the results of ModAnodeHarvester.pl, which is a part of the the CSUE RFGradTeamTools
 * application.  This data contains the LEM-calculated Linac trip rates and Cavity GSETs associated with a standard
 * configuration and one without any applied Mod Anode Voltage.
 * @author adamc
 */
public class ModAnodeHarvesterService {
    private static final Logger LOGGER = Logger.getLogger(ModAnodeHarvesterService.class.getName());
    
    public Map<String, CavityGsetData> getCavityGsetData(Date timestamp) throws SQLException, ParseException, IOException {
        final String mahErrString = "Error querying ModAnodeHarvester data.";
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat oraDF = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        
        Date start;
        try {
            start = sdf.parse(sdf.format(timestamp));
        } catch (ParseException ex) {
            LOGGER.log(Level.WARNING, "Error parse dates timestamp = {0}", timestamp);
            throw ex;
        }
        
        Calendar cal = Calendar.getInstance();
        cal.setTime(start);
        cal.add(Calendar.DATE, 1);
        cal.add(Calendar.SECOND, -1);
        Date end = cal.getTime();
        
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        
        String scanSql = "SELECT SCAN_ID, START_TIME, EPICS_DATE"
                + " FROM MOD_ANODE_HARVESTER_SCAN"
                + " WHERE START_TIME BETWEEN TO_DATE(?, 'YYYY/MM/DD HH24:MI:SS') AND"
                + " TO_DATE(?, 'YYYY/MM/DD HH24:MI:SS') AND ROWNUM <= 1 ORDER BY START_TIME ASC";
        
        String gsetSql = "SELECT CAVITY_EPICS, ENERGY_MEV, GSET_MVPM, GSET_NO_MAV_MVPM, MOD_ANODE_KV"
                + " FROM MOD_ANODE_HARVESTER_GSET"
                + " WHERE SCAN_ID = ?";
        
        
        Map<String, CavityGsetData> data = null;
        Map<String, GsetRecord> records1090 = new HashMap<>();
        Map<String, GsetRecord> records1050 = new HashMap<>();
        try {
            conn = SqlUtil.getConnection();
            pstmt = conn.prepareStatement(scanSql);
            pstmt.setString(1, oraDF.format(start));
            pstmt.setString(2, oraDF.format(end));
            rs = pstmt.executeQuery();
            if ( ! rs.next() ) {
                LOGGER.log(Level.INFO, "No results returned for day: {0}", sdf.format(start));
            } else {
                data = new HashMap<>();
                long scanId = rs.getLong("SCAN_ID");
                Date ts = sdf.parse(rs.getString("START_TIME"));
                Date ed = sdf.parse(rs.getString("EPICS_DATE"));
                System.out.println("scanId:" + scanId + "  timestamp:" + ts + "  ed:" + ed);
                pstmt.close();
                rs.close();

                pstmt = conn.prepareStatement(gsetSql);
                pstmt.setLong(1, scanId);
                rs = pstmt.executeQuery();
                while( rs.next() ) {
                    String epicsName = rs.getString("CAVITY_EPICS");
                    BigDecimal energy = new BigDecimal(rs.getDouble("ENERGY_MEV"));
                    BigDecimal gset = new BigDecimal(rs.getDouble("GSET_MVPM"));
                    BigDecimal gsetNoMav = new BigDecimal(rs.getDouble("GSET_NO_MAV_MVPM"));
                    BigDecimal modAnode = new BigDecimal(rs.getDouble("MOD_ANODE_KV"));
                    GsetRecord record = new GsetRecord(ts, ed, modAnode, epicsName, energy, gset, gsetNoMav);
                    switch (record.getEnergy().intValue()) {
                        case 1050:
                            records1050.put(record.getEpicsName(), record);
                            break;
                        case 1090:
                            records1090.put(record.getEpicsName(), record);
                            break;
                        default:
                            LOGGER.log(Level.SEVERE, "Received record with unexpected energy ''{0}''", record.getEnergy().toString());
                            throw new RuntimeException(mahErrString);
                    }
                }
                
                // Combine the two records for each cavity into one CavityGsetData object and add it to the map.
                if (records1050.size() != records1090.size()) {
                    LOGGER.log(Level.SEVERE,"Database query did not return identical cavity sets at different energies");
                    throw new RuntimeException(mahErrString);
                }
                for( String epicsName : records1050.keySet()) {
                    if ( ! records1090.containsKey(epicsName) ) {
                        LOGGER.log(Level.SEVERE,"ModAnodeHarvester database has disimilar cavity sets at different energy levels");
                        throw new RuntimeException(mahErrString);
                    }
                    if ( epicsName == null ) {
                        LOGGER.log(Level.SEVERE, "Received Gset record with null EPICS_NAME");
                        throw new RuntimeException(mahErrString);
                    }
                    CavityGsetData cgd = new CavityGsetData(records1050.get(epicsName), records1090.get(epicsName));
                    if ( cgd == null ) {
                        throw new RuntimeException(mahErrString);
                    } else {
                        data.put(epicsName, cgd);
                    }
                }
            }
        } finally {
            SqlUtil.close(rs, pstmt, conn);
        }
        return data;
    }
}
