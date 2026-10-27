package com.littlehorizons.erp.util;

import java.math.BigDecimal;
import java.util.List;

/** Console rendering helpers: banners, menus, tables and status messages. */
public final class ConsoleUI {

    private static final int BOX_WIDTH = 46;
    private static final int MAX_CELL = 38;

    private ConsoleUI() {
    }

    public static void banner(String... lines) {
        StringBuilder top = new StringBuilder("╔");
        StringBuilder bottom = new StringBuilder("╚");
        for (int i = 0; i < BOX_WIDTH; i++) {
            top.append('═');
            bottom.append('═');
        }
        System.out.println();
        System.out.println(top.append('╗'));
        for (String line : lines) {
            System.out.println("║   " + pad(line, BOX_WIDTH - 3) + "║");
        }
        System.out.println(bottom.append('╝'));
    }

    public static void menu(String title, String... options) {
        banner(title);
        for (int i = 0; i < options.length; i++) {
            System.out.println((i + 1) + ". " + options[i]);
        }
        System.out.println("0. Volver");
    }

    public static void section(String title) {
        System.out.println();
        System.out.println("--- " + title + " ---");
    }

    public static void success(String message) {
        System.out.println("[OK] " + message);
    }

    public static void error(String message) {
        System.out.println("[ERROR] " + message);
    }

    public static void info(String message) {
        System.out.println("[INFO] " + message);
    }

    public static void line(String message) {
        System.out.println(message);
    }

    public static void table(TableData data) {
        List<String> headers = data.getHeaders();
        int columns = headers.size();
        int[] widths = new int[columns];
        for (int c = 0; c < columns; c++) {
            widths[c] = headers.get(c).length();
        }
        for (Object[] row : data.getRows()) {
            for (int c = 0; c < columns; c++) {
                widths[c] = Math.max(widths[c], Math.min(MAX_CELL, format(row[c]).length()));
            }
        }
        StringBuilder header = new StringBuilder();
        StringBuilder separator = new StringBuilder();
        for (int c = 0; c < columns; c++) {
            header.append(pad(headers.get(c), widths[c])).append(" | ");
            separator.append(repeat('-', widths[c])).append("-+-");
        }
        System.out.println();
        System.out.println(header);
        System.out.println(separator);
        for (Object[] row : data.getRows()) {
            StringBuilder sb = new StringBuilder();
            for (int c = 0; c < columns; c++) {
                sb.append(pad(truncate(format(row[c])), widths[c])).append(" | ");
            }
            System.out.println(sb);
        }
        System.out.println("(" + data.size() + " registro" + (data.size() == 1 ? "" : "s") + ")");
    }

    private static String format(Object value) {
        if (value == null) {
            return "-";
        }
        if (value instanceof Boolean) {
            return ((Boolean) value) ? "Sí" : "No";
        }
        if (value instanceof BigDecimal) {
            return ((BigDecimal) value).toPlainString();
        }
        return value.toString();
    }

    private static String truncate(String text) {
        return text.length() <= MAX_CELL ? text : text.substring(0, MAX_CELL - 3) + "...";
    }

    private static String pad(String text, int width) {
        StringBuilder sb = new StringBuilder(text);
        while (sb.length() < width) {
            sb.append(' ');
        }
        return sb.toString();
    }

    private static String repeat(char ch, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(ch);
        }
        return sb.toString();
    }
}
