/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.model;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import org.jlab.rfd.business.util.DateUtil;

/**
 * This object contains the update history of a CED element.
 *
 * @author adamc
 */
public class CEDElementUpdateHistory {

    private final String elementName;
    private final Map<String, Map<Date, CEDElementUpdate>> byProp;
    private final Map<Date, Map<String, CEDElementUpdate>> byDate;

    public CEDElementUpdateHistory(String elementName) {
        this.elementName = elementName;
        byProp = new HashMap<>();
        byDate = new TreeMap<>();
    }

    public CEDElementUpdate addUpdate(CEDElementUpdate ceu, Date date, String property) {
        CEDElementUpdate oldValue = null;

        // Add it to the byProp structure
        if (!byProp.containsKey(property)) {
            byProp.put(property, new HashMap<Date, CEDElementUpdate>());
            byProp.get(property).put(date, ceu);
        } else {
            oldValue = byProp.get(property).put(date, ceu);
        }

        // Add it to the byDate structure
        if (!byDate.containsKey(date)) {
            byDate.put(date, new HashMap<String, CEDElementUpdate>());
            byDate.get(date).put(property, ceu);
        } else {
            oldValue = byDate.get(date).put(property, ceu);
        }

        return oldValue;
    }

    // Return all of the updates associated with a single property
    public Map<Date, CEDElementUpdate> getUpdateHistory(String property) {
        return byProp.get(property);
    }

    // Return the element name assoicated with this update history
    public String getElementName() {
        return elementName;
    }

    public JsonObject toJsonByDate(List<String> props) {
        JsonObjectBuilder job = Json.createObjectBuilder();
        JsonArrayBuilder dates = Json.createArrayBuilder();
        for (Date date : byDate.keySet()) {
            JsonObjectBuilder dateOB = null;
            JsonObjectBuilder propsOB = null;
            Map<String, CEDElementUpdate> updates = byDate.get(date);
            for (String prop : updates.keySet()) {
                if (props.contains(prop)) {
                    if (dateOB == null) {
                        dateOB = Json.createObjectBuilder();
                        dateOB.add("date", DateUtil.formatDateYMDHMS(date));
                    }
                    if (propsOB == null) {
                        propsOB = Json.createObjectBuilder();
                    }
                    propsOB.add(prop, updates.get(prop).toJson(false));
                }
            }
            if (dateOB != null && propsOB != null) {
                dateOB.add("properties", propsOB.build());
                dates.add(dateOB.build());
            }
        }
        return job.add("updates", dates.build()).build();
    }
}
