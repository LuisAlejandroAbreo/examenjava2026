package com.littlehorizons.erp.util;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Small JDBC helper (Template Method style). Every statement is a PreparedStatement and every
 * resource is closed with try-with-resources. DAOs only provide SQL and row mappers.
 */
public final class Jdbc {

    private Jdbc() {
    }

    @FunctionalInterface
    public interface RowMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }

    @FunctionalInterface
    public interface TransactionWork<T> {
        T execute(Connection connection) throws SQLException, ErpException;
    }

    private static Connection open() throws SQLException {
        return DatabaseConnection.getInstance().getConnection();
    }

    // ---------- Queries ----------

    public static <T> List<T> queryList(String sql, RowMapper<T> mapper, Object... params) throws ErpException {
        try (Connection connection = open()) {
            return queryList(connection, sql, mapper, params);
        } catch (SQLException e) {
            throw wrap(sql, e);
        }
    }

    public static <T> List<T> queryList(Connection connection, String sql, RowMapper<T> mapper, Object... params)
            throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                List<T> result = new ArrayList<>();
                while (rs.next()) {
                    result.add(mapper.map(rs));
                }
                return result;
            }
        }
    }

    public static <T> Optional<T> queryOne(String sql, RowMapper<T> mapper, Object... params) throws ErpException {
        List<T> list = queryList(sql, mapper, params);
        return list.isEmpty() ? Optional.<T>empty() : Optional.ofNullable(list.get(0));
    }

    public static <T> Optional<T> queryOne(Connection connection, String sql, RowMapper<T> mapper, Object... params)
            throws SQLException {
        List<T> list = queryList(connection, sql, mapper, params);
        return list.isEmpty() ? Optional.<T>empty() : Optional.ofNullable(list.get(0));
    }

    /** Returns the first column of the first row as long (0 when there is no row or the value is NULL). */
    public static long queryLong(String sql, Object... params) throws ErpException {
        return queryOne(sql, rs -> rs.getLong(1), params).orElse(0L);
    }

    public static long queryLong(Connection connection, String sql, Object... params) throws SQLException {
        return queryOne(connection, sql, rs -> rs.getLong(1), params).orElse(0L);
    }

    public static TableData queryTable(String sql, Object... params) throws ErpException {
        try (Connection connection = open();
                PreparedStatement ps = connection.prepareStatement(sql)) {
            bind(ps, params);
            try (ResultSet rs = ps.executeQuery()) {
                ResultSetMetaData meta = rs.getMetaData();
                int columns = meta.getColumnCount();
                TableData table = new TableData();
                for (int i = 1; i <= columns; i++) {
                    table.getHeaders().add(meta.getColumnLabel(i));
                }
                while (rs.next()) {
                    Object[] row = new Object[columns];
                    for (int i = 1; i <= columns; i++) {
                        row[i - 1] = rs.getObject(i);
                    }
                    table.getRows().add(row);
                }
                return table;
            }
        } catch (SQLException e) {
            throw wrap(sql, e);
        }
    }

    // ---------- Updates ----------

    /** Executes an INSERT and returns the generated key. */
    public static long insert(String sql, Object... params) throws ErpException {
        try (Connection connection = open()) {
            return insert(connection, sql, params);
        } catch (SQLException e) {
            throw wrap(sql, e);
        }
    }

    public static long insert(Connection connection, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            bind(ps, params);
            ps.executeUpdate();
            try (ResultSet keys = ps.getGeneratedKeys()) {
                return keys.next() ? keys.getLong(1) : 0L;
            }
        }
    }

    public static int update(String sql, Object... params) throws ErpException {
        try (Connection connection = open()) {
            return update(connection, sql, params);
        } catch (SQLException e) {
            throw wrap(sql, e);
        }
    }

    public static int update(Connection connection, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(sql)) {
            bind(ps, params);
            return ps.executeUpdate();
        }
    }

    // ---------- Transactions ----------

    public static <T> T inTransaction(TransactionWork<T> work) throws ErpException {
        try (Connection connection = open()) {
            connection.setAutoCommit(false);
            try {
                T result = work.execute(connection);
                connection.commit();
                return result;
            } catch (ErpException e) {
                rollbackQuietly(connection);
                throw e;
            } catch (SQLException e) {
                rollbackQuietly(connection);
                throw wrap("transaction", e);
            } catch (RuntimeException e) {
                rollbackQuietly(connection);
                throw e;
            }
        } catch (SQLException e) {
            throw wrap("transaction", e);
        }
    }

    private static void rollbackQuietly(Connection connection) {
        try {
            connection.rollback();
        } catch (SQLException e) {
            AppLogger.error("Rollback failed", e);
        }
    }

    // ---------- Helpers ----------

    private static void bind(PreparedStatement ps, Object... params) throws SQLException {
        if (params == null) {
            return;
        }
        for (int i = 0; i < params.length; i++) {
            Object value = params[i];
            int index = i + 1;
            if (value instanceof LocalDate) {
                ps.setDate(index, Date.valueOf((LocalDate) value));
            } else if (value instanceof LocalTime) {
                ps.setTime(index, Time.valueOf((LocalTime) value));
            } else if (value instanceof LocalDateTime) {
                ps.setTimestamp(index, Timestamp.valueOf((LocalDateTime) value));
            } else {
                ps.setObject(index, value);
            }
        }
    }

    public static LocalDate toLocalDate(ResultSet rs, String column) throws SQLException {
        Date date = rs.getDate(column);
        return date == null ? null : date.toLocalDate();
    }

    public static Integer getInteger(ResultSet rs, String column) throws SQLException {
        int value = rs.getInt(column);
        return rs.wasNull() ? null : value;
    }

    private static ErpException wrap(String sql, SQLException e) {
        AppLogger.error("SQL error [" + e.getSQLState() + "/" + e.getErrorCode() + "] in: " + sql, e);
        String state = e.getSQLState();
        String message;
        if (state != null && state.startsWith("23")) {
            message = "La operación viola una restricción de integridad (dato duplicado o referencia inexistente).";
        } else if (state != null && (state.startsWith("08") || state.startsWith("28") || state.startsWith("3D")
                || state.startsWith("42000"))) {
            message = "No se pudo acceder a la base de datos. Revise db.properties y que MySQL esté en ejecución.";
        } else {
            message = "Error de base de datos: " + e.getMessage();
        }
        return new ErpException(message, e);
    }
}
