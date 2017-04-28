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

/**
 *
 * @author adamc
 */
@WebServlet(name = "ModAnode", urlPatterns = {"/mod-anode"})
public class ModAnode extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ModAnode.class.getName());
    
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Date now = new Date();
        LOGGER.log(Level.FINEST, "Starting ModAnode processRequest method");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Date end = new Date();
        Date start, tableDate;
        boolean redirectNeeded = false;

        LOGGER.log(Level.FINEST, "ModAode controler with received parameters: {0}", request.getParameterMap());

        if (request.getParameter("end") == null || request.getParameter("end").equals("")) {
            LOGGER.log(Level.FINEST, "No end parameter supplied.  Defaulting to now.");
            request.setAttribute("end", sdf.format(end));
            redirectNeeded = true;
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
                request.setAttribute("start", sdf.format(sdf.parse(request.getParameter("start"))));
            } catch (ParseException e) {
                LOGGER.log(Level.WARNING, "Error parsing start parameter '{0}'.  Defaulting to -7d", request.getParameter("start"));
                start = new Date(end.getTime() - 60 * 60 * 24 * 1000L * 7);
                request.setAttribute("start", sdf.format(start));
                redirectNeeded = true;
            }
        }

        if (request.getParameter("tableDate") == null || request.getParameter("tableDate").equals("")) {
            // Default to end - four weeks
            tableDate = end;
            request.setAttribute("tableDate", sdf.format(tableDate));
            redirectNeeded = true;
        } else {
            try {
                request.setAttribute("tableDate", sdf.format(sdf.parse(request.getParameter("tableDate"))));
            } catch (ParseException e) {
                LOGGER.log(Level.WARNING, "Error parsing tableDate parameter '{0}'.  Defaulting to end", request.getParameter("tableDate"));
                tableDate = end;
                request.setAttribute("tableDate", sdf.format(tableDate));
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
                default:
                    timeUnit = "week";
            }
            request.setAttribute("timeUnit", timeUnit);
        }

//        LOGGER.log(Level.FINEST,
//                "Start: {0} - End: {1}", new Object[]{request.getAttribute("start"),
//                     request.getAttribute("end")
//                }
//        );
        if (redirectNeeded) {
            String redirectUrl;
            try {
                redirectUrl = request.getContextPath()
                        + "/mod-anode?start=" + URLEncoder.encode((String) request.getAttribute("start"), "UTF-8")
                        + "&end=" + URLEncoder.encode((String) request.getAttribute("end"), "UTF-8")
                        + "&tableDate=" + URLEncoder.encode((String) request.getAttribute("tableDate"), "UTF-8")
                        + "&timeUnit=" + URLEncoder.encode((String) request.getAttribute("timeUnit"), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                throw new RuntimeException("JVM doesn't support UTF-8");
            }
            response.sendRedirect(response.encodeRedirectURL(redirectUrl));
            return;
        }

        request.getRequestDispatcher("/WEB-INF/views/mod-anode.jsp").forward(request, response);
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
