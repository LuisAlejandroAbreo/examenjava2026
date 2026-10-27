package com.littlehorizons.erp.util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton that holds the database configuration and hands out connections.
 * Configuration is read from db.properties (project root); sensible defaults are used otherwise.
 */
public final class DatabaseConnection {

    private static final String CONFIG_FILE = "db.properties";
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/campuslands"
            + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=America/Bogota&characterEncoding=UTF-8";
    private static final String DEFAULT_USER = "camper";
    private static final String DEFAULT_PASSWORD = "Campus2026";

    private final String url;
    private final String user;
    private final String password;

    private DatabaseConnection() {
        Properties props = new Properties();
        Path file = Paths.get(CONFIG_FILE);
        if (Files.exists(file)) {
            try (InputStream in = Files.newInputStream(file)) {
                props.load(in);
            } catch (IOException e) {
                AppLogger.error("No se pudo leer " + CONFIG_FILE + ", se usan valores por defecto", e);
            }
        } else {
            AppLogger.info(CONFIG_FILE + " no encontrado, se usan valores por defecto");
        }
        this.url = props.getProperty("db.url", DEFAULT_URL);
        this.user = props.getProperty("db.user", DEFAULT_USER);
        this.password = props.getProperty("db.password", DEFAULT_PASSWORD);
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            AppLogger.info("Driver com.mysql.cj.jdbc.Driver no encontrado; se intentara el registro automatico JDBC");
        }
    }

    private static final class Holder {
        private static final DatabaseConnection INSTANCE = new DatabaseConnection();
    }

    public static DatabaseConnection getInstance() {
        return Holder.INSTANCE;
    }

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(url, user, password);
    }

    /** Verifies that a connection can be opened. */
    public void testConnection() throws SQLException {
        try (Connection connection = getConnection()) {
            if (!connection.isValid(5)) {
                throw new SQLException("La conexion no es valida");
            }
        }
    }
}
