/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.business.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.rfd.business.util.SqlUtil;
import org.jlab.rfd.model.LemRecord;
import org.jlab.rfd.model.LemSpan;
import org.jlab.rfd.model.LinacName;

/**
 *
 * @author adamc
 */
public class LemService {

    private static final Logger LOGGER = Logger.getLogger(LemService.class.getName());

    /**
     * This generates a LemSpan object based on the start and end dates
     * supplied. Dates are truncated to day precision.
     *
     * @param start The start date used in the database query
     * @param end The end date used in the database query
     * @return Returns the LemSpan object representing the LEM scan data for the
     * requested time period
     * @throws ParseException
     * @throws SQLException
     * @throws IOException
     */
    public LemSpan getLemSpan(Date start, Date end) throws ParseException, SQLException, IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        SimpleDateFormat oraDF = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

        Date s, e;
        try {
            s = sdf.parse(sdf.format(start));
            e = sdf.parse(sdf.format(end));
        } catch (ParseException ex) {
            LOGGER.log(Level.WARNING, "Error parse dates start={0}, end={1}", new Object[]{start, end});
            throw ex;
        }
        Calendar cal = Calendar.getInstance();
        cal.setTime(s);

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        // These number must match column in the lem_scan database (e.g., TRIPS_PER_HOUR_950_MEV)
        int energyStart = 950;
        int energyEnd = 1190;
        int energyStep = 5;
        String sql = "Select SCAN_ID, START_TIME, LINAC";
        for (int i = energyStart; i <= energyEnd; i = i + energyStep) {
            sql = sql + ", TRIPS_PER_HOUR_" + i + "_MEV";
        }
        sql = sql + " FROM LEM_SCAN WHERE START_TIME BETWEEN TO_DATE(?, 'YYYY/MM/DD HH24:MI:SS')"
                + " AND TO_DATE(?, 'YYYY/MM/DD HH24:MI:SS') ORDER BY START_TIME, LINAC ASC";
        //LOGGER.log(Level.FINEST, "SQL query is {0}", sql);

        ArrayList<LemRecord> data = new ArrayList<>();
        try {
            conn = SqlUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, oraDF.format(s));
            pstmt.setString(2, oraDF.format(e));
            rs = pstmt.executeQuery();

            ArrayList<BigDecimal> tripRates;
            Date lastTime = null;
            Date currTime;
            LinacName currLinac;
            boolean hasNorth = false;
            boolean hasSouth = false;
            while (rs.next()) {
                // Use the more precise date string for plotting.  Can truncate later if desired.
                currTime = sdf.parse(rs.getString(2));
                currLinac = LinacName.valueOf(rs.getString(3));
                // We only want to get a single data point for each day
                if (!currTime.equals(lastTime)) {
                    hasNorth = false;
                    hasSouth = false;
                    switch (currLinac) {
                        case North:
                            hasNorth = true;
                            data.add(processResult(rs, currTime, currLinac, energyStart, energyEnd, energyStep));
                            break;
                        case South:
                            hasSouth = true;
                            data.add(processResult(rs, currTime, currLinac, energyStart, energyEnd, energyStep));
                            break;
                        default:
                            throw new IOException("Received unexpected linac name from LEM_SCAN database - {0}");
                    }
                } else {
                    if (!hasSouth && currLinac.equals(LinacName.South)) {
                        data.add(processResult(rs, currTime, currLinac, energyStart, energyEnd, energyStep));
                        hasSouth = true;
                    } else if (!hasNorth && currLinac.equals(LinacName.North)) {
                        data.add(processResult(rs, currTime, currLinac, energyStart, energyEnd, energyStep));
                        hasNorth = true;
                    }
                }
                lastTime = currTime;
            }
        } finally {
            SqlUtil.close(rs, pstmt, conn);
        }

        // Create a span from the list.  This could be more effecient by adding the processResult call directly to the span, but
        // I'm rushing a little.
        LemSpan span = new LemSpan();
        span.addList(data);
        return span;
    }

    private LemRecord processResult(ResultSet rs, Date date, LinacName linac, int energyStart, int energyEnd, int energyStep) throws SQLException {

        long scanId = rs.getLong(1);
        Map<Integer, Double> tripRates = new TreeMap<>();
        int i = 4;
        for (int e = energyStart; e <= energyEnd; e = e + energyStep) {
            String tripRate = rs.getString(i);
            if (tripRate != null) {
                tripRates.put(e, Double.valueOf(tripRate));
            } else {
                tripRates.put(e, null);
            }
            i++;
        }
        return new LemRecord(scanId, date, linac, tripRates);
    }
}
