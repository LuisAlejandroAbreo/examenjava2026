package com.littlehorizons.erp.util;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/** Minimal file logger. Errors and key events are appended to error_log.txt. */
public final class AppLogger {

    private static final Path LOG_FILE = Paths.get("error_log.txt");
    private static final DateTimeFormatter TIMESTAMP = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private AppLogger() {
    }

    public static void info(String message) {
        write("INFO", message, null);
    }

    public static void error(String message, Throwable throwable) {
        write("ERROR", message, throwable);
    }

    public static Path getLogFile() {
        return LOG_FILE.toAbsolutePath();
    }

    private static synchronized void write(String level, String message, Throwable throwable) {
        try (BufferedWriter writer = Files.newBufferedWriter(LOG_FILE, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.APPEND);
                PrintWriter out = new PrintWriter(writer)) {
            out.println(LocalDateTime.now().format(TIMESTAMP) + " [" + level + "] " + message);
            if (throwable != null) {
                throwable.printStackTrace(out);
            }
        } catch (IOException e) {
            System.err.println("No se pudo escribir en el log: " + e.getMessage());
        }
    }
}
