/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.presentation.controller.ajax.ModAnodeHarvester;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonObject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.rfd.business.service.ModAnodeHarvesterService;
import org.jlab.rfd.presentation.util.RequestParamUtil;
import org.jlab.rfd.model.ModAnodeHarvester.LinacDataSpan;
import org.jlab.rfd.model.TimeUnit;
import org.jlab.rfd.presentation.util.DataFormatter;

/**
 *
 * @author adamc
 */
@WebServlet(name = "LinacAjax", urlPatterns = {"/ajax/linac"})
public class LinacAjax extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(LinacAjax.class.getName());

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
        PrintWriter pw = response.getWriter();
        boolean hasError = false;
        
        long ts = new Date().getTime();
        //LOGGER.log(Level.FINEST, "Received followig request parameters: {0}", request.getParameterMap().toString());

        TimeUnit timeUnit = RequestParamUtil.processTimeUnit(request, TimeUnit.WEEK);
        if (timeUnit == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            LOGGER.log(Level.SEVERE, "Unsupported timeUnit value supplied {0}", timeUnit);
            response.setContentType("application/json");
            pw.write("{error: 'Unsupported timeUnit value \"" + timeUnit + "\" supplied'}");
            return;
        }
        
        // Support requesting a selection of dates as an alternative to a full range.  This should override the start/end request.
        Set<Date> dates = null;
        if ( request.getParameter("date") != null) {
            dates = new HashSet<>();
            for (String date : request.getParameterValues("date")) {
                if (date != null) {
                    try {
                        dates.add(sdf.parse(date));
                    } catch (ParseException ex) {
                        LOGGER.log(Level.WARNING, "Error parsing dates parameter '" + date + "'", ex);
                    }
                }
            }
            if (dates.isEmpty()) {
                hasError = true;
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                LOGGER.log(Level.SEVERE, "Error.  No valid date requested");
                response.setContentType("application/json");
                pw.write("{error: 'Error. No valid date requested'}");
                return;
            }
        }
        
        Map<String, Date> startEnd;
        Date start = null;
        Date end = null;
        if (dates == null) {
            try {
                startEnd = RequestParamUtil.processStartEnd(request, TimeUnit.WEEK, 4);
                start = startEnd.get("start");
                end = startEnd.get("end");
            } catch (ParseException ex) {
                hasError = true;
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                LOGGER.log(Level.SEVERE, "Error parsing start/end attributes", ex);
                response.setContentType("application/json");
                pw.write("{error: 'Error parsing start/end parameters'}");
                return;
            }
        }
        
        String[] valid = {"json", "flot"};
        String out = RequestParamUtil.processOut(request, valid, "json");
        if (out == null ) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            LOGGER.log(Level.SEVERE, "Unsupported out value supplied {0}", out);
            response.setContentType("application/json");
            pw.write("{error:\"Unsupported out value '" + out + "' supplied\"}");
            return;            
        }
        
        ModAnodeHarvesterService mahs = new ModAnodeHarvesterService();
        LinacDataSpan span;
        if (dates == null) {
            try {
                span = mahs.getLinacDataSpan(start, end, timeUnit);
            } catch (ParseException | SQLException ex) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                LOGGER.log(Level.SEVERE, "Error querying linac data service", ex);
                response.setContentType("application/json");
                pw.write("{error: 'Error querying linac data service'}");
                return;
            }
        } else {
            try {
                span = mahs.getLinacDataSpan(dates);
            } catch (ParseException | SQLException ex) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                LOGGER.log(Level.SEVERE, "Error querying linac data service", ex);
                response.setContentType("application/json");
                pw.write("{error: 'Error querying linac data service'}");
                return;
            }
        }
        
        switch (out) {
            case "json":
                response.setContentType("application/json");
                JsonObject json = Json.createObjectBuilder().add("data", span.toJson()).build();
                pw.write(json.toString());
                break;
            case "flot":
                response.setContentType("application/json");
                SortedMap<Date, SortedMap<String, BigDecimal>> factoredData = span.getTripRates();
                try {
                    JsonObject flot = DataFormatter.toFlotFromDateMap(factoredData);
                    pw.write(flot.toString());
                } catch (ParseException ex) {
                    throw new ServletException("Error formatting data", ex);
                }
        }
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>

}
