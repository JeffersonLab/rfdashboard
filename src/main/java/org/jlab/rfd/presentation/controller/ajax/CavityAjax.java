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
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.rfd.business.service.CavityService;
import org.jlab.rfd.presentation.util.RequestParamUtil;
import org.jlab.rfd.model.CavityDataSpan;
import org.jlab.rfd.model.TimeUnit;

/**
 *
 * @author adamc
 */
@WebServlet(name = "CavityAjax", urlPatterns = {"/ajax/cavity"})
public class CavityAjax extends HttpServlet {
    private static final Logger LOGGER = Logger.getLogger(CavityAjax.class.getName());

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
        response.setContentType("application/json");

        try (PrintWriter pw = response.getWriter()) {
            TimeUnit timeUnit = RequestParamUtil.processTimeUnit(request, TimeUnit.WEEK);
            if (timeUnit == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                LOGGER.log(Level.SEVERE, "Unsupported timeUnit value supplied 'null'");
                pw.write("{\"error\": \"Unsupported timeUnit value 'null' supplied\"}");
                return;
            }

            // Support requesting a selection of dates as an alternative to a full range.  This should override the start/end request.
            List<Date> dates;
            try {
                dates = RequestParamUtil.processDate(request);
            } catch (ParseException ex) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                LOGGER.log(Level.SEVERE, "Error parsing start/end attributes", ex);
                pw.write("{\"error\": \"Error parsing start/end parameters\"}");
                return;
            }
            if (dates != null && dates.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                LOGGER.log(Level.SEVERE, "Error.  No valid date requested");
                pw.write("{\"error\": \"Error. No valid date requested\"}");
                return;
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
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    LOGGER.log(Level.SEVERE, "Error parsing start/end attributes", ex);
                    pw.write("{\"error\": \"Error parsing start/end parameters\"}");
                    return;
                }
            }

            String[] valid = {"json"};
            String out = RequestParamUtil.processOut(request, valid, "json");
            if (out == null) {
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                LOGGER.log(Level.SEVERE, "Unsupported 'out' value supplied.");
                pw.write("{\"error\":\"Unsupported 'out' value supplied.  Valid = {" + String.join(", ", valid) + "}");
                return;
            }

            CavityService cs = new CavityService();
            CavityDataSpan span;
            try {
                if (dates == null) {
                    span = cs.getCavityDataSpan(start, end, timeUnit);
                } else {
                    span = cs.getCavityDataSpan(dates);
                }
            } catch (ParseException | SQLException | IOException ex) {
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                LOGGER.log(Level.SEVERE, "Error querying cavity data service", ex);
                pw.write("{\"error\": \"Error querying cavity data service: " + ex.getMessage() + "\"}");
                return;
            }

            pw.write(span.toJson().toString());
        }
    }
}
