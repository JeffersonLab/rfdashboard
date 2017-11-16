package org.jlab.rfd.business.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.jlab.rfd.business.util.DateUtil;
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
    private static final SimpleDateFormat ORA_FORMAT = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");

    // TODO: add String topic parameter to allow filtering on topics
    public List<Comment> getComments(List<String> users, List<String> topics, Date start, Date end) throws SQLException, ParseException {
        List<Comment> comments = new ArrayList<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = SqlUtil.getConnection();

            String sql = "SELECT COMMENT_TIME, USERNAME, TOPIC, COMMENT_STRING FROM RFD_COMMENTS";
            if (start != null && end != null) {
                sql += " WHERE COMMENT_TIME BETWEEN TO_DATE(?, 'yyyy/MM/dd HH24:mm:ss')"
                        + " AND TO_DATE(? ,'yyyy/MM/dd HH24:mm:ss')";
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, ORA_FORMAT.format(start));
                pstmt.setString(2, ORA_FORMAT.format(end));
            } else if (start != null) {
                sql += " WHERE COMMENT_TIME >= ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setTimestamp(1, new java.sql.Timestamp(start.getTime()));
            } else if (end != null) {
                sql += " WHERE COMMENT_TIME <= ?";
                pstmt = conn.prepareStatement(sql);
                pstmt.setTimestamp(1, new java.sql.Timestamp(end.getTime()));
            } else {
                pstmt = conn.prepareStatement(sql);
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
}
