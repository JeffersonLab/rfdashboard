package org.jlab.rfd.business.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.rfd.business.util.SqlUtil;
import org.jlab.rfd.model.Comment;

/**
 * This class manages selecting and inserting comments into the rfd_comments
 * database table
 *
 * @author adamc
 */
public class CommentService {

    private static final Logger LOGGER = Logger.getLogger(CommentService.class.getName());

    public SortedSet<Comment> getComments(List<String> users, List<String> topics, Date start, Date end, Integer limit) throws SQLException, ParseException {

        SortedSet<Comment> comments = new TreeSet<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = SqlUtil.getConnection();

            String sql = "SELECT COMMENT_TIME, USERNAME, TOPIC, COMMENT_STRING FROM RFD_COMMENTS";
            if (start != null) {
                sql += " WHERE COMMENT_TIME >= ?";
            }
            if (end != null) {
                if (start == null) {
                    sql += " WHERE";
                } else {
                    sql += " AND";
                }
                sql += " COMMENT_TIME <= ?";
            }
            if (limit != null) {
                sql = "SELECT * FROM (" + sql + " ORDER BY COMMENT_TIME DESC) WHERE ROWNUM <= ?";
            }

            LOGGER.log(Level.FINEST, "SQL query used: {0}", sql);

            int index = 1;
            pstmt = conn.prepareStatement(sql);
            if (start != null) {
                pstmt.setDate(index++, new java.sql.Date(start.getTime()));
            }
            if (end != null) {
                pstmt.setDate(index++, new java.sql.Date(end.getTime()));
            }
            if (limit != null) {
                pstmt.setInt(index++, limit);
            }

            rs = pstmt.executeQuery();
            while (rs.next()) {
                String username = rs.getString(2);
                String topic = rs.getString(3);

                if ((users == null || users.contains(username)) && (topics == null || topics.contains(topic))) {
                    Calendar cal = Calendar.getInstance();
                    cal.setTimeInMillis(rs.getTimestamp(1).getTime());
                    Date time = cal.getTime();

                    String content = rs.getString(4);

                    comments.add(new Comment(username, time, topic, content));
                }
            }
        } finally {
            SqlUtil.close(conn, pstmt, rs);
        }
        return comments;
    }

    public void makeComment(String username, String topic, Date timestamp, String content) throws SQLException {
        Connection conn = null;
        PreparedStatement pstmt = null;

        String sql = "INSERT INTO rfd_comments "
                + " (COMMENT_ID, USERNAME, TOPIC, COMMENT_TIME, COMMENT_STRING)"
                + " VALUES (rfd_comments_seq.nextVal, ?, ?, ?, ?)";
        java.sql.Date ts = new java.sql.Date(timestamp.getTime());
        try {
            conn = SqlUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, username);
            pstmt.setString(2, topic);
            pstmt.setDate(3, ts);
            pstmt.setString(4, content);
            int n = pstmt.executeUpdate();

            if (n != 1) {
                LOGGER.log(Level.WARNING, "Error inserting comment. User: {0} - Comment: {1}", new Object[]{username, content});
                throw new RuntimeException("Error inserting comment.  " + n + " rows affected.");
            }
        } finally {
            SqlUtil.close(conn, pstmt);
        }

        LOGGER.log(Level.INFO, "Created comment for user {0} - {1}", new Object[]{username, content});
    }

    public Map<String, SortedSet<Comment>> getCommentsByTopic(List<String> users, List<String> topics, Date start, Date end, Integer limit) throws SQLException, ParseException {
        Map<String, SortedSet<Comment>> sorted = new HashMap<>();
        SortedSet<Comment> comments = this.getComments(users, topics, start, end, limit);
        for (Comment c : comments) {
            if (!sorted.containsKey(c.getTopic())) {
                sorted.put(c.getTopic(), new TreeSet<>());
            }
            sorted.get(c.getTopic()).add(c);
        }
        return sorted;
    }

    // This is a simplified wrapper for getting all comments by topic
    public Map<String, SortedSet<Comment>> getCommentsByTopic() throws SQLException, ParseException {
        return this.getCommentsByTopic(null, null, null, null, null);
    }
}
