/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.presentation.controller.ajax;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.rfd.business.util.EmailUtil;
import org.jlab.rfd.business.util.SessionUtil;

/**
 *
 * @author adamc
 */
@WebServlet(name = "Feedback", urlPatterns = {"/ajax/feedback"})
public class Feedback extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(
            Feedback.class.getName());
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
        
        String username = SessionUtil.checkAuthenticated(request);
        String[] feedbackUsers = request.getServletContext().getInitParameter("FeedbackUsers").split(",");
        
        String body = request.getParameter("body");
        String subject = request.getParameter("subject");
        
        String errorReason = null;
        try {
            EmailUtil.sendEmail(feedbackUsers, username, subject, body);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Unable to send email", e);
            errorReason = e.getClass().getSimpleName() + ": "  + e.getMessage();
        }
        
        String xml;
        if ( errorReason == null ) {
            xml = "<response><span class=\"status\">Success</span></response>";
        } else {
            xml = "<response><span class=\"status\">Error</span><span "
                    + "class=\"reason\">" + errorReason + "</span></response>";
        }
        
        response.setContentType("text/xml");
        PrintWriter pw = response.getWriter();
        pw.write(xml);
        pw.flush();
        
        if ( pw.checkError() ) {
            LOGGER.log(Level.SEVERE, "PrintWriter Error");
        }
    }
}
