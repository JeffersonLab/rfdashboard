/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.presentation.controller;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.JsonObject;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.rfd.business.service.ModAnodeService;
import org.jlab.rfd.model.ModAnodeDataSpan;

/**
 *
 * @author adamc
 */
@WebServlet(name = "ModAnode", urlPatterns = {"/mod-anode"})
public class ModAnode extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(ModAnode.class.getName());

    /**
     * Processes requests for both HTTP <code>GET</code> and <code>POST</code>
     * methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-M-dd");

        Date end = new Date();
        Date start;

        LOGGER.log(Level.FINEST, "ModAode controler with received parameters: {0}", request.getParameterMap());
        
        if (request.getParameter("end") == null) {
            LOGGER.log(Level.FINEST, "No end parameter supplied.  Defaulting to now.");
            request.setAttribute("end", sdf.format(end));
        } else {
            try {
                end = sdf.parse(request.getParameter("end"));
                request.setAttribute("end", sdf.format(end));
            } catch (ParseException e) {
                end = new Date();  // In case something bad happend during try.
                LOGGER.log(Level.WARNING, "Error parsing end parameter '{0}'.  Defaulting to now", request.getParameter("end"));
                request.setAttribute("end", sdf.format(end));
            }
        }
        
        if (request.getParameter("start") == null ) {
            start = new Date(end.getTime() - 60 * 60 * 24 * 1000L * 7);
            request.setAttribute("start", sdf.format(start));
        } else {
            try {
                request.setAttribute("start", sdf.format(sdf.parse(request.getParameter("start"))));
            } catch (ParseException e) {
                LOGGER.log(Level.WARNING, "Error parsing start parameter '{0}'.  Defaulting to -7d", request.getParameter("start"));
                start = new Date(end.getTime() - 60 * 60 * 24 * 1000L * 7);
                request.setAttribute("start", sdf.format(start));
            }
        }

        LOGGER.log(Level.FINEST,
                "Start: {0} - End: {1}", new Object[]{request.getAttribute("start"),
                     request.getAttribute("end")
                }
        );
        request.getRequestDispatcher(
                "/WEB-INF/views/mod-anode.jsp").forward(request, response);
    }

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
        processRequest(request, response);
    }

    /**
     * Handles the HTTP <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
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
