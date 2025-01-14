package org.jlab.rfd.business.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonObject;
import javax.json.JsonReader;
import org.jlab.rfd.business.util.DateUtil;
import org.jlab.rfd.config.AppConfig;
import org.jlab.rfd.model.CEDElementUpdate;
import org.jlab.rfd.model.CEDElementUpdateHistory;

/**
 *
 * @author adamc
 */
public class CEDUpdateHistoryService {

    private static final Logger LOGGER = Logger.getLogger(CEDUpdateHistoryService.class.getName());
    private static final String CED_UPDATE_HISTORY_URL = AppConfig.getAppConfig().getCEDUrl() + "/ajax/history/";

    // Thread safe method called from public method getUpdateLIst
    public CEDElementUpdateHistory getElementUpdateHistory(String elem, List<String> props, Date start, Date end) throws IOException, ParseException {
        CEDElementUpdateHistory updateHistory = new CEDElementUpdateHistory(elem);
        for (String prop : props) {
            String query = CED_UPDATE_HISTORY_URL + elem + "/" + prop + "?out=json";
            URL url = new URL(query);
            InputStream in;
            try {
                in = url.openStream();
            } catch (FileNotFoundException e) {
                LOGGER.log(Level.WARNING, "Error querying CED data.  Likely an invalid element/property pair.", e);
                throw new RuntimeException("Error querying CED data.  Likely an invalid element/property pair");
            }
            try (JsonReader reader = Json.createReader(in)) {
                JsonObject json = reader.readObject();
                String status = json.getString("stat");
                if (!"ok".equals(status)) {
                    throw new IOException("unable to update history from CED.  stat: " + status);
                }
                JsonArray history = json.getJsonObject("response").getJsonArray("history");
                for (int i = 0; i < history.size(); i++) {
                    JsonObject update = history.getJsonObject(i);

                    String dateString = update.getString("date");
                    Date date = DateUtil.parseDateStringYMDHM(dateString);
                    String value = update.getString("value");
                    String username = update.getString("username");
                    String comment = update.getString("comment");
                    int index = update.getInt("index");
                    int index1 = update.getInt("index1");
                    int index2 = update.getInt("index2");
                    if ((start == null || date.after(start)) && (end == null || date.before(end))) {
                        updateHistory.addUpdate(new CEDElementUpdate(elem, prop, dateString, value, username, comment, index, index1, index2),
                                date, prop);
                    }
                }
            }
        }

        return updateHistory;
    }

    public CEDElementUpdateHistory getElementUpdateHistory(String elem, List<String> props) throws IOException, ParseException {
        return getElementUpdateHistory(elem, props, null, null);
    }

    private Callable<CEDElementUpdateHistory> callable(String elem, List<String> props, Date start, Date end) {
        // Create a local copy since this will almost certainly be used concurrently and someone could in theory change props
        final List<String> p = new ArrayList<>(props);
        final String el = elem;
        final Date s = start;
        final Date e = end;
        return new Callable<CEDElementUpdateHistory>() {
            @Override
            public CEDElementUpdateHistory call() throws IOException, ParseException {
                return getElementUpdateHistory(el, p, s, e);
            }
        };
    }

    // This returns a the list of CED element property updates for a set of elements during a time range.
    // Function for requesting a large set of elements and properties.  All props must be defined for all elems and all elems
    // must be defined or CED will throw an exception.  Runs multiple requests in parallel.
    public List<CEDElementUpdate> getUpdateList(List<String> elems, List<String> props, Date start, Date end) throws InterruptedException, ExecutionException {
        List<Callable<CEDElementUpdateHistory>> callables = new ArrayList<>();

        if (elems == null || elems.isEmpty()) {
            return new ArrayList<>();
        } else if (props == null || props.isEmpty()) {
            return new ArrayList<>();
        }
        for (String elem : elems) {
            callables.add(callable(elem, props, start, end));
        }
        ExecutorService exec = Executors.newFixedThreadPool(10);

        List<Future<CEDElementUpdateHistory>> futures = exec.invokeAll(callables);
        List<CEDElementUpdate> updates = new ArrayList<>();
        for (Future<CEDElementUpdateHistory> f : futures) {
            CEDElementUpdateHistory hist = f.get();
            for (String prop : props) {
                Map<Date, CEDElementUpdate> pUpdates = hist.getUpdateHistory(prop);
                if (pUpdates != null) {
                    for (Date date : pUpdates.keySet()) {
                        updates.add(pUpdates.get(date));
                    }
                }
            }
        }
        Collections.sort(updates, new Comparator<>() {
            @Override
            public int compare(CEDElementUpdate c1, CEDElementUpdate c2) {
                return c2.getDateString().compareTo(c1.getDateString());
            }
        });
        try {
            exec.shutdown();
            exec.awaitTermination(20, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Error querying CED data.");
            throw e;
        } finally {
            if (!exec.isTerminated()) {
                exec.shutdownNow();
                LOGGER.log(Level.SEVERE, "Executor service timed out before all request threads had completed");
                throw new InterruptedException("CED data queries timed out.");
            }
        }
        return updates;
    }

}
