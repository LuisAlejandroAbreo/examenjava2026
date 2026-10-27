package com.littlehorizons.erp.util;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/** Static school data printed on invoices (simulated DIAN data). */
public final class SchoolInfo {

    public static final String LEGAL_NAME = "Colegio Internacional Little Horizons S.A.S.";
    public static final String NIT = "901.234.567-8";
    public static final String ADDRESS = "Carrera 7 # 45-10, Bogotá D.C., Colombia";
    public static final String PHONE = "(601) 555 0100";
    public static final String EMAIL = "facturacion@littlehorizons.edu.co";
    public static final String INVOICE_PREFIX = "LHFE";
    public static final String DIAN_RESOLUTION = "Resolución DIAN No. 18760000001 del 15/01/2026 (simulada)";
    public static final String DIAN_RANGE = "Prefijo LHFE, rango 000001 a 100000, vigencia 24 meses";
    public static final BigDecimal VAT_RATE = new BigDecimal("0.19");
    /** Concept codes that carry VAT; educational services are excluded from VAT in Colombia. */
    public static final Set<String> VAT_CONCEPT_CODES =
            Collections.unmodifiableSet(new HashSet<>(Arrays.asList("UNIFORME")));
    public static final String INVOICE_DIRECTORY = "facturas";

    private SchoolInfo() {
    }
}
