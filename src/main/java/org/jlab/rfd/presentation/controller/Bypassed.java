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
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.JsonObject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.rfd.business.service.CavityService;
import org.jlab.rfd.model.TimeUnit;
import org.jlab.rfd.presentation.util.DataFormatter;
import org.jlab.rfd.presentation.util.ParamChecker;

/**
 *
 * @author adamc
 */
@WebServlet(name = "Bypassed", urlPatterns = {"/bypassed"})
public class Bypassed extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(Bypassed.class.getName());

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

        LOGGER.log(Level.FINEST, "Bypassed controler with received parameters: {0}", request.getParameterMap());

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

        if (request.getParameter("tableDate") == null || request.getParameter("tableDate").equals("")) {
            // Default to end - four weeks
            tableDate = end;
            request.setAttribute("tableDate", sdf.format(tableDate));
            redirectNeeded = true;
        } else {
            try {
                tableDate = sdf.parse(request.getParameter("tableDate"));
                request.setAttribute("tableDate", sdf.format(tableDate));
            } catch (ParseException e) {
                LOGGER.log(Level.WARNING, "Error parsing tableDate parameter '{0}'.  Defaulting to end", request.getParameter("tableDate"));
                tableDate = end;
                request.setAttribute("tableDate", sdf.format(tableDate));
                redirectNeeded = true;
            }
        }
        ParamChecker.validateDate(tableDate);

        TimeUnit tu = TimeUnit.WEEK;
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
                    timeUnit = "week";
                    tu = TimeUnit.WEEK;
            }
            request.setAttribute("timeUnit", timeUnit);
        }

        LOGGER.log(Level.FINEST,
                "Start: {0} - End: {1}", new Object[]{request.getAttribute("start"),
                    request.getAttribute("end")
                }
        );

        if (redirectNeeded) {
            String redirectUrl;
            try {
                redirectUrl = request.getContextPath()
                        + "/bypassed?start=" + URLEncoder.encode((String) request.getAttribute("start"), "UTF-8")
                        + "&end=" + URLEncoder.encode((String) request.getAttribute("end"), "UTF-8")
                        + "&tableDate=" + URLEncoder.encode((String) request.getAttribute("tableDate"), "UTF-8")
                        + "&timeUnit=" + URLEncoder.encode((String) request.getAttribute("timeUnit"), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("JVM doesn't support UTF-8");
            }
            response.sendRedirect(response.encodeRedirectURL(redirectUrl));
            return;
        }

        CavityService cs = new CavityService();
        JsonObject bypassedCMType, bypassedLinac, tableData;
        
        List<Date> date = new ArrayList<>();
        date.add(tableDate);
        try {
            // J. Benesch requested this type of mapping
            Map<String, String> typeMapper = new HashMap<>();
            typeMapper.put("C100", "C100");
            typeMapper.put("F100", "C100");
            typeMapper.put("C25", "C25");
            typeMapper.put("C50", "C50");
            typeMapper.put("C50T", "C50");
            typeMapper.put("C75", "C75");

            bypassedCMType = DataFormatter.toFlotFromDateMap(cs.getCavityDataSpan(start, end, tu).getBypassedCountByCMType(typeMapper));
            bypassedLinac = DataFormatter.toFlotFromDateMap(cs.getCavityDataSpan(start, end, tu).getBypassedCountByLinac());
            tableData = cs.getCavityDataSpan(date).toJson();
        } catch (ParseException | SQLException ex) {
            LOGGER.log(Level.WARNING, "Error querying cavity data", ex);
            throw new ServletException("Error querying cavity data");
        }

        request.setAttribute("bypassedCMType", bypassedCMType.toString());
        request.setAttribute("bypassedLinac", bypassedLinac.toString());
        request.setAttribute("tableData", tableData.toString());
        
        request.getRequestDispatcher("/WEB-INF/views/bypassed.jsp").forward(request, response);
    }
}
