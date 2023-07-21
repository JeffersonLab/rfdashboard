package org.jlab.rfd.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class contains app-wide configuration information needed beyond the Servlet Context.  Configurations are built
 * up in the following order with values set in later steps overriding values set in earlier steps.
 * 1. Default config save in application code
 * 2. Configuration file, /config/rfdashboard.properties, found first in ClassPath.
 * 3. Environment variables.  Configuration options are the same as config file, but with "RFD_" prefix (e.g.,
 * RFD_CED_URL).
 * Class is a singleton as only configuration exists for the entire application.  Configuration can be regenerated
 * if requested when getting the application config.
 * Note: Warning suppressed as we synchronize on configInstance which is not 'final', but only one is ever created
 *       and synchronization happens after that has been created.
 */
@SuppressWarnings("SynchronizeOnNonFinalField")
public class AppConfig {
    private static final Logger LOGGER = Logger.getLogger(AppConfig.class.getName());
    private static final String DEFAULT_CONFIG_FILE = "/config/rfdashboard.properties";
    private static final List<String> CONFIG_OPTIONS = new ArrayList<>(List.of("CED_URL", "MYQUERY_URL"));
    private final Properties config;
    private static AppConfig configInstance = null;

    private AppConfig(InputStream is, Environment env) {
        // Factored out so that reloading uses the case method.
        this.config = new Properties();
        generateConfig(this.config, is, env);
    }

    private static void generateConfig(Properties config, InputStream is, Environment env) {
        if (env == null) {
            env = new Environment();
        }
        updateFromDefaultConfig(config);
        updateConfigFromStream(config, is);
        updateConfigFromEnvironment(config, env);
    }

    /**
     * This creates a Properties object the default config.
     */
    private static void updateFromDefaultConfig(Properties config) {
        config.put("CED_URL", "https://ced.acc.jlab.org");
        config.put("MYQUERY_URL", "https://epicsweb.jlab.org");
    }

    /**
     * Updates the given config in place
     *
     * @param config Object for holding the config information from the input stream
     * @param is     An input stream that can be loaded by a Properties object
     */
    private static void updateConfigFromStream(Properties config, InputStream is) {
        if (is == null) {
            try (InputStream stream = AppConfig.class.getResourceAsStream(DEFAULT_CONFIG_FILE)) {
                config.load(stream);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Error reading config file.  Using default.");
            }
        } else {
            try {
                config.load(is);
            } catch (IOException ex) {
                LOGGER.log(Level.WARNING, "Error reading config stream.  Using default.");
            }
        }
    }

    private static void updateConfigFromEnvironment(Properties config, Environment environment) {
        Map<String, String> env = environment.getenv();
        for (String option : CONFIG_OPTIONS) {
            if (env.containsKey("RFD_" + option)) {
                config.put(option, env.get("RFD_" + option));
            }
        }
    }

    /**
     * Not synchronized as it is a wrapper on a synchronized method
     *
     * @return The application's configuration.
     */
    public static AppConfig getAppConfig() {
        return getAppConfig(null, null, false);
    }

    public static synchronized AppConfig getAppConfig(InputStream is, Environment env, boolean reload) {
        // Note: The input stream is can only be read from once without worrying about mark/reset.
        if (configInstance == null) {
            configInstance = new AppConfig(is, env);
        } else if (reload) {
            // This needs to be synchronized in case someone has a reference to AppConfig and is trying to read from it
            // while the configuration is being reloaded.  configInstance is not final because it starts as null, but
            // only one is every created and has been created by the time the code reaches here.
            synchronized (configInstance) {
                configInstance.config.clear();
                generateConfig(configInstance.config, is, env);
            }
        }
        return configInstance;
    }

    public String getCEDUrl() {
        // configInstance starts as null, but only one is ever created.  Can't call this method until it exists.
        synchronized (configInstance) {
            return this.config.getProperty("CED_URL");
        }
    }

    public String getMyqueryUrl() {
        // configInstance starts as null, but only one is ever created.  Can't call this method until it exists.
        synchronized (configInstance) {
            return this.config.getProperty("MYQUERY_URL");
        }
    }

}
