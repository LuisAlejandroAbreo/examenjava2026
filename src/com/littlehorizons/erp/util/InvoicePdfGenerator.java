package com.littlehorizons.erp.util;

import com.littlehorizons.erp.model.InvoiceDetail;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;

/** Builds the simulated DIAN electronic invoice as a PDF file. */
public class InvoicePdfGenerator {

    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final int QR_MODULES = 25;
    private static final double QR_MODULE_SIZE = 4;

    /** Writes the invoice and returns the path of the generated PDF. */
    public Path generate(InvoiceDetail d) throws IOException {
        Path directory = Paths.get(SchoolInfo.INVOICE_DIRECTORY);
        Files.createDirectories(directory);
        String numeric = d.getInvoiceNumber().replaceAll("\\D", "");
        Path file = directory.resolve("factura_" + numeric + ".pdf");

        BigDecimal total = d.getAmountPaid();
        BigDecimal vat = BigDecimal.ZERO;
        BigDecimal subtotal = total;
        boolean hasVat = SchoolInfo.VAT_CONCEPT_CODES.contains(d.getConceptCode());
        if (hasVat) {
            subtotal = total.divide(BigDecimal.ONE.add(SchoolInfo.VAT_RATE), 2, RoundingMode.HALF_UP);
            vat = total.subtract(subtotal);
        }
        BigDecimal unit = subtotal.divide(new BigDecimal(d.getQuantity()), 2, RoundingMode.HALF_UP);

        SimplePdf pdf = new SimplePdf();
        // Header
        pdf.fillGray(0.15);
        pdf.rect(0, 770, SimplePdf.PAGE_WIDTH, 72, true, false);
        pdf.fillGray(1);
        pdf.text(40, 812, 16, true, SchoolInfo.LEGAL_NAME);
        pdf.text(40, 796, 9, false, "NIT " + SchoolInfo.NIT + "  |  " + SchoolInfo.ADDRESS);
        pdf.text(40, 783, 9, false, "Tel. " + SchoolInfo.PHONE + "  |  " + SchoolInfo.EMAIL);
        pdf.fillGray(0);
        pdf.strokeGray(0);

        // Invoice identification
        pdf.text(40, 740, 14, true, "FACTURA ELECTRÓNICA DE VENTA");
        pdf.textRight(555, 740, 14, true, "No. " + d.getInvoiceNumber());
        pdf.text(40, 722, 10, false, "Fecha de emisión: " + d.getIssueDate().format(DATE));
        pdf.textRight(555, 722, 10, false, "Fecha de pago: " + d.getPaymentDate().format(DATE));
        pdf.text(40, 708, 8, false, SchoolInfo.DIAN_RESOLUTION + ". " + SchoolInfo.DIAN_RANGE + ".");
        pdf.line(40, 700, 555, 700, 0.8);

        // Customer
        pdf.text(40, 682, 11, true, "Datos del cliente (acudiente responsable)");
        pdf.text(40, 664, 10, false, "Nombre: " + d.getGuardianName());
        pdf.text(40, 650, 10, false, "Documento: " + d.getGuardianDocumentType() + " " + d.getGuardianDocument());
        pdf.text(40, 636, 10, false, "Dirección: " + nullToDash(d.getGuardianAddress()));
        pdf.text(40, 622, 10, false, "Correo: " + nullToDash(d.getGuardianEmail()));
        pdf.text(40, 608, 10, false, "Estudiante: " + d.getStudentName());

        // Detail table
        double top = 580;
        pdf.fillGray(0.88);
        pdf.rect(40, top - 4, 515, 20, true, false);
        pdf.fillGray(0);
        pdf.text(46, top + 2, 10, true, "Descripción");
        pdf.textRight(360, top + 2, 10, true, "Cant.");
        pdf.textRight(455, top + 2, 10, true, "Valor unitario");
        pdf.textRight(549, top + 2, 10, true, "Subtotal");
        double rowY = top - 20;
        pdf.text(46, rowY, 10, false, d.getConceptName());
        pdf.textRight(360, rowY, 10, false, String.valueOf(d.getQuantity()));
        pdf.textRight(455, rowY, 10, false, Money.format(unit));
        pdf.textRight(549, rowY, 10, false, Money.format(subtotal));
        pdf.line(40, rowY - 10, 555, rowY - 10, 0.5);

        // Totals
        double ty = rowY - 34;
        pdf.textRight(455, ty, 10, false, "Subtotal:");
        pdf.textRight(549, ty, 10, false, Money.format(subtotal));
        pdf.textRight(455, ty - 16, 10, false, hasVat ? "IVA (19%):" : "IVA (excluido):");
        pdf.textRight(549, ty - 16, 10, false, Money.format(vat));
        pdf.line(380, ty - 24, 555, ty - 24, 0.5);
        pdf.textRight(455, ty - 40, 12, true, "TOTAL:");
        pdf.textRight(549, ty - 40, 12, true, Money.format(total));

        // Payment info
        double py = ty - 74;
        pdf.text(40, py, 10, true, "Información del pago");
        pdf.text(40, py - 16, 10, false, "Método de pago: " + d.getPaymentMethod());
        pdf.text(40, py - 30, 10, false, "Recibo de caja: " + d.getReceiptNumber());

        // CUFE + QR
        double qrBottom = 100;
        pdf.line(40, 310, 555, 310, 0.8);
        pdf.text(40, 292, 10, true, "Elementos de cumplimiento DIAN (simulados)");
        pdf.text(40, 274, 9, true, "CUFE:");
        String cufe = d.getCufe();
        pdf.text(40, 262, 7, false, cufe.substring(0, Math.min(48, cufe.length())));
        if (cufe.length() > 48) {
            pdf.text(40, 252, 7, false, cufe.substring(48));
        }
        drawQr(pdf, 40, qrBottom, buildQrPayload(d));
        pdf.text(150, 170, 8, false, "Código QR simulado: representa los datos de la factura.");
        pdf.text(150, 158, 8, false, "Documento generado con fines académicos; no tiene validez fiscal.");
        pdf.line(40, 60, 555, 60, 0.5);
        pdf.text(40, 46, 8, false, "Gracias por confiar en " + SchoolInfo.LEGAL_NAME);

        pdf.save(file);
        return file;
    }

    private static String buildQrPayload(InvoiceDetail d) {
        return "NumFac=" + d.getInvoiceNumber() + "|NitFac=" + SchoolInfo.NIT + "|FecFac=" + d.getIssueDate()
                + "|ValTot=" + d.getAmountPaid().toPlainString() + "|CUFE=" + d.getCufe();
    }

    /** Draws a deterministic QR-looking matrix (finder patterns + pseudo-random modules from SHA-256). */
    private static void drawQr(SimplePdf pdf, double x, double y, String payload) {
        boolean[][] grid = new boolean[QR_MODULES][QR_MODULES];
        byte[] bits = HashUtil.digest("SHA-256", payload);
        int counter = 0;
        int bitIndex = 0;
        for (int r = 0; r < QR_MODULES; r++) {
            for (int c = 0; c < QR_MODULES; c++) {
                if (bitIndex >= bits.length * 8) {
                    counter++;
                    bits = HashUtil.digest("SHA-256", payload + counter);
                    bitIndex = 0;
                }
                grid[r][c] = ((bits[bitIndex / 8] >> (bitIndex % 8)) & 1) == 1;
                bitIndex++;
            }
        }
        int[][] finders = {{0, 0}, {0, QR_MODULES - 7}, {QR_MODULES - 7, 0}};
        for (int[] f : finders) {
            for (int dr = -1; dr <= 7; dr++) {
                for (int dc = -1; dc <= 7; dc++) {
                    int r = f[0] + dr;
                    int c = f[1] + dc;
                    if (r < 0 || c < 0 || r >= QR_MODULES || c >= QR_MODULES) {
                        continue;
                    }
                    boolean inside = dr >= 0 && dr <= 6 && dc >= 0 && dc <= 6;
                    boolean ring = inside && (dr == 0 || dr == 6 || dc == 0 || dc == 6);
                    boolean center = inside && dr >= 2 && dr <= 4 && dc >= 2 && dc <= 4;
                    grid[r][c] = ring || center;
                }
            }
        }
        double size = QR_MODULES * QR_MODULE_SIZE;
        pdf.strokeGray(0);
        pdf.rect(x - 6, y - 6, size + 12, size + 12, false, true);
        pdf.fillGray(0);
        for (int r = 0; r < QR_MODULES; r++) {
            for (int c = 0; c < QR_MODULES; c++) {
                if (grid[r][c]) {
                    pdf.rect(x + c * QR_MODULE_SIZE, y + (QR_MODULES - 1 - r) * QR_MODULE_SIZE,
                            QR_MODULE_SIZE, QR_MODULE_SIZE, true, false);
                }
            }
        }
    }

    private static String nullToDash(String value) {
        return value == null || value.isEmpty() ? "-" : value;
    }
}
