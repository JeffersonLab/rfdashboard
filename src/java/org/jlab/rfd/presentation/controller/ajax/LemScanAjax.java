/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.presentation.controller.ajax;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.SortedMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.JsonObject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.rfd.business.service.LemService;
import org.jlab.rfd.model.LemSpan;
import org.jlab.rfd.model.TimeUnit;
import org.jlab.rfd.presentation.util.DataFormatter;
import org.jlab.rfd.presentation.util.RequestParamUtil;

/**
 *
 * @author adamc
 */
@WebServlet(name = "LemScanAjax", urlPatterns = {"/ajax/lem-scan"})
public class LemScanAjax extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(LemScanAjax.class.getName());

    /**
     * Handles the HTTP <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        Date start, end;
        
        String type = request.getParameter("type");
        if (type == null) {
            LOGGER.log(Level.WARNING, "type parameter is required.  Supported options are day-scan, reach-scan");
            throw new ServletException("Error: type parameter is required.  Supported options are day-scan, reach-scan");

            // day-scan type request
        } else if (type.equals("day-scan")) {
            // We only want one day of lem scans, but the getLemSpan method wants start and end with end being exclusive.
            // Set end to start + 1day
            String date = request.getParameter("date");
            if (date == null) {
                try {
                    start = sdf.parse(sdf.format(new Date()));
                    end = sdf.parse(sdf.format(new Date(start.getTime() + 60 * 60 * 24 * 1000L)));
                } catch (ParseException ex) {
                    LOGGER.log(Level.SEVERE, "Error parsing supplied date", ex);
                    throw new ServletException("Error parsing supplied date", ex);
                }
            } else {
                try {
                    start = sdf.parse(date);
                    end = sdf.parse(sdf.format(new Date(start.getTime() + 60 * 60 * 24 * 1000L)));
                } catch (ParseException ex) {
                    LOGGER.log(Level.SEVERE, "Error parsing supplied date", ex);
                    throw new ServletException("Error parsing supplied date", ex);
                }
            }

            LemService ls = new LemService();
            LemSpan span;
            try {
                span = ls.getLemSpan(start, end);
            } catch (SQLException | ParseException ex) {
                LOGGER.log(Level.SEVERE, "Error querying RF Gradient Team database", ex);
                throw new ServletException("Error querying RF Gradient Team database", ex);
            }

            // Defaults to json, anything else will throw an exception
            String out = request.getParameter("out");
            if (out == null) {
                out = "json";
            }

            // The Lem service in start include and end exclusive.  Decrement end by one day to show the last day of data
            LOGGER.log(Level.FINEST, "Start: {0}, End: {1}", new Object[]{start, end});
            SortedMap<Integer, SortedMap<String, BigDecimal>> tripRates = span.getTripRateCurve(start);
            PrintWriter pw = response.getWriter();
            try {
                if (out.equals("json")) {
                    JsonObject json = DataFormatter.toFlotFromIntMap(tripRates);
                    response.setContentType("application/json");
                    pw.write(json.toString());
                } else {
                    LOGGER.log(Level.WARNING, "Unsupported out format requested - {0}", out);
                    throw new ServletException("Unsupported out format requested");
                }
            } catch (ParseException ex) {
                throw new ServletException("Error formatting data", ex);
            }
            
            
        // reach-scan type request
        } else if (type.equals("reach-scan")) {
            Date last;
            Map<String, Date> startEnd;
            
            try {
                startEnd = RequestParamUtil.processStartEnd(request, TimeUnit.DAY, 7);
                start = startEnd.get("start");
                end = startEnd.get("end");
            } catch (ParseException ex) {
                LOGGER.log(Level.SEVERE, "Error parsing start/end parameters", ex);
                throw new ServletException("Error parseing start/end", ex);
            }

            String[] valid = {"flot"};
            String out = RequestParamUtil.processOut(request, valid, "flot");
            if (out == null) {
                LOGGER.log(Level.SEVERE, "Error parsing out parameter");
                throw new ServletException("Error parsing out parameter");
            }

            LemService ls = new LemService();
            LemSpan span;
            try {
                span = ls.getLemSpan(start, end);
            } catch (SQLException | ParseException ex) {
                LOGGER.log(Level.SEVERE, "Error querying RF Gradient Team database", ex);
                throw new ServletException("Error querying RF Gradient Team database", ex);
            }

            // The Lem service in start include and end exclusive.  Decrement end by one day to show the last day of data
            SortedMap<Date, SortedMap<String, BigDecimal>> reach = span.getEnergyReach();
            PrintWriter pw = response.getWriter();
            try {
                if (out.equals("flot")) {
                    JsonObject json = DataFormatter.toFlotFromDateMap(reach);
                    response.setContentType("application/json");
                    pw.write(json.toString());
                } else {
                    LOGGER.log(Level.WARNING, "Unsupported out format requested - {0}", out);
                    throw new ServletException("Unsupported out format requested");
                }
            } catch (ParseException ex) {
                throw new ServletException("Error formatting data", ex);
            }
        }
    }
}
