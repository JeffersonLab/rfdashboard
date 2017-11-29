/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.presentation.controller.ajax;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.List;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.jlab.rfd.presentation.util.RequestParamUtil;

/**
 *
 * @author adamc
 */
@WebServlet(name = "CommentFilter", urlPatterns = {"/ajax/comment-filter"})
public class CommentFilter extends HttpServlet {

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP <code>GET</code> method.  Returns a JSON object with a list of included and excluded user filters
     * for comments queries.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();
        List<String> includes = (List<String>) session.getAttribute("CommentIncludeFilter");
        List<String> excludes = (List<String>) session.getAttribute("CommentExcludeFilter");

        JsonObjectBuilder job = Json.createObjectBuilder();
        JsonArrayBuilder jin = Json.createArrayBuilder();
        JsonArrayBuilder jex = Json.createArrayBuilder();
        if (includes != null) {
            for (String inc : includes) {
                jin.add(inc);
            }
        }
        if (excludes != null) {
            for (String ex : excludes) {
                jex.add(ex);
            }
        }
        job.add("include", jin.build());
        job.add("exclude", jex.build());
        JsonObject out = job.build();

        response.setContentType("application/json");
        PrintWriter pw = response.getWriter();
        pw.write(out.toString());
    }

    /**
     * Handles the HTTP <code>POST</code> method.  Accepts two arguments
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        
        List<String> include = RequestParamUtil.processMultiValuedParameter(request, "include");
        List<String> exclude = RequestParamUtil.processMultiValuedParameter(request, "exclude");
        
        HttpSession session = request.getSession();
        session.setAttribute("CommentIncludeFilter", include);
        session.setAttribute("CommentExcludeFilter", exclude);
        
        this.doGet(request, response);
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
