package org.jlab.rfd.business.service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.jlab.rfd.business.filter.CommentFilter;
import org.jlab.rfd.business.util.DateUtil;
import org.jlab.rfd.config.AppConfig;
import org.jlab.rfd.model.CavityDataPoint;
import org.jlab.rfd.model.CavityDataSpan;
import org.jlab.rfd.model.CavityResponse;
import org.jlab.rfd.model.Comment;
import org.jlab.rfd.model.CryomoduleType;
import org.jlab.rfd.model.ModAnodeHarvester.CavityGsetData;
import org.jlab.rfd.model.TimeUnit;

/**
 * This service object returns objects related to Cryocavity information in a
 * number of different formats.
 *
 * @author adamc
 */
public class CavityService {

    private static final Logger LOGGER = Logger.getLogger(CavityService.class.getName());
    // Append the wrkspc argument to the end of this string
    public static final String CED_INVENTORY_URL = "http://ced.acc.jlab.org/inventory";

    // Caches getCavityData output.  The cached HashSets should be inserted with Collecitons.unmodifiableMap() to be safe.
    private static final ConcurrentHashMap<String, Set<CavityDataPoint>> CAVITY_CACHE = new ConcurrentHashMap<>();

    public SortedSet<String> getCavityNames() throws IOException {
        SortedSet<String> names = new TreeSet<>();

        String urlString = CED_INVENTORY_URL + "?t=Cryocavity&p=&out=json";
        URL url = new URL(urlString);
        InputStream is = url.openStream();
        try (JsonReader reader = Json.createReader(is)) {
            JsonArray elements = CEDUtils.processCEDResponse(reader);
            for (JsonObject elem : elements.getValuesAs(JsonObject.class)) {
                names.add(elem.getString("name"));
            }
        }
        return names;
    }

    public Set<CavityResponse> getCavityData(Date timestamp) throws IOException, ParseException, SQLException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        // Get the comments up through the end of the day of the specified date.  Then attach the comments to the cached
        // cavity data points later on.
        CommentService cs = new CommentService();
        CommentFilter filter = new CommentFilter(null, null, null, null, DateUtil.getEndOfDay(timestamp));
        Map<String, SortedSet<Comment>> comments = cs.getCommentsByTopic(filter);

        if (timestamp.after(new Date())) {
            return null;
        }

        String wrkspc = sdf.format(timestamp);
        String cavityQuery = "?t=CryoCavity&p=PhaseRMS,Q0,Qexternal,MaxGSET,OpsGsetMax,TripOffset,TripSlope,Length,"
                + "Bypassed,TunerBad,EPICSName,ModAnode,Housed_by&out=json&ced=history&wrkspc=" + wrkspc;

        // Chech the cache.  If not there, run the query, build the results, check that somebody else hasn't already inserted this
        // into the cache, then add the query result to the cache.
        if (CAVITY_CACHE.containsKey(cavityQuery)) {
            Set<CavityDataPoint> temp = CAVITY_CACHE.get(cavityQuery);
            Set<CavityResponse> out = this.createCommentResponseSet(temp, comments);
            return out;
        } else {

            Map<String, CryomoduleType> cmTypes = new CryomoduleService().getCryoModuleTypes(timestamp);
            Set<CavityDataPoint> data = new HashSet<>();

            //LOGGER.log(Level.FINEST, "CED Query: {0}", CED_INVENTORY_URL + cavityQuery);
            URL url = new URL(CED_INVENTORY_URL + cavityQuery);
            InputStream in = url.openStream();
            try (JsonReader reader = Json.createReader(in)) {
                JsonArray elements = CEDUtils.processCEDResponse(reader);
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
                if (gsets == null) {
                    throw new RuntimeException("MyaService returned null.  Likely requesting data from future date.");
                }

                // Get the ModAnodeHarvesterData
                ModAnodeHarvesterService mahs = new ModAnodeHarvesterService();
                Map<String, CavityGsetData> cgds = mahs.getCavityGsetData(timestamp);

                // If we got back ModAnodeHarvester data for this timestamp, make sure that we have data for every cavity.
                // We either need a whole data set or throw an error.  Except that we expect this behavior for injector cavities
                // since ModAnodeHarvester only runs against the North and South Linacs.  It's also possible (but should be
                // checked for in the data harvester) that the database has an incomplete set of cavities.  We only want to include
                // the data if all of the cavities are present.
                boolean useMAH = true;
                if (cgds == null) {
                    useMAH = false;
                } else {
                    for (JsonObject element : elements.getValuesAs(JsonObject.class)) {
                        String eName = element.getJsonObject("properties").getString("EPICSName");
                        if (cgds.get(eName) == null && (!Pattern.matches("^R0..", eName))) {
                            useMAH = false;
                            LOGGER.log(Level.FINEST, "ModAnodeHarvester data missing for {0} on {1}.  MAH data will not be included in response", new Object[]{eName, timestamp});
                        }
                    }
                }

                for (JsonObject element : elements.getValuesAs(JsonObject.class)) {
                    // Optional in CED - objects initialized to null are not required to be displayed on dashboard
                    BigDecimal mav = new BigDecimal(0);  // No ModAnode property indicates a zero modulating anode voltage is applied
                    // CED units: kilovolts  
                    boolean tunerBad = false; // CED note: false unless set
                    boolean bypassed = false; // CED note: false unless set
                    BigDecimal tripOffset = null;   // CED units: trips per shift
                    BigDecimal tripSlope = null;   // CED units: trips per shift
                    BigDecimal opsGsetMax = null;  // CED units: MeV/m

                    // Required in CED
                    String q0;
                    String qExternal;
                    BigDecimal maxGset; // CED units: MeV/m
                    BigDecimal length; // CED units: meters

                    String cavityName = element.getString("name");
                    cmType = cmTypes.get(cavityName.substring(0, 4));

                    JsonObject properties = element.getJsonObject("properties");

                    // Check for the existence of CED optional parameters
                    if (properties.containsKey("TunerBad")) {
                        tunerBad = true;
                    }
                    if (properties.containsKey("Bypassed")) {
                        bypassed = true;
                    }
                    if (properties.containsKey("TripOffset")) {
                        tripOffset = new BigDecimal(properties.getString("TripOffset"));
                    }
                    if (properties.containsKey("TripSlope")) {
                        tripSlope = new BigDecimal(properties.getString("TripSlope"));
                    }
                    if (properties.containsKey("ModAnode")) {
                        mav = mav.add(new BigDecimal(properties.getString("ModAnode")));
                    }
                    if (properties.containsKey("OpsGsetMax")) {
                        opsGsetMax = new BigDecimal(properties.getString("OpsGsetMax"));
                    }
                    if (properties.containsKey("EPICSName")) {
                        epicsName = properties.getString("EPICSName");
                    } else {
                        LOGGER.log(Level.WARNING, "Cryocavity '{0}' is missing EPICSName in ced history '{1}'.  Cannot process request.",
                                new Object[]{cavityName, wrkspc});
                        throw new IOException("Cryocavity '" + cavityName + "' missing EPICSName in ced history '" + wrkspc);
                    }

                    // Get all of the CED required parameters.  These should always be returned since they are "required" fields
                    q0 = properties.getString("Q0");
                    qExternal = properties.getString("Qexternal");
                    maxGset = new BigDecimal(properties.getString("MaxGSET"));
                    length = new BigDecimal(properties.getString("Length"));

                    BigDecimal odvh = maxGset;
                    // The ops drive high (odvh) is set to the opsGsetMax if it exists or GsetMax otherwise
                    if (opsGsetMax != null) {
                        odvh = opsGsetMax;
                    }

                    //  useMAH should cover the case that cgds is null
                    if (useMAH) {
                        data.add(new CavityDataPoint(timestamp, cavityName, cmType, mav, epicsName, gsets.get(cavityName),
                                odvh, q0, qExternal, maxGset, opsGsetMax, tripOffset, tripSlope,
                                length, cgds.get(epicsName), bypassed, tunerBad));
                    } else {
                        data.add(new CavityDataPoint(timestamp, cavityName, cmType, mav, epicsName, gsets.get(cavityName),
                                odvh, q0, qExternal, maxGset, opsGsetMax, tripOffset, tripSlope,
                                length, null, bypassed, tunerBad));
                    }
                }
            }

            // In case somebody has already inserted it use putIfAbsent.
            CAVITY_CACHE.putIfAbsent(cavityQuery, Collections.unmodifiableSet(data));

            return this.createCommentResponseSet(CAVITY_CACHE.get(cavityQuery), comments);
        }

    }

    /**
     * Convenience function for returning data as a map keyed on cavity name
     * instead of a set
     *
     * @param timestamp Timestamp to look up data for
     * @return A Map of cavity names to cavity response objects
     * @throws IOException
     * @throws ParseException
     * @throws SQLException
     */
    public Map<String, CavityResponse> getCavityDataMap(Date timestamp) throws IOException, ParseException, SQLException {
        Set<CavityResponse> crs = getCavityData(timestamp);

        Map<String, CavityResponse> cavMap = new HashMap<>();
        for (CavityResponse cr : crs) {
            cavMap.put(cr.getCavityName(), cr);
        }

        return cavMap;
    }

    /**
     * Utility function for creating a set of CavityResponse objects. Grabs the
     * latest comment for each comment and adds it to the CavityResponse. Note:
     * The CavityDataPoints and the Comments should be from the same date for
     * this to make sense.
     *
     * @param cavities The CavityDataPoints for a given date
     * @param comments The comments split by topic (getCommentsByTopic) up until
     * the end of the requested date
     * @return
     */
    private Set<CavityResponse> createCommentResponseSet(Set<CavityDataPoint> cavities, Map<String, SortedSet<Comment>> comments) {
        Set<CavityResponse> cr = new HashSet<>();
        for (CavityDataPoint cdp : cavities) {
            SortedSet<Comment> temp = comments.get(cdp.getCavityName());
            if (temp == null) {
                cr.add(new CavityResponse(cdp, null));
            } else {
                cr.add(new CavityResponse(cdp, temp.first()));
            }
        }
        return cr;
    }

    /*
    * If end is for future date, set it for now.  Data isn't valid for future dates, but some of our data services (CED, MYA) give results anyway.
     */
    public CavityDataSpan getCavityDataSpan(Date start, Date end, TimeUnit timeUnit) throws ParseException, IOException, SQLException {
        int timeInt;
        switch (timeUnit) {
            case DAY:
                timeInt = Calendar.DATE;
                break;
            case WEEK:
            default:
                timeInt = Calendar.WEEK_OF_YEAR;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        // Convert date objects to have no hh:mm:ss ... portion
        Date curr = sdf.parse(sdf.format(start));
        Date e = sdf.parse(sdf.format(end));
        Calendar cal = Calendar.getInstance();
        cal.setTime(curr);

        if (end.after(new Date())) {
            e = sdf.parse(sdf.format(new Date()));
        }

        CavityDataSpan span = new CavityDataSpan();

        while (!curr.after(e)) {
            span.put(curr, this.getCavityData(curr));
            cal.add(timeInt, 1);
            curr = cal.getTime();
        }

        return span;
    }

    public CavityDataSpan getCavityDataSpan(List<Date> dates) throws ParseException, IOException, SQLException {

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        CavityDataSpan span = new CavityDataSpan();
        for (Date d : dates) {
            // Convert date objects to have no hh:mm:ss ... portion
            d = sdf.parse(sdf.format(d));
            span.put(d, this.getCavityData(d));
        }
        return span;
    }
}
