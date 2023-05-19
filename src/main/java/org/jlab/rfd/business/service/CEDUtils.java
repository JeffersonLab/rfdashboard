package org.jlab.rfd.business.service;

import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import java.io.IOException;

public class CEDUtils {
    private CEDUtils() {
        //private as this is intended to be a collection of static utility functions.  No objects to be created.
    }
    /**
     * Process the CED server's JSON response to return the element list from the Inventory.
     * @param reader A JsonReader for reading the CED response
     * @return A JsonArray that represents the "elements" array of the inventory.
     * @throws IOException On non-ok response from CED server
     */
    static JsonArray processCEDResponse(JsonReader reader) throws IOException {
        JsonObject json = reader.readObject();
        String status = json.getString("stat");
        if (!"ok".equals(status)) {
            throw new IOException("unable to lookup Cavity Data from CED: response stat: " + status);
        }

        return json.getJsonObject("Inventory").getJsonArray("elements");
    }
}
