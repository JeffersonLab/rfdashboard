package org.jlab.rfd.config;

import java.util.Map;

/**
 * Define a class for reading System environment variables as this will allow mocking in unit testing.
 */
public class Environment {
    /**
     * Not clear if System.getenv is thread-safe. No harm in making this synchronized since it should only be called
     * rarely
     * @return A map of key/value pairs of environment variables.
     */
    public synchronized Map<String, String> getenv() {
        return System.getenv();
    }
}
