package org.jlab.rfd.presentation.controller;

import java.io.IOException;
import java.net.URLEncoder;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
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
import org.jlab.rfd.model.Comment;

/**
 *
 * @author adamc
 */
@WebServlet(name = "NewComment", urlPatterns = {"/comments/new-comment"})
public class NewComment extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(NewComment.class.getName());

    /**
     * Handles the HTTP <code>GET</code> method. Returns a JSON object with a
     * list of included and excluded user filters for comments queries.
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

        String topic = request.getParameter("topic");
        System.out.println("Topic: " + topic);
        String l = request.getParameter("limit");
        String o = request.getParameter("offset");
        int limit;
        int offset;

        // If no limit is set, then redirect.  However, offset is optional.
        if (l == null || l.isEmpty()) {
            limit = 20;
            redirectNeeded = true;
        } else {
            limit = Integer.parseUnsignedInt(l);
        }
        if (o == null || o.isEmpty()) {
            offset = 0;
            redirectNeeded = true;
        } else {
            offset = Integer.parseUnsignedInt(o);
        }

        request.setAttribute("offset", offset);
        request.setAttribute("limit", limit);
        request.setAttribute("topic", topic);

        if (redirectNeeded) {
            String redirectUrl = request.getContextPath() + "/comments/new-comment";

            redirectUrl += "?limit=" + URLEncoder.encode(String.valueOf(limit), "UTF-8");
            if (topic != null) {
                redirectUrl += "&topic=" + URLEncoder.encode(topic, "UTF-8");
            }
            redirectUrl += "&offset=" + URLEncoder.encode(String.valueOf(offset), "UTF-8");

            System.out.println("Redirecting to " + redirectUrl);
            response.sendRedirect(response.encodeRedirectURL(redirectUrl));
            return;
        }

        CommentService cs = new CommentService();
        try {
            List<String> topics = null;
            if (topic != null) {
                topics = new ArrayList<>();
                topics.add(topic);
            }
            CommentFilter filter = new CommentFilter(null, null, topics, null, null);
            SortedSet<Comment> comments = cs.getComments(filter, limit, offset);
            request.setAttribute("comments", comments);

            SortedSet<String> validTopics = cs.getValidTopics();
            request.setAttribute("validTopics", validTopics);
        } catch (SQLException | ParseException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            LOGGER.log(Level.WARNING, "Error querying comment database\n{0}", ex);
            throw new ServletException("Erorr querying comment database");
        }

        request.getRequestDispatcher("/WEB-INF/views/comments/new-comment.jsp").forward(request, response);
    }
}