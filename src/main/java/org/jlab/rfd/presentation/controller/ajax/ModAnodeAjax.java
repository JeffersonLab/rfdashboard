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
import org.jlab.rfd.business.service.CavityService;
import org.jlab.rfd.presentation.util.RequestParamUtil;
import org.jlab.rfd.model.CavityDataSpan;
import org.jlab.rfd.model.TimeUnit;
import org.jlab.rfd.presentation.util.DataFormatter;

/**
 *
 * @author adamc
 */
@WebServlet(name = "ModAnodeAJAX", urlPatterns = {"/ajax/mod-anode"})
public class ModAnodeAjax extends HttpServlet {
    public static final Logger LOGGER = Logger.getLogger(ModAnodeAjax.class.getName());
    
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
        //LOGGER.log(Level.FINEST, "Received following request parameters: {0}", request.getParameterMap().toString());
        
        Map<String, Date> startEnd;
        Date start;
        Date end;

        try {
            startEnd = RequestParamUtil.processStartEnd(request, TimeUnit.WEEK, 4);
            start = startEnd.get("start");
            end = startEnd.get("end");
        } catch (ParseException ex) {
            LOGGER.log(Level.SEVERE, "Error parsing start/end attributes", ex);
            throw new ServletException("Error parsing start/end", ex);
        }

        String[] valid = {"flot"};
        String out = RequestParamUtil.processOut(request, valid, "flot");
        if (out == null) {
            throw new ServletException("Unsupported out format requested");
        }

        String factor = request.getParameter("factor");
        if ( factor == null) {
            factor = "linac";
        }

        TimeUnit timeUnit = RequestParamUtil.processTimeUnit(request, TimeUnit.WEEK);
        if ( timeUnit == null ) {
            throw new ServletException("Unsupported timeUnit requested");
        }
                
        CavityService cs = new CavityService();
        CavityDataSpan span;
        try {
            span = cs.getCavityDataSpan(start, end, timeUnit);
        } catch (ParseException | SQLException ex) {
            throw new ServletException("Error in getting modAnode Data", ex);
        }

        SortedMap<Date, SortedMap<String, Integer>> factoredData;
        if (factor.equals("cmtype")) {
            factoredData = span.getModAnodeCountByCMType(null);
        } else {
            factoredData = span.getModAnodeCountByLinac(true);
        }

        PrintWriter pw = response.getWriter();
        try {
            if (out.equals("flot")) {
                JsonObject json = DataFormatter.toFlotFromDateMapInt(factoredData);
                response.setContentType("application/json");
                pw.write(json.toString());
            }
        } catch (ParseException ex) {
            throw new ServletException("Error formatting data", ex);
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
