/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.business.service;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.jlab.rfd.business.util.CebafNames;
import org.jlab.rfd.business.util.DateUtil;
import org.jlab.rfd.config.AppConfig;
import org.jlab.rfd.model.CavityResponse;
import org.jlab.rfd.model.CryomoduleDataPoint;
import org.jlab.rfd.model.CryomoduleType;
import org.jlab.rfd.model.LinacName;

/**
 * Service used to query the CED about Cryomodule information.
 *
 * @author adamc
 */
public class CryomoduleService {

    private static final Logger LOGGER = Logger.getLogger(CryomoduleService.class.getName());
    private static final String LEM_PV_HEAT_SUFFIX = "XHTPLEM";
    private static final String CED_INVENTORY_URL = AppConfig.getAppConfig().getCEDUrl() + "/inventory";

    /**
     * This queries the history CED for a listing of ModuleType for all
     * CryoModules in the CED. Then, using string manipulations based on our
     * standard naming conventions (e.g., cavity 1L22-1 is in module 1L22),
     * creates a Map of cavity CED name to CryoModule Module Type (e.g., C25 or
     * C100). All queries are limited to date precision (no hours, minutes,
     * etc.). Returns null if timestamp is for future date.
     *
     * @param timestamp The date for which we query the CED history workspace
     * @return A map of Cavity EPICS name to it parent CryoModule ModuleType on
     * the requested date
     * @throws java.text.ParseException  On error parsing response
     * @throws java.io.IOException On non-ok response querying CED service
     */
    public HashMap<String, CryomoduleType> getCryoModuleTypes(Date timestamp) throws ParseException, IOException {
        URL url = getCryomoduleTypeURL(timestamp);
        if (url == null) {
            return null;
        }
        InputStream in = url.openStream();

        HashMap<String, CryomoduleType> cmTypes = new HashMap<>();
        try (JsonReader reader = Json.createReader(in)) {
            JsonArray elements = CEDUtils.processCEDResponse(reader);

            for (JsonObject element : elements.getValuesAs(JsonObject.class)) {
                JsonObject properties = element.getJsonObject("properties");

                cmTypes.put(element.getString("name"), CryomoduleType.valueOf(properties.getString("ModuleType")));
            }
        }
        return cmTypes;
    }

    /**
     * Generate a map of cryomodule types to the zones that had that type on the
     * date in question. Can limit responses to a list of specified zones (CED
     * names)
     *
     * @param timestamp Used as the CED history workspace
     * @param zones A list of
     * @return A map of CryomoduleType to the list of zones with that type.
     * @throws IOException On non-ok response from CED server
     */
    public SortedMap<CryomoduleType, List<String>> getZonesByCryomoduleType(Date timestamp, List<String> zones) throws IOException {
        if (timestamp.after(new Date())) {
            return null;
        }

        String wrkspc = DateUtil.formatDateYMDHMS(timestamp).replace(" ", "+");
        String cmQuery = "?t=Cryomodule&p=ModuleType&out=json&ced=history&wrkspc=" + wrkspc;

        SortedMap<CryomoduleType, List<String>> cmTypes = new TreeMap<>();
        for (CryomoduleType type : CryomoduleType.values()) {
            cmTypes.put(type, new ArrayList<>());
        }

        URL url = new URL(CED_INVENTORY_URL + cmQuery);
        InputStream in = url.openStream();

        try (JsonReader reader = Json.createReader(in)) {
            JsonArray elements = CEDUtils.processCEDResponse(reader);

            for (JsonObject element : elements.getValuesAs(JsonObject.class)) {
                JsonObject properties = element.getJsonObject("properties");
                String zoneName = element.getString("name");
                if (zones == null || zones.isEmpty() || zones.contains(zoneName)) {
                    cmTypes.get(CryomoduleType.valueOf(properties.getString("ModuleType"))).add(zoneName);
                }
            }
        }
        return cmTypes;
    }

    /**
     * This queries the history CED for a listing of ModuleType for all
     * Cryomodules in the CED. It then creates a Map of Cryomodule CED name to
     * ModuleType (e.g., C25 or C100). All queries are limited to date precision
     * (no hours, minutes, etc.). Returns null if timestamp is for future date.
     *
     * @param timestamp The date for which we query the CED history workspace
     * @param zones Limit the response to the specified set of zones. Return all
     * zones if null.
     * @return A map of CED Cryomodule names to Cryomodule ModuleType on the
     * requested date
     * @throws java.text.ParseException On error parsing JSON response
     * @throws java.io.IOException On non-ok response from CED server
     */
    public SortedMap<String, CryomoduleType> getCryoModuleTypesByZone(Date timestamp, List<String> zones) throws ParseException, IOException {
        URL url = getCryomoduleTypeURL(timestamp);
        if (url == null) {
            return null;
        }
        InputStream in = url.openStream();

        SortedMap<String, CryomoduleType> cmTypes = new TreeMap<>();
        try (JsonReader reader = Json.createReader(in)) {
            JsonArray elements = CEDUtils.processCEDResponse(reader);

            for (JsonObject element : elements.getValuesAs(JsonObject.class)) {
                if (zones == null || zones.isEmpty() || zones.contains(element.getString("name"))) {
                    JsonObject properties = element.getJsonObject("properties");
                    cmTypes.put(element.getString("name"), CryomoduleType.valueOf(properties.getString("ModuleType")));
                }
            }
        }
        return cmTypes;
    }

    private URL getCryomoduleTypeURL(Date timestamp) throws MalformedURLException {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        if (timestamp.after(new Date())) {
            return null;
        }

        String wrkspc = sdf.format(timestamp);
        String cmQuery = "?t=Cryomodule&p=ModuleType&out=json&ced=history&wrkspc=" + wrkspc;
        return new URL(CED_INVENTORY_URL + cmQuery);
    }

    /**
     * This builds a List of CryomoduleDataPoints describing CEBAF at a point in
     * time.
     *
     * @param date The time of interest. Note: May be truncated to day precision
     * for caching purposes
     * @return A List of CryomoduleDataPoints describing CEBAF cryomodules on
     * date.
     */
    public List<CryomoduleDataPoint> getCryomoduleDataPoints(Date date) throws IOException, ParseException, SQLException {
        List<CryomoduleDataPoint> cmList = new ArrayList<>();

        CavityService cs = new CavityService();
        Set<CavityResponse> cavList = cs.getCavityData(date);

        // Mapped on EPICSName
        Map<String, Double[]> gsets = new HashMap<>();
        Map<String, Double[]> lengths = new HashMap<>();
        Map<String, LinacName> linacs = new HashMap<>();
        Map<String, CryomoduleType> cmTypes = new HashMap<>();

        // Iterate through the list picking off the gset and length information
        for (CavityResponse cr : cavList) {
            String cavName = cr.getEpicsName();
            String epicsName = cavName.substring(0, 3);
            int position = Integer.parseInt(cavName.substring(3, 4)) - 1;
            
            Double gset = (cr.getGset() == null) ? null : cr.getGset().doubleValue();
            double length = cr.getLength().doubleValue();

            if (!gsets.containsKey(epicsName)) {
                gsets.put(epicsName, new Double[8]);
            }
            if (!lengths.containsKey(epicsName)) {
                lengths.put(epicsName, new Double[8]);
            }

            gsets.get(epicsName)[position] = gset;
            lengths.get(epicsName)[position] = length;
            linacs.put(epicsName, cr.getLinacName());
            cmTypes.put(epicsName, cr.getCryomoduleType());
        }

        List<String> pvs = new ArrayList<>();
        for (String epicsName : gsets.keySet()) {
            pvs.add(epicsName + LEM_PV_HEAT_SUFFIX);
        }
        MyaService ms = new MyaService();
        Map<String, String> heats = ms.mySampler(pvs, date);
        for (String epicsName : gsets.keySet()) {
            Double heat = null;
            if (heats != null) {
                String pv = epicsName + LEM_PV_HEAT_SUFFIX;
                if (heats.containsKey(pv)) {
                    try {
                        heat = Double.parseDouble(heats.get(pv));
                    } catch (NumberFormatException e) {
                        LOGGER.log(Level.INFO, "Error parsing number from mySampler response for {0}={1}", new Object[]{pv, heats.get(pv)});
                    }
                } else {
                    LOGGER.log(Level.INFO, "mySampler did not return response for " + pv);
                }
            }
            String name = CebafNames.epicsZoneToCedZone(epicsName);
            CryomoduleDataPoint cdp = new CryomoduleDataPoint(name, epicsName, linacs.get(epicsName),
                    cmTypes.get(epicsName), gsets.get(epicsName), lengths.get(epicsName), heat);

            cmList.add(cdp);
        }

        return cmList;
    }
}
