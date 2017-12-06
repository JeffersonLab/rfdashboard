package org.jlab.rfd.model;

import java.util.Date;
import javax.json.Json;
import javax.json.JsonObject;
import org.jlab.rfd.business.util.DateUtil;

/**
 * The class represents an RF Dashboard comment. These comments are identified
 * by a timestamp, topic, username, and comment contents
 *
 * @author adamc
 */
public class Comment implements Comparable<Comment> {

    private final String username;
    private final Date timestamp;
    private final String topic;
    private final String content;
    private final long comment_id;

    public Comment(String username, Date timestamp, String topic, String content, long comment_id) {
        this.username = username;
        this.timestamp = timestamp;
        this.topic = topic;
        this.content = content;
        this.comment_id = comment_id;
    }

    public String getUsername() {
        return username;
    }

    public Date getTimestamp() {
        return timestamp;
    }

    public String getTopic() {
        return topic;
    }

    public String getContent() {
        return content;
    }

    public JsonObject toJson() {
        return Json.createObjectBuilder()
                .add("username", username)
                .add("timestamp", DateUtil.formatDateYMDHMS(timestamp))
                .add("topic", topic)
                .add("content", content)
                .build();
    }

    // We almost always want the most recent comment first, which mean reverse chronological order.
    @Override
    public int compareTo(Comment c) {
        int cmp = 0;
        if (this.timestamp.before(c.getTimestamp())) {
            cmp = 1;
        } else if (this.timestamp.after(c.getTimestamp())) {
            cmp = -1;
        }
        return cmp;
    }
}
