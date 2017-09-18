/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.model;

import java.math.BigDecimal;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 *
 * @author adamc
 */
public class CEDElementUpdate {

    private String dateString;
    private String value;
    private String username;
    private String comment;
    private int index;
    private int index1;
    private int index2;

    public CEDElementUpdate(String dateString, String value, String username, String comment, int index, int index1, int index2) {
        this.dateString = dateString;
        this.value = value;
        this.username = username;
        this.comment = comment;
        this.index = index;
        this.index1 = index1;
        this.index2 = index2;
    }

    public String getDateString() {
        return dateString;
    }

    public String getValue() {
        return value;
    }

    public String getUsername() {
        return username;
    }

    public String getComment() {
        return comment;
    }

    public int getIndex() {
        return index;
    }

    public int getIndex1() {
        return index1;
    }

    public int getIndex2() {
        return index2;
    }

    public JsonObject toJson(boolean showDate) {
        JsonObjectBuilder job = Json.createObjectBuilder();
        if (showDate) {
            job.add("date", dateString);
        }
        job.add("value", value)
                .add("username", username)
                .add("comment", comment);
        return job.build();

    }
}
