package com.littlehorizons.erp.util;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;

/** Formats Colombian pesos, e.g. $ 350.000,00 */
public final class Money {

    private Money() {
    }

    public static String format(BigDecimal amount) {
        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.forLanguageTag("es-CO"));
        symbols.setGroupingSeparator('.');
        symbols.setDecimalSeparator(',');
        return "$ " + new DecimalFormat("#,##0.00", symbols).format(amount == null ? BigDecimal.ZERO : amount);
    }
}
