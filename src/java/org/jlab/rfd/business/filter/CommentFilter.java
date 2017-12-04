/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.business.filter;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 *
 * @author adamc
 */
public class CommentFilter {

    private static List<String> users;
    private static List<String> excludeUsers;
    private static List<String> topics;
    private static Date start;
    private static Date end;

    public CommentFilter(List<String> users, List<String> excludeUsers, List<String> topics, Date start, Date end) {
        this.users = users;
        this.excludeUsers = excludeUsers;
        this.topics = topics;
        this.start = start;
        this.end = end;
    }

    public String getSqlWhereClause() {
        String filter = "";
        List<String> filters = new ArrayList<>();

        if (end != null) {
            filters.add("comment_time < ? ");
        }

        if (start != null) {
            filters.add("comment_time >= ?");
        }

        if (users != null) {
            String userFilter = "username in (?";
            for (int i = 1; i < users.size(); i++) {
                userFilter += ",?";
            }
            userFilter += ") ";
            filters.add(userFilter);
        }

        if (excludeUsers != null) {
            String excludeUserFilter = "username not in (?";
            for (int i = 1; i < excludeUsers.size(); i++) {
                excludeUserFilter += ",?";
            }
            excludeUserFilter += ") ";
            filters.add(excludeUserFilter);
        }

        if (topics != null) {
            String topicFilter = "topic in (?";
            for (int i = 1; i < topics.size(); i++) {
                topicFilter += ",?";
            }
            topicFilter += ") ";
            filters.add(topicFilter);
        }

        if (!filters.isEmpty()) {
            filter = "where " + filters.get(0);

            if (filters.size() > 1) {
                for (int i = 1; i < filters.size(); i++) {
                    filter = filter + "and " + filters.get(i);
                }
            }
        }

        return filter;
    }

    public void assignParameterValues(PreparedStatement stmt) throws SQLException {
        int i = 1;
        if (end != null) {
            stmt.setDate(i++, new java.sql.Date(end.getTime()));
        }
        if (start != null) {
            stmt.setDate(i++, new java.sql.Date(start.getTime()));
        }

        if (users != null) {
            for (String user : users) {
                stmt.setString(i++, user);
            }
        }

        if (excludeUsers != null) {
            for (String user : users) {
                stmt.setString(i++, user);
            }
        }
        
        if (topics != null) {
            for (String topic : topics) {
                stmt.setString(i++, topic);
            }
        }
    }
}
