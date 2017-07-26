/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.presentation.controller.ajax;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.rfd.business.service.CavityService;
import org.jlab.rfd.model.CavityDataSpan;

/**
 *
 * @author adamc
 */
@WebServlet(name = "CavityAjax", urlPatterns = {"/ajax/cavity"})
public class CavityAjax extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(CavityAjax.class.getName());

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
        
        // Support requesting a selection of dates as an alternative to a full range.  This should override the start/end request.
        List<Date> dates = null;
        if ( request.getParameter("date") != null) {
            dates = new ArrayList<>();
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
        
        Date start, end;
        try {
            String eString = request.getParameter("end");
            String sString = request.getParameter("start");
            if ( eString != null) {
                end = sdf.parse((eString));
            } else {
                // Default to "now"
                end = sdf.parse(sdf.format(new Date()));
            }
            if ( sString != null) {
                start = sdf.parse(sString);
            } else {
                // Default to four weeks before end
                start = sdf.parse(sdf.format(new Date(end.getTime() - 60*60*24*1000L*7*4)));
            }
        } catch (ParseException ex) {
            hasError = true;
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            LOGGER.log(Level.SEVERE, "Error parsing start/end attributes", ex);
            response.setContentType("application/json");
            pw.write("{error: 'Error parsing start/end parameters'}");
            return;
        }

        if ( ! start.before(end) ) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            pw.write("{error: 'start cannot be after end'}");
            return;
         }
        if ( end.after(new Date()) ) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.setContentType("application/json");
            pw.write("{\"error\":\"end cannot be a future date\"}");
            return;
        }
        
        String out = request.getParameter("out");
        if (out == null) {
            out = "json";
        } else if ( ! out.equals("json") ) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            LOGGER.log(Level.SEVERE, "Unsupported out value supplied {0}", out);
            response.setContentType("application/json");
            pw.write("{error:\"Unsupported out value '" + out + "' supplied\"}");
            return;
        }

        String timeUnit = request.getParameter("timeUnit");
        if ( timeUnit == null) {
            timeUnit = "week";
        } else if ( ! timeUnit.equals("day") && ! timeUnit.equals("week")) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            LOGGER.log(Level.SEVERE, "Unsupported timeUnit value supplied {0}", timeUnit);
            response.setContentType("application/json");
            pw.write("{error: 'Unsupported timeUnit value \"" + timeUnit + "\" supplied'}");
            return;
        }
        
        CavityService cs = new CavityService();
        CavityDataSpan span;
        if (dates == null) {
            try {
                span = cs.getCavityDataSpan(start, end, timeUnit);
            } catch (ParseException | SQLException ex) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                LOGGER.log(Level.SEVERE, "Error querying cavity data service", ex);
                response.setContentType("application/json");
                pw.write("{error: 'Error querying cavity data service'}");
                return;
            }
        } else {
            try {
                span = cs.getCavityDataSpan(dates);
            } catch (ParseException | SQLException ex) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                LOGGER.log(Level.SEVERE, "Error querying cavity data service", ex);
                response.setContentType("application/json");
                pw.write("{error: 'Error querying cavity data service'}");
                return;
            }
        }
        
        response.setContentType("application/json");
        pw.write(span.toJson().toString());
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
