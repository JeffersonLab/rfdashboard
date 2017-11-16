
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
public class Comment {
    private final String username;
    private final Date timestamp;
    private final String topic;
    private final String content;
    
    public Comment(String username, Date timestamp, String topic, String content) {
        this.username = username;
        this.timestamp = timestamp;
        this.topic = topic;
        this.content = content;
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
    
    public JsonObject toJsonObject() {
        return Json.createObjectBuilder()
                .add("username", username)
                .add("timestamp", DateUtil.formatDateYMDHMS(timestamp))
                .add("topic", topic)
                .add("content", content)
                .build();
    }
}
