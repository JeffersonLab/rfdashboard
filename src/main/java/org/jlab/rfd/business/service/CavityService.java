package org.jlab.rfd.business.service;

import java.io.IOException;
import java.io.InputStream;
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
    private static final String CED_INVENTORY_URL = AppConfig.getAppConfig().getCEDUrl() + "/inventory";
    private static final String MAV_MOVED_TO_MYA_DATE = "2022-03-01";
    private static final int CACHE_QUERY_CHUNK_SIZE = 180;
    private static final String CAVITY_TYPE_INTRODUCED = "2018-11-01";

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
     * Delete cached cavity data corresponding to a particular date
     * @param date The date for which we want to flush the cache
     * @return The number of rows that were deleted from the cache table
     * @throws SQLException If something goes wrong communicating with the database.
     */
    public int clearCache(Date date) throws SQLException {
        int rowsDeleted = 0;
        if (date == null) {
            return rowsDeleted;
        }

        synchronized (CACHE_LOCK) {
            Connection conn = null;
            PreparedStatement pstmt = null;
            String sql = "DELETE from CAVITY_CACHE WHERE QUERY_DATE = TO_DATE(?, 'YYYY-MM-DD')";
            try {
                conn = SqlUtil.getConnection();
                pstmt = conn.prepareStatement(sql);
                pstmt.setString(1, DateUtil.formatDateYMD(date));
                rowsDeleted = pstmt.executeUpdate();
            } finally {
                SqlUtil.close(pstmt, conn);
            }
        }
        return rowsDeleted;
    }

    private String getCacheReadSql(int numDates) {
        if (numDates < 1) {
            throw new IllegalArgumentException("Cache query must be for at least one date");
        }
        StringBuilder sql = new StringBuilder("SELECT * FROM CAVITY_CACHE WHERE QUERY_DATE IN" +
                " (TO_DATE(?, 'YYYY-MM-DD')");

        // Only do this if we've requested more than one date.
        for (int i = 1; i < numDates; i++) {
            sql.append(", TO_DATE(?, 'YYYY-MM-DD')");
        }
        sql.append(")");

        return sql.toString();
    }

    /**
     * Query the cache for a single date.
     * @param date The date to query for
     * @return A set of cavity data
     * @throws SQLException  If a problem happens while querying the database
     * @throws ParseException If a problem happens while formatting/parsing date strings
     */
    public Set<CavityDataPoint> readCache(Date date) throws SQLException, ParseException {
        List<Date> dates = new ArrayList<>();
        dates.add(date);
        Map<Date, Set<CavityDataPoint>> data = readCache(dates);
        if (data == null) {
            return null;
        }
        return data.get(date);
    }


        /**
         * Check the cache to see if we have data for these dates.
         *
         * @param dates The list dates for which cached data should be queried
         * @return The Set of CavityDataPoints associated with the query or null if cache miss
         * @throws SQLException If a problem happens while querying the database
         * @throws ParseException If a problem happens while formatting/parsing date strings
         */
    public Map<Date, Set<CavityDataPoint>> readCache(List<Date> dates) throws SQLException, ParseException {
        Map<Date, Set<CavityDataPoint>> dataMap = null;

        // SQL can grow too large over many years.  Check if we need to break the query into chunks.
        if (dates.size() > CACHE_QUERY_CHUNK_SIZE) {
            int numChunks = (int) Math.ceil(dates.size() / (double) CACHE_QUERY_CHUNK_SIZE);
            Map<Date, Set<CavityDataPoint>> chunkMap;
            dataMap = new HashMap<>();
            for (int i = 0; i < numChunks; i++) {
                int end = Math.min((i + 1) * CACHE_QUERY_CHUNK_SIZE - 1, dates.size());
                int start = i * CACHE_QUERY_CHUNK_SIZE;
                chunkMap = readCache(dates.subList(start, end));
                if (chunkMap != null) {
                    dataMap.putAll(chunkMap);
                }
            }
        } else {
            synchronized (CACHE_LOCK) {
                Connection conn = null;
                PreparedStatement pstmt = null;
                ResultSet rs = null;
                String sql = getCacheReadSql(dates.size());
                try {
                    conn = SqlUtil.getConnection();
                    pstmt = conn.prepareStatement(sql);

                    for (int i = 0; i < dates.size(); i++) {
                        pstmt.setString(i + 1, DateUtil.formatDateYMD(dates.get(i)));
                    }
                    rs = pstmt.executeQuery();

                    // If we don't have the data cached, return null.  Happens after the processing block.
                    if (rs.isBeforeFirst()) {
                        dataMap = new HashMap<>();
                        Date d;
                        String cavityName, epicsName, q0, qExternal;
                        CryomoduleType cryoModuleType;
                        CavityType cavityType;
                        String cavityTypeString;
                        boolean bypassed, tunerBad;
                        Double modAnodeVoltage, gset, odvh, maxGset, opsGsetMax, tripSlope, tripOffset, length;
                        CavityDataPoint cdp;
                        while (rs.next()) {
                            d = rs.getDate("QUERY_DATE");
                            cavityName = rs.getString("CAVITY_NAME");
                            epicsName = rs.getString("EPICS_NAME");
                            modAnodeVoltage = getDouble(rs, "MOD_ANODE_VOLTAGE");
                            cryoModuleType = CryomoduleType.valueOf(rs.getString("CRYOMODULE_TYPE"));
                            cavityTypeString = rs.getString("CAVITY_TYPE");
                            if (rs.wasNull() || cavityTypeString.isEmpty()) {
                                cavityType = CavityType.None;
                            } else {
                                cavityType = CavityType.valueOf(cavityTypeString);
                            }
                            gset = getDouble(rs, "GSET");
                            odvh = getDouble(rs, "ODVH");
                            q0 = rs.getString("Q0");
                            qExternal = rs.getString("QEXTERNAL");
                            maxGset = getDouble(rs, "MAX_GSET");
                            opsGsetMax = getDouble(rs, "OPS_GSET_MAX");
                            tripOffset = getDouble(rs, "TRIP_OFFSET");
                            tripSlope = getDouble(rs, "TRIP_SLOPE");
                            length = getDouble(rs, "LENGTH");
                            bypassed = rs.getInt("BYPASSED") == 1;
                            tunerBad = rs.getInt("TUNER_BAD") == 1;

                            cdp = new CavityDataPoint(d, cavityName, cavityType, cryoModuleType, modAnodeVoltage, epicsName, gset, odvh,
                                    q0, qExternal, maxGset, opsGsetMax, tripOffset, tripSlope, length, null, bypassed, tunerBad);
                            dataMap.putIfAbsent(d, new HashSet<>());
                            dataMap.get(d).add(cdp);
                        }
                    }
                    rs.close();
                } finally {
                    SqlUtil.close(rs, pstmt, conn);
                }
            }
        }

        if (dataMap == null) {
            return null;
        }

        Map<Date, Set<CavityDataPoint>> outMap = new HashMap<>();
        for (Date date : dataMap.keySet()) {
            // If we haven't returned null already, then we expect there to be something in the cavity cache.
            // The mod anode simulation data is already saved in the database.  Query that and update the existing data points.
            ModAnodeHarvesterService mahs = new ModAnodeHarvesterService();
            Map<String, CavityGsetData> mahData = mahs.getCavityGsetData(date);
            Set<CavityDataPoint> out;
            if (mahData == null || mahData.isEmpty()) {
                outMap.put(date, dataMap.get(date));
            } else {
                out = new HashSet<>();
                for (CavityDataPoint cdp : dataMap.get(date)) {
                    CavityGsetData mah = mahData.get(cdp.getCavityName());
                    if (mah != null) {
                        CavityDataPoint copy = new CavityDataPoint(cdp.getTimestamp(), cdp.getCavityName(),
                                cdp.getCavityType(), cdp.getCryomoduleType(), cdp.getModAnodeVoltage(),
                                cdp.getEpicsName(), cdp.getGset(), cdp.getOdvh(), cdp.getQ0(), cdp.getqExternal(),
                                cdp.getMaxGset(), cdp.getOpsGsetMax(), cdp.getTripOffset(), cdp.getTripSlope(),
                                cdp.getLength(), mah, cdp.isBypassed(), cdp.isTunerBad());
                        out.add(copy);
                    } else {
                        out.add(cdp);
                    }
                }
                outMap.put(date, out);
            }
        }

        return outMap;
    }

    /**
     * Utility function for reading Double values (saved as doubles) from a ResultSet.
     *
     * @param rs         The ResultSet from an executed statement
     * @param columnName The name of the column to process into a Double
     * @return The value as a Double.  Null if the database had a null value
     * @throws SQLException If something goes wrong communicating with the database
     */
    private Double getDouble(ResultSet rs, String columnName) throws SQLException {
        Double out;
        out = rs.getDouble(columnName);
        if (rs.wasNull()) {
            out = null;
        }
        return out;
    }

    /**
     * Utility function for setting Double values (as doubles) to a PreparedStatement.
     *
     * @param value  The Double value to be written to the database
     * @param stmt   The PreparedStatement to update
     * @param index  The index of the current parameter to be updated
     * @return The incremented index
     * @throws SQLException If something goes wrong communicating with the database
     */
    private int setDouble(Double value, PreparedStatement stmt, int index) throws SQLException {
        if (value == null) {
            stmt.setNull(index, Types.NULL);
        } else {
            stmt.setDouble(index, value);
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
            // 18 names
            //
            String sql = "INSERT INTO CAVITY_CACHE (CACHE_ID, QUERY_DATE, CAVITY_NAME, EPICS_NAME, " +
                    " MOD_ANODE_VOLTAGE, CAVITY_TYPE, CRYOMODULE_TYPE, GSET, ODVH, Q0, QEXTERNAL," +
                    " MAX_GSET, OPS_GSET_MAX, TRIP_OFFSET, TRIP_SLOPE, LENGTH, BYPASSED, TUNER_BAD) " +
                    "VALUES (CAVITY_CACHE_SEQ.NEXTVAL, TO_DATE(?, 'YYYY-MM-DD'), ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?," +
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
                        try {
                            i = 1; // Since the sequence is the first
                            pstmt.setString(i++, DateUtil.formatDateYMD(date));
                            pstmt.setString(i++, cdp.getCavityName());
                            pstmt.setString(i++, cdp.getEpicsName());
                            i = setDouble(cdp.getModAnodeVoltage(), pstmt, i);
                            if (cdp.getCavityType().equals(CavityType.None)) {
                                pstmt.setString(i++, "");
                            } else {
                                pstmt.setString(i++, cdp.getCavityType().toString());
                            }
                            pstmt.setString(i++, cdp.getCryomoduleType().toString());
                            i = setDouble(cdp.getGset(), pstmt, i);
                            i = setDouble(cdp.getOdvh(), pstmt, i);
                            pstmt.setString(i++, cdp.getQ0());
                            pstmt.setString(i++, cdp.getqExternal());
                            pstmt.setDouble(i++, cdp.getMaxGset());
                            i = setDouble(cdp.getOpsGsetMax(), pstmt, i);
                            i = setDouble(cdp.getTripOffset(), pstmt, i);
                            i = setDouble(cdp.getTripSlope(), pstmt, i);
                            pstmt.setDouble(i++, cdp.getLength());
                            pstmt.setInt(i++, cdp.isBypassed() ? 1 : 0);
                            pstmt.setInt(i, cdp.isTunerBad() ? 1 : 0);

                            numUpdated = pstmt.executeUpdate();
                        } catch (SQLException ex) {
                            // Which cavity triggered the error gets lost without catch and rethrow.
                            LOGGER.log(Level.WARNING, "Error caching cavity data on this cavity: " + cdp.toJson().toString());
                            throw ex;
                        }
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

    /**
     * Get data for all cavities from all linacs.
     * @param timestamp  The date to query data for
     * @return A set of cavity responses with data for the requested date
     * @throws IOException
     * @throws ParseException
     * @throws SQLException
     */
    public Set<CavityResponse> getCavityData(Date timestamp) throws IOException, ParseException, SQLException {
        return getCavityData(timestamp, null);
    }

    /**
     * Get data for all cavities for a given date with an optional filter on linac.
     * @param timestamp  The date to query data for
     * @param linacName  Only return data for cavities that are in the specified linac.  All cavities if null.
     * @return A set of cavity responses with data for the requested date
     * @throws IOException
     * @throws ParseException
     * @throws SQLException
     */
    public Set<CavityResponse> getCavityData(Date timestamp, LinacName linacName) throws IOException, ParseException, SQLException {
        return getCavityData(timestamp, linacName, null);
    }


    /**
     * Get data for all cavities for a given date with an optional filter on linac.
     * @param timestamp  The date to query data for
     * @param linacName  Only return data for cavities that are in the specified linac.  All cavities if null.
     * @param cavityType  Only return data for cavities that are of the specified type.
     * @return A set of cavity responses with data for the requested date
     * @throws IOException
     * @throws ParseException
     * @throws SQLException
     */
    public Set<CavityResponse> getCavityData(Date timestamp, LinacName linacName, CavityType cavityType) throws IOException, ParseException, SQLException {

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

        // CavityType became a property in Oct 2018
        String cavityQuery = "?t=CryoCavity&p=PhaseRMS,Q0,Qexternal,MaxGSET,OpsGsetMax,TripOffset,TripSlope,Length,"
                + "Bypassed,TunerBad,EPICSName,ModAnode,Housed_by&out=json&ced=history&wrkspc=" + wrkspc;
        if (timestamp.after(sdf.parse(CAVITY_TYPE_INTRODUCED))) {
            cavityQuery = "?t=CryoCavity&p=PhaseRMS,Q0,Qexternal,MaxGSET,OpsGsetMax,TripOffset,TripSlope,Length,"
                    + "Bypassed,TunerBad,EPICSName,ModAnode,Housed_by,CavityType&out=json&ced=history&wrkspc=" + wrkspc;
        }

        // Check the cache.  If not there, run the query, build the results, check that somebody else hasn't already inserted this
        // into the cache, then add the query result to the cache.
        Set<CavityDataPoint> data = null;
        try {
            data = readCache(timestamp);
        } catch (SQLException | ParseException ex) {
            LOGGER.log(Level.WARNING, "Error reading from cavity cache", ex);
        }

        // If cache miss
        if (data == null) {
            LOGGER.log(Level.INFO, "Fetching cavity data for " + timestamp + ".");
            Map<String, CryomoduleType> cmTypes = new CryomoduleService().getCryoModuleTypes(timestamp);
            Map<String, CavityType> cavTypes = new HashMap<>();
            data = new HashSet<>();

            //LOGGER.log(Level.FINEST, "CED Query: {0}", CED_INVENTORY_URL + cavityQuery);
            URL url = new URL(CED_INVENTORY_URL + cavityQuery);
            InputStream in = url.openStream();
            try (JsonReader reader = Json.createReader(in)) {
                JsonArray elements = CEDUtils.processCEDResponse(reader);
                CryomoduleType cmType;
                CavityType cavType = CavityType.None;

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
                    // At some point we introduced an actual CavityType parameter instead of inferring from the ModuleType.
                    cmType = cmTypes.get(zoneName);
                    if (timestamp.before(sdf.parse(CAVITY_TYPE_INTRODUCED))) {
                        if (cmType.equals(CryomoduleType.C25) ||
                                cmType.equals(CryomoduleType.C50) ||
                                cmType.equals(CryomoduleType.C50T)) {
                            names6GEV.add(cavityName);
                            // Weird historical problem, the QTR used to be LLRF 1.0 (6GeV), but they never archived the PV.
                            // But the QTR was swapped out to 3.0 controls so the KMAS PV was archived at some point.  Ignoring
                            // the old style just means that before the upgrade we will get null / unknown for this cavity which
                            // is correct.  I'll leave this little comment block here in case someone comes across this in the
                            // future.
                            // } else if (cmTypes.get(zoneName).equals(CryomoduleType.QTR)
                            //        && timestamp.before(DateUtil.parseDateStringYMD(QTR_UPGRADE_TO_LLRF3_0_DATE))) {
                            //     names6GEV.add(cavityName);
                        } else {
                            names12GEV.add(cavityName);
                        }
                    } else {
                        cavType = CavityType.valueOf(properties.getString("CavityType"));
                        if (cavType.equals(CavityType.C25) ||
                            cavType.equals(CavityType.C50)) {
                            names6GEV.add(cavityName);
                        } else {
                            names12GEV.add(cavityName);
                        }
                    }
                    cavTypes.put(cavityName, cavType);
                }
                MyaService ms = new MyaService();

                // We switched from CED to EPICS/MYA for tracking ModAnodeVolts in 2022.
                boolean useMyaMAV = timestamp.after(DateUtil.parseDateString(MAV_MOVED_TO_MYA_DATE));

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
                Map<String, Double> gsets = ms.getCavityMyaData(timestamp, name2Epics, "GSET");
                if (gsets == null) {
                    throw new RuntimeException("MyaService returned null.  Likely requesting data from future date.");
                }


                Map<String, Double> mavsEPICS = null;
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
                    // CED units: kilovolts
                    boolean tunerBad = false; // CED note: false unless set
                    boolean bypassed = false; // CED note: false unless set
                    Double tripOffset = null;   // CED units: trips per shift
                    Double tripSlope = null;   // CED units: trips per shift
                    Double opsGsetMax = null;  // CED units: MeV/m

                    // Required in CED
                    String q0;
                    String qExternal;
                    Double maxGset; // CED units: MeV/m
                    Double length; // CED units: meters

                    // ModAnode is CED property before 2022, no prop means MAV == 0. After starting 2022, it's moved
                    // to MYA/EPICS and has some slightly more complicated processing rules.
                    Double mav;

                    String cavityName = element.getString("name");
                    cmType = cmTypes.get(cavityName.substring(0, 4));
                    cavType = cavTypes.get(cavityName);

                    JsonObject properties = element.getJsonObject("properties");

                    // Check for the existence of CED optional parameters
                    if (properties.containsKey("TunerBad")) {
                        tunerBad = true;
                    }
                    if (properties.containsKey("Bypassed")) {
                        bypassed = true;
                    }
                    if (properties.containsKey("TripOffset")) {
                        tripOffset = Double.valueOf(properties.getString("TripOffset"));
                    }
                    if (properties.containsKey("TripSlope")) {
                        tripSlope = Double.valueOf(properties.getString("TripSlope"));
                    }
                    if (mavsEPICS != null) {
                        // Getting from MYA.  There are some weird cases in mya data that R...KMAS is huge, but the
                        // control system tries to limit KMAS to less than 2.  Maybe it's possible for this to be set
                        // a little more than 2.0 kV, but it's very unlikely that it gets set much higher.  3.0 kV is a
                        // reasonable cutoff as a sanity check that something went wrong with the data reporting.  If
                        // > 3 kV, assume it's a software bug and that no mod anode voltage is present per discussion
                        // with K. Hesse and C. Mounts.
                        if (mavsEPICS.containsKey(cavityName)) {
                            if (mavsEPICS.get(cavityName) == null) {
                                mav = null;
                            } else if (mavsEPICS.get(cavityName) > 3.0) {
                                mav = 0.0;
                            } else {
                                mav = mavsEPICS.get(cavityName);
                            }
                        } else {
                            // If for some
                            mav = null;
                        }
                    } else {
                        // Getting from CED.  no CED property means zero mod anode voltage is present.  Otherwise, use
                        // the value in CED.  Not possible to have a null/unknown value in this case.
                        if (properties.containsKey("ModAnode")) {
                            mav = Double.valueOf(properties.getString("ModAnode"));
                        } else {
                            mav = 0.0;
                        }
                    }
                    if (properties.containsKey("OpsGsetMax")) {
                        opsGsetMax = Double.valueOf(properties.getString("OpsGsetMax"));
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
                    maxGset = Double.valueOf(properties.getString("MaxGSET"));
                    length = Double.valueOf(properties.getString("Length"));

                    Double odvh = maxGset;
                    // The ops drive high (odvh) is set to the opsGsetMax if it exists or GsetMax otherwise
                    if (opsGsetMax != null) {
                        odvh = opsGsetMax;
                    }

                    //  useMAH should cover the case that cgds is null
                    if (useMAH) {
                        data.add(new CavityDataPoint(timestamp, cavityName, cavType, cmType, mav, epicsName,
                                gsets.get(cavityName), odvh, q0, qExternal, maxGset, opsGsetMax, tripOffset,
                                tripSlope, length, cgds.get(epicsName), bypassed, tunerBad));
                    } else {
                        data.add(new CavityDataPoint(timestamp, cavityName, cavType, cmType, mav, epicsName,
                                gsets.get(cavityName), odvh, q0, qExternal, maxGset, opsGsetMax, tripOffset,
                                tripSlope, length, null, bypassed, tunerBad));
                    }
                }
            }

            try {
                writeCache(timestamp, data);
            } catch (SQLException | IllegalArgumentException ex) {
                LOGGER.log(Level.WARNING, "Error writing to cavity cache", ex);
            }
        }

        // Filter down to only the requested linac if one was specified.
        if (linacName != null) {
            data.removeIf(d -> !d.getLinacName().equals(linacName));
        }
        // Filter down to only the requested cavity type if that filter was specified
        if (cavityType != null) {
            data.removeIf(d -> !d.getCavityType().equals(cavityType));
        }

        return this.createCommentResponseSet(data, comments);
    }

    /**
     * Convenience function for returning data as a map keyed on cavity name instead of a set.  Return all cavities.
     *
     * @param timestamp Timestamp to look up data for
     * @return A Map of cavity names to cavity response objects
     * @throws IOException    On non-ok CED server response
     * @throws ParseException On error parsing JSON responses
     * @throws SQLException   On database error
     */
    public Map<String, CavityResponse> getCavityDataMap(Date timestamp) throws IOException, ParseException, SQLException {
        return getCavityDataMap(timestamp, null);
    }


    /**
     * Convenience function for returning data as a map keyed on cavity name instead of a set with linac filter.
     *
     * @param timestamp Timestamp to look up data for
     * @param linacName Only return cavities from this linac.  Return all if null
     * @return A Map of cavity names to cavity response objects
     * @throws IOException    On non-ok CED server response
     * @throws ParseException On error parsing JSON responses
     * @throws SQLException   On database error
     */
    public Map<String, CavityResponse> getCavityDataMap(Date timestamp, LinacName linacName) throws IOException, ParseException, SQLException {
        Set<CavityResponse> crs = getCavityData(timestamp, linacName);

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
    public Set<CavityResponse> createCommentResponseSet(Set<CavityDataPoint> cavities, Map<String, SortedSet<Comment>> comments) {
        Set<CavityResponse> cr = new HashSet<>();
        if (cavities == null) {
            return null;
        }
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

        List<Date> dates = new ArrayList<>();
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
            dates.add(curr);
            cal.add(timeInt, 1);
            curr = cal.getTime();
        }

        Map<Date, Set<CavityDataPoint>> cacheData = this.readCache(dates);
        if (cacheData != null) {
            CommentService cs = new CommentService();
            for (Date d : cacheData.keySet()) {
                CommentFilter filter = new CommentFilter(null, null, null, null, DateUtil.getEndOfDay(d));
                Map<String, SortedSet<Comment>> comments = cs.getCommentsByTopic(filter);

                span.put(d, this.createCommentResponseSet(cacheData.get(d), comments));
            }
            dates.removeAll(cacheData.keySet());
        }

        // Randomize the order of the dates that are queried.  Consider multiple overlapping requests looking for
        // uncached data.  If they all go in order, then cache is never helpful and each query fetches the uncached
        // data.  Shuffling the order of requests is an easy way to minimize the chance of queries hitting the same
        // dates at the same time.
        Collections.shuffle(dates);
        for (Date date : dates) {
            span.put(date, this.getCavityData(date));
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
