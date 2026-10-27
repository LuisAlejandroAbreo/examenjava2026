package com.littlehorizons.erp.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Dependency-free single-page PDF writer (A4, Helvetica / Helvetica-Bold, WinAnsi encoding).
 * Supports text, lines and rectangles, which is all the invoice needs.
 */
public class SimplePdf {

    public static final double PAGE_WIDTH = 595;
    public static final double PAGE_HEIGHT = 842;

    private final StringBuilder content = new StringBuilder();

    public void text(double x, double y, double size, boolean bold, String text) {
        content.append("BT /").append(bold ? "F2" : "F1").append(' ').append(num(size)).append(" Tf ")
                .append(num(x)).append(' ').append(num(y)).append(" Td (").append(escape(text)).append(") Tj ET\n");
    }

    public void textRight(double xRight, double y, double size, boolean bold, String text) {
        text(xRight - width(text, size, bold), y, size, bold, text);
    }

    public void line(double x1, double y1, double x2, double y2, double lineWidth) {
        content.append(num(lineWidth)).append(" w ").append(num(x1)).append(' ').append(num(y1)).append(" m ")
                .append(num(x2)).append(' ').append(num(y2)).append(" l S\n");
    }

    public void rect(double x, double y, double w, double h, boolean fill, boolean stroke) {
        String op = fill && stroke ? "B" : (fill ? "f" : "S");
        content.append(num(x)).append(' ').append(num(y)).append(' ').append(num(w)).append(' ').append(num(h))
                .append(" re ").append(op).append('\n');
    }

    public void fillGray(double gray) {
        content.append(num(gray)).append(" g\n");
    }

    public void strokeGray(double gray) {
        content.append(num(gray)).append(" G\n");
    }

    /** Approximate Helvetica text width (exact for digits, spaces and punctuation used in amounts). */
    public static double width(String text, double size, boolean bold) {
        double total = 0;
        for (char c : text.toCharArray()) {
            double w;
            if (Character.isDigit(c) || c == '$') {
                w = 0.556;
            } else if (c == ' ' || c == '.' || c == ',' || c == ':' || c == ';' || c == '/' || c == '!') {
                w = 0.278;
            } else if (c == 'i' || c == 'l' || c == 'j' || c == '|' || c == '\'') {
                w = 0.222;
            } else if (c == 'm' || c == 'w' || c == 'M' || c == 'W') {
                w = 0.833;
            } else if (Character.isUpperCase(c)) {
                w = 0.68;
            } else {
                w = 0.52;
            }
            total += w;
        }
        return total * size * (bold ? 1.06 : 1.0);
    }

    public void save(Path file) throws IOException {
        Files.write(file, build());
    }

    private byte[] build() throws IOException {
        byte[] stream = content.toString().getBytes(StandardCharsets.ISO_8859_1);
        List<byte[]> objects = new ArrayList<>();
        objects.add(obj("<< /Type /Catalog /Pages 2 0 R >>"));
        objects.add(obj("<< /Type /Pages /Kids [3 0 R] /Count 1 >>"));
        objects.add(obj("<< /Type /Page /Parent 2 0 R /MediaBox [0 0 595 842] /Resources << /Font << /F1 5 0 R /F2 6 0 R >> >> /Contents 4 0 R >>"));
        ByteArrayOutputStream streamObj = new ByteArrayOutputStream();
        streamObj.write(("<< /Length " + stream.length + " >>\nstream\n").getBytes(StandardCharsets.ISO_8859_1));
        streamObj.write(stream);
        streamObj.write("\nendstream".getBytes(StandardCharsets.ISO_8859_1));
        objects.add(streamObj.toByteArray());
        objects.add(obj("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica /Encoding /WinAnsiEncoding >>"));
        objects.add(obj("<< /Type /Font /Subtype /Type1 /BaseFont /Helvetica-Bold /Encoding /WinAnsiEncoding >>"));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write("%PDF-1.4\n".getBytes(StandardCharsets.ISO_8859_1));
        int[] offsets = new int[objects.size()];
        for (int i = 0; i < objects.size(); i++) {
            offsets[i] = out.size();
            out.write(((i + 1) + " 0 obj\n").getBytes(StandardCharsets.ISO_8859_1));
            out.write(objects.get(i));
            out.write("\nendobj\n".getBytes(StandardCharsets.ISO_8859_1));
        }
        int xrefOffset = out.size();
        StringBuilder xref = new StringBuilder("xref\n0 " + (objects.size() + 1) + "\n");
        xref.append("0000000000 65535 f \n");
        for (int offset : offsets) {
            xref.append(String.format(Locale.ROOT, "%010d 00000 n \n", offset));
        }
        xref.append("trailer\n<< /Size ").append(objects.size() + 1).append(" /Root 1 0 R >>\nstartxref\n")
                .append(xrefOffset).append("\n%%EOF\n");
        out.write(xref.toString().getBytes(StandardCharsets.ISO_8859_1));
        return out.toByteArray();
    }

    private static byte[] obj(String body) {
        return body.getBytes(StandardCharsets.ISO_8859_1);
    }

    private static String num(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private static String escape(String text) {
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (c == '\\' || c == '(' || c == ')') {
                sb.append('\\').append(c);
            } else if (c < 32) {
                sb.append(' ');
            } else if (c > 255) {
                sb.append('?');
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }
}
