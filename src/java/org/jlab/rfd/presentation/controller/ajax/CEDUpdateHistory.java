/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.presentation.controller.ajax;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.ParseException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.rfd.business.service.CEDUpdateHistoryService;
import org.jlab.rfd.model.CEDElementUpdateHistory;

/**
 *
 * @author adamc
 */
@WebServlet(name = "CEDElementUpdateHistory", urlPatterns = {"/ajax/ced-update-history"})
public class CEDUpdateHistory extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(CEDUpdateHistory.class.getName());

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

        response.setContentType("application/json");
        PrintWriter pw = response.getWriter();

        String elem = request.getParameter("elem");
        if (elem == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            LOGGER.log(Level.SEVERE, "elem parameter required");
            pw.write("{error: 'elem required'}");
            return;
        }
        String[] props = request.getParameterValues("prop");
        if (props == null || props.length == 0) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            LOGGER.log(Level.SEVERE, "prop parameter required");
            pw.write("{error: 'prop required'}");
            return;
        }
        
        List<String> propList = Arrays.asList(props);
        CEDUpdateHistoryService cuhs = new CEDUpdateHistoryService();
        try {
            CEDElementUpdateHistory elemHistory = cuhs.getElementUpdateHistory(elem, propList);
            pw.write(elemHistory.toJsonByDate(propList).toString());
        } catch (ParseException ex) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            LOGGER.log(Level.SEVERE, "Error querying data from CED");
            pw.write("{error: 'Error querying data from CED'}");
            return;
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
