package com.littlehorizons.erp.util;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.regex.Pattern;

/** Validated console input. Every read method loops until the user types a valid value. */
public class InputReader {

    private static final Pattern EMAIL = Pattern.compile("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$");
    private static final Pattern PHONE = Pattern.compile("^\\+?[0-9]{7,15}$");
    private static final Pattern DOCUMENT = Pattern.compile("^[A-Za-z0-9]{5,20}$");

    private final Scanner scanner;

    public InputReader(Scanner scanner) {
        this.scanner = scanner;
    }

    public String readLine(String prompt) {
        System.out.print(prompt);
        try {
            return scanner.nextLine().trim();
        } catch (NoSuchElementException e) {
            throw new InputClosedException(e);
        }
    }

    public String readRequired(String prompt, int maxLength) {
        while (true) {
            String value = readLine(prompt);
            if (value.isEmpty()) {
                ConsoleUI.error("Este campo es obligatorio.");
            } else if (value.length() > maxLength) {
                ConsoleUI.error("Máximo " + maxLength + " caracteres.");
            } else {
                return value;
            }
        }
    }

    /** Returns null when the user leaves the field empty. */
    public String readOptional(String prompt, int maxLength) {
        while (true) {
            String value = readLine(prompt + " (opcional, Enter para omitir): ");
            if (value.isEmpty()) {
                return null;
            }
            if (value.length() > maxLength) {
                ConsoleUI.error("Máximo " + maxLength + " caracteres.");
            } else {
                return value;
            }
        }
    }

    public int readInt(String prompt, int min, int max) {
        while (true) {
            String value = readLine(prompt);
            try {
                int number = Integer.parseInt(value);
                if (number < min || number > max) {
                    ConsoleUI.error("Ingrese un número entre " + min + " y " + max + ".");
                } else {
                    return number;
                }
            } catch (NumberFormatException e) {
                ConsoleUI.error("Ingrese un número entero válido.");
            }
        }
    }

    /** Reads a positive id; 0 means cancel. */
    public int readId(String prompt) {
        return readInt(prompt + " (0 para cancelar): ", 0, Integer.MAX_VALUE);
    }

    public BigDecimal readMoney(String prompt) {
        while (true) {
            String value = readLine(prompt).replace(",", ".");
            try {
                BigDecimal amount = new BigDecimal(value);
                if (amount.signum() <= 0) {
                    ConsoleUI.error("El valor debe ser mayor que cero.");
                } else if (amount.scale() > 2 || amount.compareTo(new BigDecimal("99999999.99")) > 0) {
                    ConsoleUI.error("Use máximo 2 decimales y un valor menor a 100.000.000.");
                } else {
                    return amount.setScale(2);
                }
            } catch (NumberFormatException e) {
                ConsoleUI.error("Ingrese un valor numérico válido (ej: 350000).");
            }
        }
    }

    /** Colombian grade scale: 0.0 - 5.0 with one decimal. */
    public BigDecimal readGrade(String prompt) {
        while (true) {
            String value = readLine(prompt).replace(",", ".");
            try {
                BigDecimal grade = new BigDecimal(value);
                if (grade.scale() > 1) {
                    ConsoleUI.error("Use máximo un decimal (ej: 4.5).");
                } else if (grade.compareTo(BigDecimal.ZERO) < 0 || grade.compareTo(new BigDecimal("5.0")) > 0) {
                    ConsoleUI.error("La nota debe estar entre 0.0 y 5.0.");
                } else {
                    return grade.setScale(1);
                }
            } catch (NumberFormatException e) {
                ConsoleUI.error("Ingrese una nota válida (ej: 4.5).");
            }
        }
    }

    public LocalDate readDate(String prompt) {
        while (true) {
            String value = readLine(prompt + " [AAAA-MM-DD]: ");
            try {
                return LocalDate.parse(value);
            } catch (DateTimeParseException e) {
                ConsoleUI.error("Fecha inválida. Use el formato AAAA-MM-DD (ej: 2026-10-14).");
            }
        }
    }

    /** Date that defaults to today when the user presses Enter. */
    public LocalDate readDateOrToday(String prompt) {
        while (true) {
            String value = readLine(prompt + " [AAAA-MM-DD, Enter = hoy]: ");
            if (value.isEmpty()) {
                return LocalDate.now();
            }
            try {
                return LocalDate.parse(value);
            } catch (DateTimeParseException e) {
                ConsoleUI.error("Fecha inválida. Use el formato AAAA-MM-DD.");
            }
        }
    }

    public LocalDate readPastDate(String prompt) {
        while (true) {
            LocalDate date = readDate(prompt);
            if (date.isAfter(LocalDate.now())) {
                ConsoleUI.error("La fecha no puede ser futura.");
            } else {
                return date;
            }
        }
    }

    public LocalDate readFutureOrTodayDate(String prompt, LocalDate minimum) {
        while (true) {
            LocalDate date = readDate(prompt);
            if (date.isBefore(minimum)) {
                ConsoleUI.error("La fecha no puede ser anterior a " + minimum + ".");
            } else {
                return date;
            }
        }
    }

    public LocalTime readTime(String prompt) {
        while (true) {
            String value = readLine(prompt + " [HH:mm]: ");
            try {
                return LocalTime.parse(value);
            } catch (DateTimeParseException e) {
                ConsoleUI.error("Hora inválida. Use el formato HH:mm (ej: 07:30).");
            }
        }
    }

    public String readDocument(String prompt) {
        while (true) {
            String value = readLine(prompt);
            if (DOCUMENT.matcher(value).matches()) {
                return value;
            }
            ConsoleUI.error("Documento inválido: 5 a 20 letras o números, sin espacios ni puntos.");
        }
    }

    public String readPhone(String prompt, boolean required) {
        while (true) {
            String value = readLine(prompt + (required ? ": " : " (opcional, Enter para omitir): "));
            if (value.isEmpty() && !required) {
                return null;
            }
            String clean = value.replace(" ", "");
            if (PHONE.matcher(clean).matches()) {
                return clean;
            }
            ConsoleUI.error("Teléfono inválido: use de 7 a 15 dígitos (puede iniciar con +).");
        }
    }

    public String readEmail(String prompt, boolean required) {
        while (true) {
            String value = readLine(prompt + (required ? ": " : " (opcional, Enter para omitir): "));
            if (value.isEmpty() && !required) {
                return null;
            }
            if (EMAIL.matcher(value).matches() && value.length() <= 100) {
                return value;
            }
            ConsoleUI.error("Correo inválido (ej: nombre@dominio.com).");
        }
    }

    /** Shows numbered options and returns the chosen text. */
    public String readOption(String prompt, String... options) {
        System.out.println(prompt);
        for (int i = 0; i < options.length; i++) {
            System.out.println("  " + (i + 1) + ". " + options[i]);
        }
        int choice = readInt("Seleccione una opción: ", 1, options.length);
        return options[choice - 1];
    }

    public boolean confirm(String prompt) {
        while (true) {
            String value = readLine(prompt + " (s/n): ").toLowerCase();
            if (value.equals("s") || value.equals("si") || value.equals("sí")) {
                return true;
            }
            if (value.equals("n") || value.equals("no")) {
                return false;
            }
            ConsoleUI.error("Responda 's' o 'n'.");
        }
    }

    /** Money amount that defaults to {@code defaultValue} when the user presses Enter. */
    public BigDecimal readMoneyOrDefault(String prompt, BigDecimal defaultValue) {
        while (true) {
            String value = readLine(prompt + " [Enter = " + defaultValue.toPlainString() + "]: ").replace(",", ".");
            if (value.isEmpty()) {
                return defaultValue;
            }
            try {
                BigDecimal amount = new BigDecimal(value);
                if (amount.signum() <= 0) {
                    ConsoleUI.error("El valor debe ser mayor que cero.");
                } else if (amount.scale() > 2 || amount.compareTo(new BigDecimal("99999999.99")) > 0) {
                    ConsoleUI.error("Use máximo 2 decimales y un valor menor a 100.000.000.");
                } else {
                    return amount.setScale(2);
                }
            } catch (NumberFormatException e) {
                ConsoleUI.error("Ingrese un valor numérico válido.");
            }
        }
    }
}
