package org.jlab.rfd.business.service;

import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;

import org.jlab.rfd.business.filter.CommentFilter;
import org.jlab.rfd.business.util.DateUtil;
import org.jlab.rfd.business.util.SqlUtil;
import org.jlab.rfd.config.AppConfig;
import org.jlab.rfd.model.*;
import org.jlab.rfd.model.ModAnodeHarvester.CavityGsetData;

/**
 * This service object returns objects related to Cryocavity information in a
 * number of different formats.
 *
 * @author adamc
 */
public class CavityService {

    private static final Logger LOGGER = Logger.getLogger(CavityService.class.getName());
    // Append the wrkspc argument to the end of this string
    public static final String CED_INVENTORY_URL = AppConfig.getAppConfig().getCEDUrl() + "/inventory";

    // This manages concurrent access to the cache
    private static final Object CACHE_LOCK = new Object();

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

    /**
     * Check the cache to see if we have this response already.
     *
     * @param date The date for which data is cached
     * @return The Set of CavityDataPoints associated with the query or null if cache miss
     */
    private Set<CavityDataPoint> readCache(Date date) throws SQLException, ParseException, IOException {
        Set<CavityDataPoint> data = null;
        synchronized (CACHE_LOCK) {
            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            String sql = "SELECT * FROM CAVITY_CACHE WHERE QUERY_DATE = TO_DATE(?, 'YYYY-MM-DD')";
            try {
                conn = SqlUtil.getConnection();
                pstmt = conn.prepareStatement(sql);

                pstmt.setString(1, DateUtil.formatDateYMD(date));
                rs = pstmt.executeQuery();

                // If we don't have the data cached, return null
                if (rs.isBeforeFirst()) {
                    data = new HashSet<>();
                    Date d;
                    String cavityName, epicsName, q0, qExternal;
                    CryomoduleType cryoModuleType;
                    boolean bypassed, tunerBad;
                    BigDecimal modAnodeVoltage, gset, odvh, maxGset, opsGsetMax, tripSlope, tripOffset, length;
                    CavityDataPoint cdp;
                    while (rs.next()) {
                        d = rs.getDate("QUERY_DATE");
                        cavityName = rs.getString("CAVITY_NAME");
                        epicsName = rs.getString("EPICS_NAME");
                        modAnodeVoltage = BigDecimal.valueOf(rs.getDouble("MOD_ANODE_VOLTAGE"));
                        cryoModuleType = CryomoduleType.valueOf(rs.getString("CRYOMODULE_TYPE"));
                        gset = getBigDecimal(rs, "GSET");
                        odvh = getBigDecimal(rs, "ODVH");
                        q0 = rs.getString("Q0");
                        qExternal = rs.getString("QEXTERNAL");
                        maxGset = getBigDecimal(rs, "MAX_GSET");
                        opsGsetMax = getBigDecimal(rs, "OPS_GSET_MAX");
                        tripOffset = getBigDecimal(rs, "TRIP_OFFSET");
                        tripSlope = getBigDecimal(rs, "TRIP_SLOPE");
                        length = getBigDecimal(rs, "LENGTH");
                        bypassed = rs.getInt("BYPASSED") == 1;
                        tunerBad = rs.getInt("TUNER_BAD") == 1;

                        cdp = new CavityDataPoint(d, cavityName, cryoModuleType, modAnodeVoltage, epicsName, gset, odvh, q0,
                                qExternal, maxGset, opsGsetMax, tripOffset, tripSlope, length, null, bypassed, tunerBad);
                        data.add(cdp);
                    }
                }
                rs.close();
            } finally {
                SqlUtil.close(rs, pstmt, conn);
            }
        }

        if (data == null) {
            return null;
        }

        // If we haven't returned null already, then we expect there to be something in the cavity cache.
        // The mod anode simulation data is already saved in the database.  Query that and update the existing data points.
        ModAnodeHarvesterService mahs = new ModAnodeHarvesterService();
        Map<String, CavityGsetData> mahData = mahs.getCavityGsetData(date);
        Set<CavityDataPoint> out;
        if (mahData == null || mahData.isEmpty()) {
            out = data;
        } else {
            out = new HashSet<>();
            for (CavityDataPoint cdp : data) {
                CavityGsetData mah = mahData.get(cdp.getCavityName());
                if (mah != null) {
                    CavityDataPoint copy = new CavityDataPoint(cdp.getTimestamp(), cdp.getCavityName(),
                            cdp.getCryomoduleType(), cdp.getModAnodeVoltage(), cdp.getEpicsName(), cdp.getGset(),
                            cdp.getOdvh(), cdp.getQ0(), cdp.getqExternal(), cdp.getMaxGset(), cdp.getOpsGsetMax(),
                            cdp.getTripOffset(), cdp.getTripSlope(), cdp.getLength(), mah, cdp.isBypassed(),
                            cdp.isTunerBad());
                    out.add(copy);
                } else {
                    out.add(cdp);
                }
            }
        }

        return out;
    }

    /**
     * Utility function for reading BigDecimal values (saved as doubles) from a ResultSet.
     *
     * @param rs         The ResultSet from an executed statement
     * @param columnName The name of the column to process into a BigDecimal
     * @return The value as a BigDecimal.  Null if the database had a null value
     * @throws SQLException If something goes wrong communicating with the database
     */
    private BigDecimal getBigDecimal(ResultSet rs, String columnName) throws SQLException {
        BigDecimal out;
        double temp;
        temp = rs.getDouble(columnName);
        if (rs.wasNull()) {
            out = null;
        } else {
            out = BigDecimal.valueOf(temp);
        }
        return out;
    }

    /**
     * Utility function for setting BigDecimal values (as doubles) to a PreparedStatement.
     *
     * @param value  The BigDecimal value to be written to the database
     * @param stmt   The PreparedStatement to update
     * @param index  The index of the current parameter to be updated
     * @return The incremented index
     * @throws SQLException If something goes wrong communicating with the database
     */
    private int setBigDecimal(BigDecimal value, PreparedStatement stmt, int index) throws SQLException {
        if (value == null) {
            stmt.setNull(index, Types.NULL);
        } else {
            stmt.setDouble(index, value.doubleValue());
        }
        return index + 1;
    }

    /**
     * Cache the cavity data in the performance improvements.
     * Cavity data can take a long time to query from the CED as each day has to be queried individually.  At the last
     * performance test, a single query can take ~0.3 seconds, which adds up when considering a month or a year's worth
     * of data.
     *
     * @param date The date to use when reading/writing from/to the cache
     * @param data The set of cavity data to cache
     * @throws SQLException On problem communicating with database
     */
    private void writeCache(Date date, Set<CavityDataPoint> data) throws SQLException, IllegalArgumentException {
        synchronized (CACHE_LOCK) {

            // Check that all cavities have the same date
            for (CavityDataPoint cdp : data) {
                if (!cdp.getTimestamp().equals(date)) {
                    throw new IllegalArgumentException("Cavity '" + cdp.getCavityName() + "' has different date (" +
                            DateUtil.formatDateYMD(cdp.getTimestamp()) + ") than used for caching (" +
                            DateUtil.formatDateYMD(date) + ").");
                }
            }

            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;
            String checkSQL = "SELECT QUERY_DATE FROM CAVITY_CACHE WHERE QUERY_DATE = TO_DATE(?, 'YYYY-MM-DD')";
            String sql = "INSERT INTO CAVITY_CACHE (CACHE_ID, QUERY_DATE, CAVITY_NAME, EPICS_NAME, " +
                    " MOD_ANODE_VOLTAGE, CRYOMODULE_TYPE, GSET, ODVH, Q0, QEXTERNAL," +
                    " MAX_GSET, OPS_GSET_MAX, TRIP_OFFSET, TRIP_SLOPE, LENGTH, BYPASSED, TUNER_BAD) " +
                    "VALUES (CAVITY_CACHE_SEQ.NEXTVAL, TO_DATE(?, 'YYYY-MM-DD'), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?," +
                    " ?, ?)";
            try {
                conn = SqlUtil.getConnection();
                conn.setAutoCommit(false);

                // Check that another thread didn't cache this before us (i.e., we lost the race).
                pstmt = conn.prepareStatement(checkSQL);
                pstmt.setString(1, DateUtil.formatDateYMD(date));
                rs = pstmt.executeQuery();
                if (!rs.isBeforeFirst()) {
                    rs.close();
                    pstmt.close();
                    pstmt = conn.prepareStatement(sql);
                    int i, numUpdated;
                    for (CavityDataPoint cdp : data) {
                        i = 1; // Since the sequence is the first
                        pstmt.setString(i++, DateUtil.formatDateYMD(date));
                        pstmt.setString(i++, cdp.getCavityName());
                        pstmt.setString(i++, cdp.getEpicsName());
                        i = setBigDecimal(cdp.getModAnodeVoltage(), pstmt, i);
                        pstmt.setString(i++, cdp.getCryomoduleType().toString());
                        i = setBigDecimal(cdp.getGset(), pstmt, i);
                        i = setBigDecimal(cdp.getOdvh(), pstmt, i);
                        pstmt.setString(i++, cdp.getQ0());
                        pstmt.setString(i++, cdp.getqExternal());
                        pstmt.setDouble(i++, cdp.getMaxGset().doubleValue());
                        i = setBigDecimal(cdp.getOpsGsetMax(), pstmt, i);
                        i = setBigDecimal(cdp.getTripOffset(), pstmt, i);
                        i = setBigDecimal(cdp.getTripSlope(), pstmt, i);
                        pstmt.setDouble(i++, cdp.getLength().doubleValue());
                        pstmt.setInt(i++, cdp.isBypassed() ? 1 : 0);
                        pstmt.setInt(i, cdp.isTunerBad() ? 1 : 0);

                        numUpdated = pstmt.executeUpdate();
                        if (numUpdated != 1) {
                            conn.rollback();
                            throw new SQLException("Error adding cavity data ('" + cdp.getCavityName() + "') to database cache");
                        }
                    }
                    conn.commit();
                }
            } finally {
                SqlUtil.close(rs, pstmt, conn);
            }
        }
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

        // Check the cache.  If not there, run the query, build the results, check that somebody else hasn't already inserted this
        // into the cache, then add the query result to the cache.
        Set<CavityDataPoint> data = null;
        try {
            data = readCache(timestamp);
        } catch (SQLException | ParseException | IOException ex) {
            LOGGER.log(Level.WARNING, "Error reading from cavity cache", ex);
        }

        // If cache miss
        if (data == null) {
            LOGGER.log(Level.INFO, "Fetching cavity data for " + timestamp + ".");
            Map<String, CryomoduleType> cmTypes = new CryomoduleService().getCryoModuleTypes(timestamp);
            data = new HashSet<>();

            //LOGGER.log(Level.FINEST, "CED Query: {0}", CED_INVENTORY_URL + cavityQuery);
            URL url = new URL(CED_INVENTORY_URL + cavityQuery);
            InputStream in = url.openStream();
            try (JsonReader reader = Json.createReader(in)) {
                JsonArray elements = CEDUtils.processCEDResponse(reader);
                CryomoduleType cmType;

                // Construct a map of ced names to epics names
                String epicsName;
                Map<String, String> name2Epics = new HashMap<>();
                Set<String> names6GEV = new HashSet<>();
                Set<String> names12GEV = new HashSet<>();
                for (JsonObject element : elements.getValuesAs(JsonObject.class)) {
                    String cavityName = element.getString("name");
                    String zoneName = cavityName.substring(0, 4);
                    JsonObject properties = element.getJsonObject("properties");
                    if (properties.containsKey("EPICSName")) {
                        epicsName = properties.getString("EPICSName");
                    } else {
                        LOGGER.log(Level.WARNING, "Cryocavity '{0}' is missing EPICSName in ced history '{1}'.  Cannot process request.",
                                new Object[]{cavityName, wrkspc});
                        throw new IOException("Cryocavity '" + cavityName + "' missing EPICSName in ced history '" + wrkspc);
                    }
                    name2Epics.put(cavityName, epicsName);
                    if (cmTypes.get(zoneName).equals(CryomoduleType.C25) ||
                            cmTypes.get(zoneName).equals(CryomoduleType.C50) ||
                            cmTypes.get(zoneName).equals(CryomoduleType.C50T)) {
                        names6GEV.add(cavityName);
                    } else {
                        names12GEV.add(cavityName);
                    }
                }
                MyaService ms = new MyaService();

                // We switched from CED to EPICS/MYA for tracking ModAnodeVolts in 2022.
                boolean useMyaMAV = timestamp.after(DateUtil.parseDateString("2022-01-01"));

                Map<String, List<String>> postfixes = new HashMap<>();
                for (String name : name2Epics.keySet()) {
                    postfixes.put(name, new ArrayList<>());
                    if (useMyaMAV) {
                        if (names6GEV.size() + names12GEV.size() < 25) {
                            // sanity check
                            throw new RuntimeException("Error querying cavity data.  Only found < 25 cavities.");
                        }

                        if (names6GEV.contains(name)) {
                            postfixes.get(name).add("ModAnodeVolts");
                        } else {
                            postfixes.get(name).add("KMAS");
                        }
                    }
                }

                // These could return null if timestamp is a future date.  Shouldn't happen as we check end before, but let''s check
                // and throw an exception if it does happen.  URL is too long unless we split GSET from the rest.
                Map<String, BigDecimal> gsets = ms.getCavityMyaData(timestamp, name2Epics, "GSET");
                if (gsets == null) {
                    throw new RuntimeException("MyaService returned null.  Likely requesting data from future date.");
                }


                Map<String, BigDecimal> mavsEPICS = null;
                if (useMyaMAV) {
                    mavsEPICS = ms.getCavityMyaData(timestamp, name2Epics, postfixes);
                    if (mavsEPICS == null) {
                        throw new RuntimeException("MyaService returned null.  Likely requesting data from future date.");
                    }
                }

                // Get the ModAnodeHarvesterData
                ModAnodeHarvesterService mahs = new ModAnodeHarvesterService();
                Map<String, CavityGsetData> cgds = mahs.getCavityGsetData(timestamp);

                // If we got back ModAnodeHarvester data for this timestamp, make sure that we have data for every cavity.
                // We either need a whole data set or throw an error.  Except that we expect this behavior for injector cavities
                // since ModAnodeHarvester only runs against the North and South Linacs.  It's also possible (but should be
                // checked for in the data harvester) that the database has an incomplete set of cavities.  We only want to include
                // the data if all cavities are present.
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
                    if (mavsEPICS != null) {
                        mav = mavsEPICS.get(cavityName);
                    } else {
                        if (properties.containsKey("ModAnode")) {
                            mav = mav.add(new BigDecimal(properties.getString("ModAnode")));
                        }
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

                    // Get all CED required parameters.  These should always be returned since they are "required" fields
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
            try {
                writeCache(timestamp, data);
            } catch (SQLException | IllegalArgumentException ex) {
                LOGGER.log(Level.WARNING, "Error writing to cavity cache", ex);
            }
        }

        return this.createCommentResponseSet(data, comments);
    }

    /**
     * Convenience function for returning data as a map keyed on cavity name
     * instead of a set
     *
     * @param timestamp Timestamp to look up data for
     * @return A Map of cavity names to cavity response objects
     * @throws IOException    On non-ok CED server response
     * @throws ParseException On error parsing JSON responses
     * @throws SQLException   On database error
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
     *                 the end of the requested date
     * @return A set of CavityResponse objects
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
