/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.business.service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.jlab.rfd.model.CavityDataPoint;
import org.jlab.rfd.model.CavityDataSpan;
import org.jlab.rfd.model.CryomoduleType;
import org.jlab.rfd.model.ModAnodeHarvester.CavityGsetData;
import org.jlab.rfd.model.TimeUnit;

/**
 * Returns null if timestamp is later than today
 * @author adamc
 */
public class CavityService {

    private static final Logger LOGGER = Logger.getLogger(CavityService.class.getName());
    // Append the wrkspc argument to the end of this string
    public static final String CED_INVENTORY_URL = "http://ced.acc.jlab.org/inventory";

    // Caches getCavityData output.  The cached HashSets should be inserted with Collecitons.unmodifiableMap() to be safe.
    private static final ConcurrentHashMap<String, Set<CavityDataPoint>> CAVITY_CACHE = new ConcurrentHashMap<>();

    public Set<CavityDataPoint> getCavityData(Date timestamp) throws IOException, ParseException, SQLException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        
        if ( timestamp.after(new Date()) ) {
            return null;
        }

        String wrkspc = sdf.format(timestamp);
        String cavityQuery = "?t=CryoCavity&p=EPICSName,ModAnode,Housed_by&out=json&ced=history&wrkspc=" + wrkspc;

        // Chech the cache.  If not there, run the query, build the results, check that somebody else hasn't already inserted this
        // into the cache, then add the query result to the cache.
        if (CAVITY_CACHE.containsKey(cavityQuery)) {
            Set<CavityDataPoint> out = CAVITY_CACHE.get(cavityQuery);
            return out;
        } else {

            Map<String, CryomoduleType> cmTypes = new CryomoduleService().getCryoModuleTypes(timestamp);
            Set<CavityDataPoint> data = new HashSet<>();
            
            //LOGGER.log(Level.FINEST, "CED Query: {0}", CED_INVENTORY_URL + cavityQuery);
            URL url = new URL(CED_INVENTORY_URL + cavityQuery);
            InputStream in = url.openStream();
            try (JsonReader reader = Json.createReader(in)) {
                JsonObject json = reader.readObject();
                String status = json.getString("stat");
                if (!"ok".equals(status)) {
                    throw new IOException("unable to lookup Cavity Data from CED: response stat: " + status);
                }
                JsonObject inventory = json.getJsonObject("Inventory");
                JsonArray elements = inventory.getJsonArray("elements");
                CryomoduleType cmType;

                // Construct a map of ced names to epics names
                String epicsName;
                Map<String, String> name2Epics = new HashMap<>();
                for (JsonObject element : elements.getValuesAs(JsonObject.class)) {
                    String cavityName = element.getString("name");
                    JsonObject properties = element.getJsonObject("properties");
                    if (properties.containsKey("EPICSName")) {
                        epicsName = properties.getString("EPICSName");
                    } else {
                        LOGGER.log(Level.WARNING, "Cryocavity '{0}' is missing EPICSName in ced history '{1}'.  Cannot process request.",
                                new Object[]{cavityName, wrkspc});
                        throw new IOException("Cryocavity '" + cavityName + "' missing EPICSName in ced history '" + wrkspc);
                    }
                    name2Epics.put(cavityName, epicsName);
                }
                MyaService ms = new MyaService();
                
                // These could return null if timestamp is a future date.  Shouldn't happen as we check end before, but let''s check
                // and throw an exception if it does happen.
                Map<String, BigDecimal> gsets = ms.getCavityMyaData(timestamp, name2Epics, "GSET");
                Map<String, BigDecimal> odvhs = ms.getCavityMyaData(timestamp, name2Epics, "ODVH");
                if ( gsets == null || odvhs == null ) {
                    throw new RuntimeException("MyaService returned null.  Likely requesting data from future date.");
                }

                // Get the ModAnodeHarvesterData
                ModAnodeHarvesterService mahs = new ModAnodeHarvesterService();
                Map<String, CavityGsetData> cgds = mahs.getCavityGsetData(timestamp);
                
                for (JsonObject element : elements.getValuesAs(JsonObject.class)) {
                    BigDecimal mav = new BigDecimal(0);
                    String cavityName = element.getString("name");
                    cmType = cmTypes.get(cavityName.substring(0, 4));

                    JsonObject properties = element.getJsonObject("properties");
                    if (properties.containsKey("ModAnode")) {
                        mav = mav.add(new BigDecimal(properties.getString("ModAnode")));
                    }
                    if (properties.containsKey("EPICSName")) {
                        epicsName = properties.getString("EPICSName");
                    } else {
                        LOGGER.log(Level.WARNING, "Cryocavity '{0}' is missing EPICSName in ced history '{1}'.  Cannot process request.",
                                new Object[]{cavityName, wrkspc});
                        throw new IOException("Cryocavity '" + cavityName + "' missing EPICSName in ced history '" + wrkspc);
                    }
                    
                    // If we got back ModAnodeHarvester data for this timestamp, make sure that we have data for every cavity.
                    // We either need a whole data set or throw an error.  Except that we expect this behavior for injector cavities
                    // since ModAnodeHarvester only runs against the North and South Linacs... ugh ...
                    if ( cgds != null && Pattern.matches("^R0*", epicsName) && cgds.get(epicsName) == null) {
                        throw new RuntimeException("ModAnodeHarvester data missing for " + epicsName + " on '" + timestamp);
                    }
                    data.add(new CavityDataPoint(timestamp, cavityName, cmType, mav, epicsName, gsets.get(cavityName),
                            odvhs.get(cavityName), cgds.get(epicsName)));
                }
            }

            // In case somebody has already inserted it use putIfAbsent.
            CAVITY_CACHE.putIfAbsent(cavityQuery, Collections.unmodifiableSet(data));            
            return CAVITY_CACHE.get(cavityQuery);
        }

    }

    
    /*
    * If end is for future date, set it for now.  Data isn't valid for future dates, but some of our data services (CED, MYA) give results anyway.
    */
    public CavityDataSpan getCavityDataSpan(Date start, Date end, TimeUnit timeUnit) throws ParseException, IOException, SQLException {
        long timeInt;
        switch (timeUnit) {
            case DAY:
                timeInt = 60 * 60 * 24 * 1000L;
                break;
            case WEEK:
            default:
                timeInt = 60 * 60 * 24 * 7 * 1000L;
        }
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        // Convert date objects to have no hh:mm:ss ... portion
        Date curr = sdf.parse(sdf.format(start));
        Date e = sdf.parse(sdf.format(end));

        if ( end.after(new Date()) ) {
            e = sdf.parse(sdf.format(new Date()));
        }

        CavityDataSpan span = new CavityDataSpan();

        while (curr.before(e)) {
            span.put(curr, this.getCavityData(curr));
            curr = new Date(curr.getTime() + timeInt);            // Increment by time interval
        }

        return span;
    }
    
        public CavityDataSpan getCavityDataSpan(List<Date> dates) throws ParseException, IOException, SQLException {
        
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        CavityDataSpan span = new CavityDataSpan();
        for(Date d : dates) {
            // Convert date objects to have no hh:mm:ss ... portion
            d = sdf.parse(sdf.format(d));
            span.put(d, this.getCavityData(d));
        }
        return span;
    }
}