/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.presentation.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.SortedMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.JsonObject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.rfd.business.service.CavityService;
import org.jlab.rfd.business.service.LemService;
import org.jlab.rfd.business.util.DateUtil;
import org.jlab.rfd.config.AppConfig;
import org.jlab.rfd.model.CavityDataSpan;
import org.jlab.rfd.model.LemSpan;
import org.jlab.rfd.model.TimeUnit;
import org.jlab.rfd.presentation.util.DataFormatter;
import org.jlab.rfd.presentation.util.ParamChecker;

/**
 *
 * @author adamc
 */
@WebServlet(name = "EnergyReach", urlPatterns = {"/energy-reach"})
public class EnergyReach extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(EnergyReach.class.getName());

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
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

        Date end, start;
        boolean redirectNeeded = false;

        LOGGER.log(Level.FINEST, "EnergyReach controler with received parameters: {0}", request.getParameterMap());
        if (request.getParameter("end") == null || request.getParameter("end").equals("")) {
            LOGGER.log(Level.FINEST, "No end parameter supplied.  Defaulting to today.");
            end = new Date();
            request.setAttribute("end", sdf.format(end));
            redirectNeeded = true;
        } else {
            try {
                end = sdf.parse(request.getParameter("end"));
                request.setAttribute("end", sdf.format(end));
            } catch (ParseException e) {
                end = new Date();  // In case something bad happend during try.
                LOGGER.log(Level.WARNING, "Error parsing end parameter '{0}'.  Defaulting to today", request.getParameter("end"));
                request.setAttribute("end", sdf.format(end));
                redirectNeeded = true;
            }
        }

        if (request.getParameter("start") == null || request.getParameter("start").equals("")) {
            // Default to end - four weeks
            start = new Date(end.getTime() - 60 * 60 * 24 * 1000L * 7 * 4);
            request.setAttribute("start", sdf.format(start));
            redirectNeeded = true;
        } else {
            try {
                start = sdf.parse(request.getParameter("start"));
                request.setAttribute("start", sdf.format(start));
            } catch (ParseException e) {
                LOGGER.log(Level.WARNING, "Error parsing start parameter '{0}'.  Defaulting to -7d", request.getParameter("start"));
                start = new Date(end.getTime() - 60 * 60 * 24 * 1000L * 7);
                request.setAttribute("start", sdf.format(start));
                redirectNeeded = true;
            }
        }

        // Throws a RuntimeException if invalid
        if (start != null && end != null) {
            ParamChecker.validateStartEnd(start, end);
        }

        TimeUnit tu = TimeUnit.DAY;
        if (request.getParameter("timeUnit") == null || request.getParameter("timeUnit").equals("")) {
            // Default to week
            LOGGER.log(Level.FINEST, "No timeUnit parameter supplied.  Defaulting to 'week'.");
            request.setAttribute("timeUnit", "week");
            redirectNeeded = true;
        } else {
            String timeUnit;
            switch (request.getParameter("timeUnit")) {
                case "day":
                    timeUnit = "day";
                    tu = TimeUnit.DAY;
                    break;
                case "week":
                    timeUnit = "week";
                    tu = TimeUnit.WEEK;
                    break;
                default:
                    timeUnit = "day";
                    tu = TimeUnit.DAY;
            }
            request.setAttribute("timeUnit", timeUnit);
        }

        Date diffStart, diffEnd;
        if (request.getParameter("diffStart") == null || request.getParameter("diffStart").equals("")) {
            diffStart = start;
            request.setAttribute("diffStart", request.getAttribute("start"));
            redirectNeeded = true;
        } else {
            try {
                diffStart = sdf.parse(request.getParameter("diffStart"));
                request.setAttribute("diffStart", sdf.format(diffStart));
            } catch (ParseException e) {
                LOGGER.log(Level.WARNING, "Error parsing diffStart parameter '{0}'.  Defaulting to value of start", request.getParameter("diffStart"));
                diffStart = start;
                request.setAttribute("diffStart", request.getAttribute("start"));
                redirectNeeded = true;
            }
        }

        if (request.getParameter("diffEnd") == null || request.getParameter("diffEnd").equals("")) {
            diffEnd = end;
            request.setAttribute("diffEnd", request.getAttribute("end"));
            redirectNeeded = true;
        } else {
            try {
                diffEnd = sdf.parse(request.getParameter("diffEnd"));
                request.setAttribute("diffEnd", sdf.format(diffEnd));
            } catch (ParseException e) {
                LOGGER.log(Level.WARNING, "Error parsing diffEnd parameter '{0}'.  Defaulting to value of end", request.getParameter("diffEnd"));
                diffEnd = end;
                request.setAttribute("diffEnd", request.getAttribute("end"));
                redirectNeeded = true;
            }
        }
        // Throws a RuntimeException if invalid
        if (diffStart != null && diffEnd != null) {
            ParamChecker.validateStartEnd(diffStart, diffEnd);
        }

        LOGGER.log(Level.FINEST, "Start: {0} - End: {1}", new Object[]{request.getAttribute("start"), request.getAttribute("end")});

        if (redirectNeeded) {
            String redirectUrl;
            try {
                redirectUrl = request.getContextPath()
                        + "/energy-reach?start=" + URLEncoder.encode((String) request.getAttribute("start"), "UTF-8")
                        + "&end=" + URLEncoder.encode((String) request.getAttribute("end"), "UTF-8")
                        + "&diffStart=" + URLEncoder.encode((String) request.getAttribute("diffStart"), "UTF-8")
                        + "&diffEnd=" + URLEncoder.encode((String) request.getAttribute("diffEnd"), "UTF-8")
                        + "&timeUnit=" + URLEncoder.encode((String) request.getAttribute("timeUnit"), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("JVM doesn't support UTF-8");
            }
            response.sendRedirect(response.encodeRedirectURL(redirectUrl));
            return;
        }

        LemService ls = new LemService();
        JsonObject energyReach;
        JsonObject dayScan;
        try {
            // getLemSpan searches for energy reaches between the two dates, but requestors really want the number for the last day
            // which was almost certainly not done at exactly midight the start of that day.
            Date endEffective = DateUtil.getNextDay(end);
            SortedMap<Date, SortedMap<String, BigDecimal>> reach = ls.getLemSpan(start, endEffective).getEnergyReach();

            energyReach = DataFormatter.toFlotFromDateMap(reach);
            LemSpan lemSpan = ls.getLemSpan(diffEnd, DateUtil.getNextDay(diffEnd));
            SortedMap<Integer, SortedMap<String, BigDecimal>> tripRates = lemSpan.getTripRateCurve(diffEnd);
            dayScan = DataFormatter.toFlotFromIntMap(tripRates);
        } catch (ParseException | SQLException ex) {
            LOGGER.log(Level.WARNING, "Error querying LEM scan database", ex);
            throw new ServletException("Error querying LEM scan database");
        }
        // This is a little hacky, but there was already lots of client code written to manage the JSON objects
        request.setAttribute("energyReach", energyReach == null ? "undefined" : energyReach.toString());
        request.setAttribute("dayScan", dayScan == null ? "undefined" : dayScan.toString());

        CavityService cs = new CavityService();
        JsonObject cavityData;
        List<Date> dates = new ArrayList<>();
        dates.add(diffStart);
        dates.add(diffEnd);
        try {
            cavityData = cs.getCavityDataSpan(dates).toJson();
        } catch (Exception ex) {
            LOGGER.log(Level.WARNING, "Error querying cavity datasources", ex);
            throw new ServletException("Error querying cavity datasources");
        }
        request.setAttribute("cavityData", cavityData.toString());
        request.setAttribute("myaURL", AppConfig.getAppConfig().getMYAUrl());

        request.getRequestDispatcher("/WEB-INF/views/energy-reach.jsp").forward(request, response);
    }
}
