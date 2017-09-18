/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.business.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.jlab.rfd.business.util.DateUtil;
import org.jlab.rfd.model.CEDElementUpdate;
import org.jlab.rfd.model.CEDElementUpdateHistory;

/**
 *
 * @author adamc
 */
public class CEDUpdateHistoryService {
    private static final Logger LOGGER = Logger.getLogger(CEDUpdateHistoryService.class.getName());
    private static final String CED_UPDATE_HISTORY_URL = "https://ced.acc.jlab.org/ajax/history/";

    public CEDElementUpdateHistory getElementUpdateHistory(String elem, List<String> props) throws IOException, ParseException {
        CEDElementUpdateHistory updateHistory = new CEDElementUpdateHistory(elem);
        for(String prop : props) {
            String query = CED_UPDATE_HISTORY_URL + elem + "/" + prop + "?output=json";
            URL url = new URL(query);
            InputStream in = url.openStream();
            try(JsonReader reader = Json.createReader(in)) {
                JsonObject json = reader.readObject();
                String status = json.getString("stat");
                if ( ! "ok".equals(status) ) {
                    throw new IOException("unable to update history from CED.  stat: " + status);
                }
                JsonArray history = json.getJsonObject("response").getJsonArray("history");
                for ( int i = 0; i < history.size(); i++) {
                    JsonObject update = history.getJsonObject(i);
                    
                    String dateString = update.getString("date");
                    Date date = DateUtil.parseDateStringYMDHMS(dateString);
                    String value = update.getString("value");
                    String username = update.getString("username");
                    String comment = update.getString("comment");
                    int index = update.getInt("index");
                    int index1 = update.getInt("index1");
                    int index2 = update.getInt("index2");
                     updateHistory.addUpdate(new CEDElementUpdate(dateString, value, username, comment, index, index1, index2),
                             date, prop);
                }
            }
        }
        
        return updateHistory;
    }
}
