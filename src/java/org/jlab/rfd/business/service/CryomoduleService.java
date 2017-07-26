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
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.jlab.rfd.model.CryomoduleType;

/**
 * Service used to query the CED about Cryomodule information.
 * @author adamc
 */
public class CryomoduleService {

    private static final Logger LOGGER = Logger.getLogger(CryomoduleService.class.getName());
    private static final String CED_INVENTORY_URL = "http://ced.acc.jlab.org/inventory";

    /**
     * This queries the history CED for a listing of ModuleType for all
     * CryoModules in the CED. Then, using string manipulations based on our
     * standard naming conventions (e.g., cavity 1L22-1 is in module 1L22),
     * creates a Map of cavity CED name to CryoModule Module Type (e.g., C25 or
     * C100). All queries are limited to date precision (no hours, minutes,
     * etc.). Returns null if timestamp is for future date.
     *
     * @param timestamp The date for which we query the CED history workspace
     * @return A map of Cavity EPICS name to it parent CryoModule ModuleType on the requested date
     * @throws java.text.ParseException
     * @throws java.io.IOException
     */
    public HashMap<String, CryomoduleType> getCryoModuleTypes(Date timestamp) throws ParseException, IOException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if ( timestamp.after(new Date()) ) {
            return null;
        }

        String wrkspc = sdf.format(timestamp);
        String cmQuery = "?t=Cryomodule&p=ModuleType&out=json&ced=history&wrkspc=" + wrkspc;

        //LOGGER.log(Level.FINEST, "CED Query: {0}", CED_INVENTORY_URL + cmQuery);
        URL url = new URL(CED_INVENTORY_URL + cmQuery);
        InputStream in = url.openStream();

        HashMap<String, CryomoduleType> cmTypes = new HashMap<>();
        try (JsonReader reader = Json.createReader(in)) {
            JsonObject json = reader.readObject();
            String status = json.getString("stat");
            if (!"ok".equals(status)) {
                throw new IOException("unable to lookup Cavity Data from CED: response stat: " + status);
            }
            JsonObject inventory = json.getJsonObject("Inventory");
            JsonArray elements = inventory.getJsonArray("elements");

            for (JsonObject element : elements.getValuesAs(JsonObject.class)) {
                JsonObject properties = element.getJsonObject("properties");

                cmTypes.put(element.getString("name"), CryomoduleType.valueOf(properties.getString("ModuleType")));
            }
        }
        return cmTypes;
    }
}