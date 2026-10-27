package com.littlehorizons.erp.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** SHA-based hashing helpers used to build the simulated CUFE and QR pattern. */
public final class HashUtil {

    private HashUtil() {
    }

    public static byte[] digest(String algorithm, String text) {
        try {
            return MessageDigest.getInstance(algorithm).digest(text.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo no disponible: " + algorithm, e);
        }
    }

    public static String hex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    /** Simulated CUFE: SHA-384 (96 hex chars) of invoice number + NIT + issue date + total. */
    public static String cufe(String invoiceNumber, String nit, String issueDate, String total) {
        return hex(digest("SHA-384", invoiceNumber + nit + issueDate + total));
    }
}
