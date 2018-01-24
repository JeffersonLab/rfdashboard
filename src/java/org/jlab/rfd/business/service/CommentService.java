package org.jlab.rfd.business.service;

import java.io.IOException;
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
import org.jlab.rfd.business.filter.CommentFilter;

/**
 * This class manages selecting and inserting comments into the rfd_comments
 * database table
 *
 * @author adamc
 */
public class CommentService {

    private static final Logger LOGGER = Logger.getLogger(CommentService.class.getName());

    public SortedSet<String> getCurrnetAuthors() throws SQLException {
        SortedSet<String> users = new TreeSet<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = SqlUtil.getConnection();
            String sql = "Select DISTINCT USERNAME FROM RFD_COMMENTS ORDER BY USERNAME";
            pstmt = conn.prepareStatement(sql);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                users.add(rs.getString(1));
            }

        } finally {
            SqlUtil.close(conn, pstmt, rs);
        }
        return users;
    }

    /**
     * Returns a set of suggested valid topics that users can comment upon.
     *
     * @return Returns the set of current Cryocavity names in the CED
     * @throws IOException
     */
    public SortedSet<String> getValidTopics() throws IOException {
        CavityService cs = new CavityService();
        SortedSet<String> cavs = cs.getCavityNames();
        SortedSet<String> topics = new TreeSet<>();
        // We also want cryomodule/zones as topics.  Just strip off the '-#' and add
        for(String cav : cavs) {
            topics.add(cav);
            topics.add(cav.substring(0, 4));
        }
        return topics;
    }

    /**
     *
     * @param filter
     * @return
     * @throws java.sql.SQLException
     */
    public int countList(CommentFilter filter) throws SQLException {
        int count = 0;
        String sql = "select count(comment_id) from rfd_comments " + filter.getSqlWhereClause();

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = SqlUtil.getConnection();
            pstmt = conn.prepareStatement(sql);
            filter.assignParameterValues(pstmt);
            rs = pstmt.executeQuery();
            if (rs.next()) {
                count = rs.getInt(1);
            }
        } finally {
            SqlUtil.close(rs, pstmt, conn);
        }
        return count;
    }

    /**
     * A wrapper on getComments that provides all results all at once.
     *
     * @param filter
     * @return
     * @throws java.sql.SQLException
     * @throws java.text.ParseException
     */
    public SortedSet<Comment> getComments(CommentFilter filter) throws SQLException, ParseException {
        return getComments(filter, -1, -1);
    }

    /**
     * Retrieves user comments from the RFD Database.The query allows pagination
     * using limit and offset parameters
     *
     * @param filter Contains any filtering parameters to be used
     * @param limit Return at most this many responses. Less than one implies no limit
     * @param offset Start the result set after the "offset" most recent.  Less than one implies no offset 
     * comments.
     * @return Collection of user comments
     * @throws SQLException Database query error
     * @throws ParseException String to int parse on return from database
     */
    public SortedSet<Comment> getComments(CommentFilter filter, int limit, int offset) throws SQLException, ParseException {

        SortedSet<Comment> comments = new TreeSet<>();
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = SqlUtil.getConnection();

            String sql = "SELECT COMMENT_ID, COMMENT_TIME, USERNAME, TOPIC, COMMENT_STRING "
                    + "FROM RFD_COMMENTS " + filter.getSqlWhereClause();

            // order pagination
            sql = sql + "order by comment_time desc";

            if (limit > 0 && offset > 0) {
                // limit number of count (pagination) if offset and limit are valid
                sql = "select * from (select z.*, ROWNUM rnum from ("
                        + sql + ") z where ROWNUM <= " + (offset + limit) + ") where rnum > " + offset;
            } else if (limit > 0) {
                sql = "select *  from (" + sql + ") where ROWNUM <= " + limit;
            } else if (offset > 0 ) {                
                sql = "select * from (select z.*, ROWNUM rnum from (" + sql + ") z where rnum > " + offset;
            }
            LOGGER.log(Level.FINEST, "SQL query used: {0}", sql);

            pstmt = conn.prepareStatement(sql);
            filter.assignParameterValues(pstmt);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                long comment_id = rs.getLong(1);
                String username = rs.getString(3);
                String topic = rs.getString(4);

                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(rs.getTimestamp(2).getTime());
                Date time = cal.getTime();

                String content = rs.getString(5);
                comments.add(new Comment(username, time, topic, content, comment_id));
            }
        } finally {
            SqlUtil.close(pstmt, rs, conn);
        }
        
        return comments;
    }

    public void makeComment(String username, String topic, Date timestamp, String content) throws SQLException, IOException {
        
        SortedSet<String> topics = getValidTopics();
        if (!topics.contains(topic)) {
            throw new RuntimeException("Invalid topic " + topic);
        }
        
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
            SqlUtil.close(pstmt, conn);
        }

        LOGGER.log(Level.INFO, "Created comment for user {0} - {1}", new Object[]{username, content});
    }

    public Map<String, SortedSet<Comment>> getCommentsByTopic(CommentFilter filter) throws SQLException, ParseException {
        return getCommentsByTopic(filter, -1, -1);
    }

    /**
     * Return a map keyed on topics with values being a sortedset (chonological order) of comments for that topic.  Limit and 
     * offset apply on a per topic basis.
     * @param filter CommentFilter object
     * @param limit  Max number of comments per topic.  Less than one implies no limit.
     * @param offset  How many of the initial comments to skip.  Less than one implies no offset.
     * @return
     * @throws SQLException
     * @throws ParseException 
     */
    public Map<String, SortedSet<Comment>> getCommentsByTopic(CommentFilter filter, int limit, int offset) throws SQLException, ParseException {
        
        int l = limit < 1 ? Integer.MAX_VALUE : limit;
        int o = offset < 1 ? 0 : offset;
        Map<String, SortedSet<Comment>> sorted = new HashMap<>();
        SortedSet<Comment> comments = this.getComments(filter, -1, -1);
        Map<String, Integer> indexes = new HashMap<>();
        for (Comment c : comments) {
            if (!sorted.containsKey(c.getTopic())) {
                sorted.put(c.getTopic(), new TreeSet<>());
                indexes.put(c.getTopic(), 1);
            }
                       
            // Check against limit and offset
            int i = indexes.get(c.getTopic());            
            if (i > o && i <= (o + l)) {
                sorted.get(c.getTopic()).add(c);
            }
            indexes.put(c.getTopic(), i + 1);
        }
        return sorted;
    }

    // This is a simplified wrapper for getting all comments by topic
    public Map<String, SortedSet<Comment>> getCommentsByTopic() throws SQLException, ParseException {
        CommentFilter filter = new CommentFilter(null, null, null, null, null);
        return this.getCommentsByTopic(filter, -1, -1);
    }
}
