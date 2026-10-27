package com.littlehorizons.erp.controller;

import java.nio.file.Path;

/** Outcome of registering a payment: ids, invoice number and generated PDF (null if generation failed). */
public class PaymentResult {

    private final int paymentId;
    private final String receiptNumber;
    private final String invoiceNumber;
    private final Path pdfPath;
    private final String warning;

    public PaymentResult(int paymentId, String receiptNumber, String invoiceNumber, Path pdfPath, String warning) {
        this.paymentId = paymentId;
        this.receiptNumber = receiptNumber;
        this.invoiceNumber = invoiceNumber;
        this.pdfPath = pdfPath;
        this.warning = warning;
    }

    public int getPaymentId() {
        return paymentId;
    }

    public String getReceiptNumber() {
        return receiptNumber;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public Path getPdfPath() {
        return pdfPath;
    }

    public String getWarning() {
        return warning;
    }
}
