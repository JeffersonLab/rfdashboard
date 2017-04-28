/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.presentation.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.rfd.business.service.LemService;
import org.jlab.rfd.model.LemRecord;

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

        Date end = new Date();
        Date start;
        boolean redirectNeeded = false;

        LOGGER.log(Level.FINEST, "EnergyReach controler with received parameters: {0}", request.getParameterMap());        
        if (request.getParameter("end") == null || request.getParameter("end").equals("")) {
            LOGGER.log(Level.FINEST, "No end parameter supplied.  Defaulting to today.");
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
        
        if (request.getParameter("start") == null || request.getParameter("start").equals("") ) {
            // Default to end - four weeks
            start = new Date(end.getTime() - 60 * 60 * 24 * 1000L * 7 * 4);
            request.setAttribute("start", sdf.format(start));
            redirectNeeded = true;
        } else {
            try {
                request.setAttribute("start", sdf.format(sdf.parse(request.getParameter("start"))));
            } catch (ParseException e) {
                LOGGER.log(Level.WARNING, "Error parsing start parameter '{0}'.  Defaulting to -7d", request.getParameter("start"));
                start = new Date(end.getTime() - 60 * 60 * 24 * 1000L * 7);
                request.setAttribute("start", sdf.format(start));
                redirectNeeded = true;
            }
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
                    timeUnit = "week";
                    break;
                default:
                    timeUnit = "day";
            }
            request.setAttribute("timeUnit", timeUnit);
        }

        if (request.getParameter("diffStart") == null || request.getParameter("diffStart").equals("")) {
            request.setAttribute("diffStart", request.getAttribute("start"));
            redirectNeeded = true;
        } else {
            try {
                request.setAttribute("diffStart", sdf.format(sdf.parse(request.getParameter("diffStart"))));
            } catch (ParseException e) {
                LOGGER.log(Level.WARNING, "Error parsing diffStart parameter '{0}'.  Defaulting to value of start", request.getParameter("diffStart"));
                request.setAttribute("diffStart", request.getAttribute("start"));
                redirectNeeded = true;
            }
        }

        if (request.getParameter("diffEnd") == null || request.getParameter("diffEnd").equals("")) {
            request.setAttribute("diffEnd", request.getAttribute("end"));
            redirectNeeded = true;
        } else {
            try {
                request.setAttribute("diffEnd", sdf.format(sdf.parse(request.getParameter("diffEnd"))));
            } catch (ParseException e) {
                LOGGER.log(Level.WARNING, "Error parsing diffEnd parameter '{0}'.  Defaulting to value of end", request.getParameter("diffEnd"));
                request.setAttribute("diffEnd", request.getAttribute("end"));
                redirectNeeded = true;
            }
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

        request.getRequestDispatcher("/WEB-INF/views/energy-reach.jsp").forward(request, response);
    }
}