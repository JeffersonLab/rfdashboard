package org.jlab.rfd.presentation.controller.ajax;

import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.jlab.rfd.business.service.CommentService;
import org.jlab.rfd.business.util.DateUtil;
import org.jlab.rfd.business.util.SessionUtil;
import org.jlab.rfd.model.Comment;

/**
 * Controller for inserting and querying comments from the rfd_comments database
 * table
 *
 * @author adamc
 */
@WebServlet(name = "CommentsAjax", urlPatterns = {"/ajax/comments"})
public class CommentsAjax extends HttpServlet {

    private static final Logger LOGGER = Logger.getLogger(CommentsAjax.class.getName());

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

        response.setContentType("application/json");

        // The SessionUtil method throws an exception if not authenticated
        String username = SessionUtil.checkAuthenticated(request);
        String topic = request.getParameter("topic");
        String comment = request.getParameter("comment");
        Date now = Calendar.getInstance().getTime();
        PrintWriter pw = response.getWriter();

        if (topic == null || comment == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            String error = "{error: \"Required parameters are topic and comment\"}";
            pw = response.getWriter();
            pw.write(error);
            pw.flush();
            return;
        }

        CommentService cs = new CommentService();
        try {
            cs.makeComment(username, topic, now, comment);
        } catch (SQLException ex) {
            LOGGER.log(Level.SEVERE, null, ex);
            String error = "{error: \"Error submitting comment to database\"}";
            pw.write(error);
            pw.flush();
            return;
        }

        pw.write("{\"data\": \"success\"}");
        pw.flush();
    }

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

        // Only one content type support here
        response.setContentType("application/json");

        // First check if we are reading comments or making a new one
        String error = null;

        // If reading, then we can get everything or filter by username, topic, and timestamps (start/end).  Check for these
        // options and pass them on to the proper service.  Another layer of filter is handled by sessions.  If users are requested 
        // using the user parameter, then use that list and ignore the session filters.  Otherwise, use the session filters.
        List<String> users = null;
        String userParam = request.getParameter("user");
        if (userParam != null) {
            users = Arrays.asList(userParam.split(","));
        } else {
            HttpSession session = request.getSession();
            users = (List<String>) session.getAttribute("CommentIncludeFilter");
        }
        List<String> topics = null;
        String topic = request.getParameter("topic");
        if (topic != null) {
            topics = Arrays.asList(topic.split(","));
        }

        String s = request.getParameter("start");
        String e = request.getParameter("end");
        String l = request.getParameter("limit");
        String b = request.getParameter("by");
        Date start = null;
        Date end = null;
        Integer limit = null;
        String by = null;
        if (l != null) {
            try {
                limit = Integer.parseInt(l);
            } catch (NumberFormatException ex) {
                LOGGER.log(Level.INFO, "Unable to process limit parameter");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                error = "{\"error\": \"unable to parse 'limit'\"}";
            }
        }

        if (null == b) {
            by = "timestamp";
        } else {
            switch (b) {
                case "timestamp":
                    by = "timestamp";
                    break;
                case "topic":
                    by = "topic";
                    break;
                default:
                    LOGGER.log(Level.INFO, "Unsupported 'by' parameter value");
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    error = "{\"error\": \"Unsupported 'by' parameter value\"}";
                    break;
            }
        }

        try {
            if (s != null) {
                start = DateUtil.parseDateStringYMDHMS(s);

            }
            if (e != null) {
                end = DateUtil.parseDateStringYMDHMS(e);
            }
        } catch (ParseException ex) {
            LOGGER.log(Level.WARNING, "Unable to process start or end parameters - {0}", ex);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            error = "{\"error\": \"unable to process start or end parameters\"}";
        }

        // The strategy here is to create a JSON object that will be written out to the PrintWriter or to create an error string that
        // should be written out instead.
        JsonObject out = null;
        // Check that we haven't hit an error before we submit the request
        if (error == null) {
            try {
                CommentService cs = new CommentService();
                JsonObjectBuilder top = Json.createObjectBuilder();

                // Use the session exclude filter if users were not specifically requested
                List<String> excludeUsers = null;
                if (userParam == null) {
                    excludeUsers = (List<String>) request.getSession().getAttribute("CommentExcludeFilter");
                }

                // by shouldn't be null since it gets assigned based on 'b' being null or not
                switch (by) {
                    case "timestamp":

                        SortedSet<Comment> commentSet = null;  // null value used in a check below
                        commentSet = cs.getComments(users, excludeUsers, topics, start, end, limit);

                        top = Json.createObjectBuilder();
                        JsonArrayBuilder jab = Json.createArrayBuilder();
                        if (commentSet != null) {
                            for (Comment com : commentSet) {
                                jab.add(com.toJson());
                            }
                        }
                        out = top.add("data", jab.build()).build();
                        break;

                    case "topic":
                        Map<String, SortedSet<Comment>> commentMap = null;
                        commentMap = cs.getCommentsByTopic(users, excludeUsers, topics, start, end, limit);
                        for (String topicKey : commentMap.keySet()) {
                            JsonArrayBuilder topicComments = Json.createArrayBuilder();
                            for (Comment com : commentMap.get(topicKey)) {
                                topicComments.add(com.toJson());
                            }
                            top.add(topicKey, topicComments.build());
                        }
                        out = top.build();
                        break;
                    default:
                        LOGGER.log(Level.SEVERE, "Error querying database for comments");
                        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                        error = "{\"error\": \"Unsupported 'by' parameter\"}";
                        break;
                }
            } catch (SQLException | ParseException ex) {
                LOGGER.log(Level.SEVERE, "Error querying database for comments");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                error = "{\"error\": 'Error querying database for comments'}";
            }
        }

        // See if we have an error. If not, respond with the real output
        PrintWriter pw = response.getWriter();
        if (error != null) {
            pw.print(error);
            pw.flush();
        } else {
            if (out != null) {
                JsonWriter jw = Json.createWriter(pw);
                jw.writeObject(out);
            } else {
                LOGGER.log(Level.SEVERE, "Unexpected error querying database for comments");
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                pw.print("{\"error\": \"Unexpected error querying database for comments\"}");
                pw.flush();
            }
        }
    }
}
