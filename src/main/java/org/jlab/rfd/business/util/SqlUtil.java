/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.jlab.rfd.business.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

/**
 *
 * @author adamc
 */
public final class SqlUtil {

    private static final Logger LOGGER = Logger.getLogger(SqlUtil.class.getName());

    private static DataSource source;

    private SqlUtil() {
        // not public
    }

    static {
        try {
            source = (DataSource) new InitialContext().lookup("jdbc/rfgradteam_rw");
        } catch (NamingException e) {
            LOGGER.log(Level.WARNING, "JDBC resource lookup failed", e);
            throw new ExceptionInInitializerError(e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return source.getConnection();
    }

    public static void close(AutoCloseable... resources) {
        if (resources != null) {
            for (AutoCloseable resource : resources) {
                try {
                    resource.close();
                } catch (Exception e) {
                    LOGGER.log(Level.WARNING, "Unable to close resource", e);
                }
            }
        }
    }
}