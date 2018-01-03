/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.presentation.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.JsonObject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.rfd.business.service.CavityService;
import org.jlab.rfd.presentation.util.ParamChecker;

/**
 *
 * @author adamc
 */
@WebServlet(name = "Cryo", urlPatterns = {"/cryo"})
public class Cryo extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(Cryo.class.getName());

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

        Date end, start, tableDate;
        boolean redirectNeeded = false;

        LOGGER.log(Level.FINEST, "Cryo controler with received parameters: {0}", request.getParameterMap());

        if (request.getParameter("end") == null || request.getParameter("end").equals("")) {
            LOGGER.log(Level.FINEST, "No end parameter supplied.  Defaulting to now.");
            end = new Date();
            redirectNeeded = true;
            request.setAttribute("end", sdf.format(end));
        } else {
            try {
                end = sdf.parse(request.getParameter("end"));
                request.setAttribute("end", sdf.format(end));
            } catch (ParseException e) {
                end = new Date();  // In case something bad happend during try.
                LOGGER.log(Level.WARNING, "Error parsing end parameter '{0}'.  Defaulting to now", request.getParameter("end"));
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
                    break;
                case "week":
                default:
                    timeUnit = "week";
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
                        + "/cryo?start=" + URLEncoder.encode((String) request.getAttribute("start"), "UTF-8")
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

        CavityService cs = new CavityService();
        JsonObject cavityData;
        List<Date> tableDates = new ArrayList<>();
        tableDates.add(diffStart);
        tableDates.add(diffEnd);
        try {
            cavityData = cs.getCavityDataSpan(tableDates).toJson();
        } catch (ParseException | SQLException ex) {
            LOGGER.log(Level.WARNING, "Error querying cavity datasources");
            throw new ServletException("Error querying cavity datasources");
        }

        request.setAttribute("cavityData", cavityData);

        request.getRequestDispatcher("/WEB-INF/views/cryo.jsp").forward(request, response);
    }
}
