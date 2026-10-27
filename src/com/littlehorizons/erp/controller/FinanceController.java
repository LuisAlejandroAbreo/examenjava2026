package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.GuardianDao;
import com.littlehorizons.erp.dao.InvoiceDao;
import com.littlehorizons.erp.dao.PaymentConceptDao;
import com.littlehorizons.erp.dao.PaymentDao;
import com.littlehorizons.erp.dao.StudentDao;
import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.model.InvoiceDetail;
import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.model.PaymentConcept;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.util.AppLogger;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.HashUtil;
import com.littlehorizons.erp.util.InvoicePdfGenerator;
import com.littlehorizons.erp.util.SchoolInfo;
import com.littlehorizons.erp.util.TableData;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/** Business rules for payment concepts, payments and electronic invoices. */
public class FinanceController {

    public static final List<String> METHODS =
            Arrays.asList("Efectivo", "Transferencia", "Tarjeta Débito", "Tarjeta Crédito", "Cheque");
    public static final List<String> PERIODS = Arrays.asList("I", "II", "III", "IV", "Anual");

    private final PaymentConceptDao conceptDao;
    private final PaymentDao paymentDao;
    private final InvoiceDao invoiceDao;
    private final GuardianDao guardianDao;
    private final StudentDao studentDao;
    private final InvoicePdfGenerator pdfGenerator;

    public FinanceController(PaymentConceptDao conceptDao, PaymentDao paymentDao, InvoiceDao invoiceDao,
            GuardianDao guardianDao, StudentDao studentDao, InvoicePdfGenerator pdfGenerator) {
        this.conceptDao = conceptDao;
        this.paymentDao = paymentDao;
        this.invoiceDao = invoiceDao;
        this.guardianDao = guardianDao;
        this.studentDao = studentDao;
        this.pdfGenerator = pdfGenerator;
    }

    // ---------- Concepts ----------

    public TableData listConcepts() throws ErpException {
        return conceptDao.findAllSummary();
    }

    public int createConcept(PaymentConcept concept) throws ErpException {
        concept.setCode(concept.getCode().toUpperCase());
        concept.setActive(true);
        return conceptDao.insert(concept);
    }

    public PaymentConcept getConcept(int id) throws ErpException {
        return conceptDao.findById(id).orElseThrow(() -> new ErpException("No existe el concepto " + id + "."));
    }

    public void setCustomAmount(int studentId, int conceptId, BigDecimal amount, String reason) throws ErpException {
        requireStudent(studentId);
        getConcept(conceptId);
        conceptDao.saveCustomAmount(studentId, conceptId, amount, reason);
    }

    public TableData customAmounts(int studentId) throws ErpException {
        return conceptDao.findCustomAmounts(studentId);
    }

    /** Amount the student should pay: the custom value when one exists, otherwise the base value. */
    public BigDecimal referenceAmount(int studentId, int conceptId) throws ErpException {
        PaymentConcept concept = getConcept(conceptId);
        return conceptDao.findCustomAmount(studentId, conceptId).orElse(concept.getBaseAmount());
    }

    private Student requireStudent(int studentId) throws ErpException {
        return studentDao.findById(studentId)
                .orElseThrow(() -> new ErpException("No existe el estudiante " + studentId + "."));
    }

    // ---------- Payments ----------

    /**
     * Registers the payment and automatically generates the invoice (database record + PDF).
     * guardianId may be null: the main guardian of the student is used.
     */
    public PaymentResult registerPayment(int studentId, int conceptId, BigDecimal amount, String method, String period,
            Integer academicYear, Integer guardianId, LocalDate paymentDate, String notes) throws ErpException {
        requireStudent(studentId);
        PaymentConcept concept = getConcept(conceptId);
        if (!concept.isActive()) {
            throw new ErpException("El concepto de pago está inactivo.");
        }
        if (!METHODS.contains(method)) {
            throw new ErpException("Método de pago inválido.");
        }
        if (amount.signum() <= 0) {
            throw new ErpException("El monto debe ser mayor que cero.");
        }
        if (paymentDate.isAfter(LocalDate.now())) {
            throw new ErpException("La fecha de pago no puede ser futura.");
        }
        Guardian payer;
        if (guardianId == null) {
            payer = guardianDao.findMainGuardian(studentId)
                    .orElseThrow(() -> new ErpException("El estudiante no tiene acudiente asociado."));
        } else {
            payer = guardianDao.findById(guardianId)
                    .orElseThrow(() -> new ErpException("No existe el acudiente " + guardianId + "."));
            if (!guardianDao.isLinked(studentId, payer.getId())) {
                throw new ErpException("Ese acudiente no está asociado al estudiante.");
            }
        }

        Payment payment = new Payment();
        payment.setStudentId(studentId);
        payment.setGuardianId(payer.getId());
        payment.setConceptId(conceptId);
        payment.setAmountPaid(amount);
        payment.setPaymentDate(paymentDate);
        payment.setMethod(method);
        payment.setAcademicYear(academicYear);
        payment.setPeriod(period);
        payment.setNotes(notes);
        payment.setReceiptNumber(paymentDao.nextReceiptNumber(paymentDate.getYear()));
        int paymentId = paymentDao.insert(payment);

        return issueInvoice(paymentId, payment.getReceiptNumber(), amount, paymentDate);
    }

    private PaymentResult issueInvoice(int paymentId, String receipt, BigDecimal total, LocalDate paymentDate)
            throws ErpException {
        LocalDate today = LocalDate.now();
        String number = SchoolInfo.INVOICE_PREFIX + "-" + String.format("%06d", invoiceDao.nextSequence());
        Invoice invoice = new Invoice();
        invoice.setInvoiceNumber(number);
        invoice.setPaymentId(paymentId);
        invoice.setIssueDate(today);
        invoice.setCufe(HashUtil.cufe(number, SchoolInfo.NIT, today.toString(), total.toPlainString()));
        int invoiceId = invoiceDao.insert(invoice);
        try {
            Path pdf = generatePdf(paymentId);
            invoiceDao.updateFile(invoiceId, pdf.getFileName().toString(), pdf.toAbsolutePath().toString());
            return new PaymentResult(paymentId, receipt, number, pdf.toAbsolutePath(), null);
        } catch (IOException | ErpException e) {
            AppLogger.error("No se pudo generar el PDF de la factura " + number, e);
            return new PaymentResult(paymentId, receipt, number, null,
                    "El pago y la factura quedaron registrados, pero el PDF no se pudo generar. "
                    + "Use la opción 'Regenerar PDF de factura'.");
        }
    }

    private Path generatePdf(int paymentId) throws IOException, ErpException {
        InvoiceDetail detail = invoiceDao.findDetailByPaymentId(paymentId)
                .orElseThrow(() -> new ErpException("No hay factura para el pago " + paymentId + "."));
        return pdfGenerator.generate(detail);
    }

    /** Regenerates the PDF of an existing invoice (identified by its payment id). */
    public Path regenerateInvoicePdf(int paymentId) throws ErpException {
        Invoice invoice = invoiceDao.findByPaymentId(paymentId)
                .orElseThrow(() -> new ErpException("El pago " + paymentId + " no tiene factura."));
        try {
            Path pdf = generatePdf(paymentId);
            invoiceDao.updateFile(invoice.getId(), pdf.getFileName().toString(), pdf.toAbsolutePath().toString());
            return pdf.toAbsolutePath();
        } catch (IOException e) {
            AppLogger.error("No se pudo regenerar el PDF del pago " + paymentId, e);
            throw new ErpException("No se pudo escribir el PDF: " + e.getMessage(), e);
        }
    }

    public TableData paymentsOfStudent(int studentId) throws ErpException {
        requireStudent(studentId);
        return paymentDao.findByStudent(studentId);
    }

    public TableData paymentsOfGuardian(int guardianId) throws ErpException {
        guardianDao.findById(guardianId).orElseThrow(() -> new ErpException("No existe el acudiente " + guardianId + "."));
        return paymentDao.findByGuardian(guardianId);
    }

    public TableData listInvoices() throws ErpException {
        return invoiceDao.findAllSummary();
    }

    /** Students whose last tuition (PENSION) payment is more than one month old. */
    public TableData delinquents() throws ErpException {
        return paymentDao.findDelinquents(LocalDate.now().minusMonths(1));
    }
}
