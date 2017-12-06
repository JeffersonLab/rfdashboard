/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.presentation.controller;

import java.io.IOException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.jlab.rfd.business.filter.CommentFilter;
import org.jlab.rfd.business.service.CommentService;
import org.jlab.rfd.business.util.DateUtil;
import org.jlab.rfd.model.Comment;
import org.jlab.rfd.presentation.util.DataFormatter;
import org.jlab.rfd.presentation.util.RequestParamUtil;

/**
 *
 * @author adamc
 */
@WebServlet(name = "CommentHistory", urlPatterns = {"/comments/history"})
public class CommentHistory extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(CommentHistory.class.getName());

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

        boolean redirectNeeded = false;

        List<String> includeUsers = RequestParamUtil.processMultiValuedParameter(request, "includeUser");
        List<String> excludeUsers = RequestParamUtil.processMultiValuedParameter(request, "excludeUser");
        List<String> topics = RequestParamUtil.processMultiValuedParameter(request, "topic");
        String s = request.getParameter("start");
        String e = request.getParameter("end");
        String l = request.getParameter("limit");
        String o = request.getParameter("offset");

        int limit;
        int offset;

        try {
            if (l == null || l.isEmpty()) {
                redirectNeeded = true;
                limit = 20;
            } else {
                limit = Integer.parseInt(l);
            }
            if (o == null || o.isEmpty()) {
                redirectNeeded = true;
                offset = 0;
            } else {
                offset = Integer.parseInt(o);
            }
        } catch (NumberFormatException ex) {
            LOGGER.log(Level.WARNING, "Error parsing limit or offset parameters - {0}", ex);
            throw new ServletException("Error parsing limit or offset parameters");
        }

        Date start = null;
        Date end = null;
        try {
            start = (s == null || s.isEmpty()) ? null : DateUtil.parseDateString(s);
            end = (e == null || e.isEmpty()) ? null : DateUtil.parseDateString(e);
        } catch (ParseException ex) {
            LOGGER.log(Level.WARNING, "Error parsing start or end paramter", ex);
            throw new ServletException("Error parsing start or end parameter");
        }

        request.setAttribute("topics", DataFormatter.listToMap(topics));
        request.setAttribute("includeUsers", DataFormatter.listToMap(includeUsers));
        request.setAttribute("excludeUsers", DataFormatter.listToMap(excludeUsers));
        request.setAttribute("start", (start == null) ? null : DateUtil.formatDateYMDHMS(start));
        request.setAttribute("end", (end == null) ? null : DateUtil.formatDateYMDHMS(end));
        request.setAttribute("limit", limit);
        request.setAttribute("offset", offset);

        if (redirectNeeded) {
            String redirectUrl = request.getContextPath() + "/comments/history";

            redirectUrl += "?limit=" + URLEncoder.encode(String.valueOf(limit), "UTF-8");
            redirectUrl += "&offset=" + URLEncoder.encode(String.valueOf(offset), "UTF-8");
            if (start != null) {
                redirectUrl += "&start=" + URLEncoder.encode(DateUtil.formatDateYMDHMS(start), "UTF-8");
            }
            if (end != null) {
                redirectUrl += "&end=" + URLEncoder.encode(DateUtil.formatDateYMDHMS(end), "UTF-8");
            }
            if (includeUsers != null) {
                for (String user : includeUsers) {
                    redirectUrl += "&includeUser=" + URLEncoder.encode(user, "UTF-8");
                }
            }
            if (excludeUsers != null) {
                for (String user : excludeUsers) {
                    redirectUrl += "&excludeUser=" + URLEncoder.encode(user, "UTF-8");
                }
            }
            if (topics != null) {
                for (String topic : topics) {
                    redirectUrl += "&topic=" + URLEncoder.encode(topic, "UTF-8");
                }
            }
            System.out.println("Redirecting to " + redirectUrl);
            response.sendRedirect(response.encodeRedirectURL(redirectUrl));
            return;
        }

        CommentService cs = new CommentService();
        CommentFilter filter = new CommentFilter(includeUsers, excludeUsers, topics, start, end);

        try {
            SortedSet<Comment> comments = cs.getComments(filter, limit, offset);
            SortedSet<String> validTopics = cs.getValidTopics();
            SortedSet<String> authors = cs.getCurrnetAuthors();
            
            request.setAttribute("comments", comments);
            request.setAttribute("validTopics", validTopics);
            request.setAttribute("authors", authors);
            
            request.getRequestDispatcher("/WEB-INF/views/comments/history.jsp").forward(request, response);
        } catch (SQLException | ParseException ex) {
            LOGGER.log(Level.WARNING, "Error querying comments database - {0}", ex);
            throw new ServletException("Error querying comments database");
        }
    }

}
