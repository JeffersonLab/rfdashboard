/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.presentation.controller.ajax;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
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
import org.jlab.rfd.model.CavityDataSpan;
import org.jlab.rfd.presentation.util.DataFormatter;

/**
 *
 * @author adamc
 */
@WebServlet(name = "ModAnodeAJAX", urlPatterns = {"/ajax/mod-anode"})
public class ModAnodeAJAX extends HttpServlet {
    public static final Logger LOGGER = Logger.getLogger(ModAnodeAJAX.class.getName());
    
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
        long ts = new Date().getTime();
        //LOGGER.log(Level.FINEST, "Received followig request parameters: {0}", request.getParameterMap().toString());
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
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
            LOGGER.log(Level.SEVERE, "Error parsing start/end attributes", ex);
            throw new ServletException("Error parseing start/end", ex);
        }

        String out = request.getParameter("out");
        if (out == null) {
            out = "json";
        }

        String factor = request.getParameter("factor");
        if ( factor == null) {
            factor = "linac";
        }

        String timeUnit = request.getParameter("timeUnit");
        if ( timeUnit == null) {
            timeUnit = "week";
        }

        long ts2 = new Date().getTime();        
        CavityService cs = new CavityService();
        CavityDataSpan span;
        try {
            span = cs.getCavityDataSpan(start, end, timeUnit);
        } catch (ParseException ex) {
            throw new ServletException("Error in getting modAnode Data", ex);
        }
        System.out.print(".  ModAnodeAJAX cavity service duration: " + ((new Date().getTime() - ts2) / 1000.0) + "s");


        long ts1 = new Date().getTime();
        SortedMap<Date, SortedMap<String, BigDecimal>> factoredData;
        if ( factor.equals("linac") ) {
            factoredData = span.getModAnodeCountByLinac();
        } else if ( factor.equals("cmtype") ){
            factoredData = span.getModAnodeCountByCMType();
        } else {
            factoredData = span.getModAnodeCountByLinac();
        }
        System.out.print(".  ModAnodeAJAX span.get* duration: " + ((new Date().getTime() - ts1) / 1000.0) + "s");

        PrintWriter pw = response.getWriter();
        try {
            if (out.equals("json")) {
                JsonObject json = DataFormatter.toJsonFromDateMap(factoredData);
                response.setContentType("application/json");
                pw.write(json.toString());
            } else {
                LOGGER.log(Level.WARNING, "Unsupported out format requested - {0}", out);
                throw new ServletException("Unsupported out format requested");
            }
        } catch (ParseException ex) {
            throw new ServletException("Error formatting data", ex);
        }
        System.out.print(".ModAnodeAJAX doGet duration: " + ((new Date().getTime() - ts) / 1000.0) + "s");

    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
//    @Override
//    protected void doPost(HttpServletRequest request, HttpServletResponse response)
//            throws ServletException, IOException {
//        processRequest(request, response);
//    }

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
