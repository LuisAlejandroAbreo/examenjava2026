package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.FinanceController;
import com.littlehorizons.erp.controller.PaymentResult;
import com.littlehorizons.erp.controller.StudentController;
import com.littlehorizons.erp.controller.TeacherController;
import com.littlehorizons.erp.model.PaymentConcept;
import com.littlehorizons.erp.util.ConsoleUI;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.InputReader;
import com.littlehorizons.erp.util.Money;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.LocalDate;

/** Console view for payment concepts, payments, invoices and delinquency report. */
public class FinanceView extends BaseView {

    private final FinanceController finance;

    public FinanceView(InputReader input, StudentController students, TeacherController teachers, FinanceController finance) {
        super(input, students, teachers);
        this.finance = finance;
    }

    public void show() {
        while (true) {
            ConsoleUI.menu("GESTIÓN FINANCIERA", "Ver catálogo de conceptos de pago", "Crear concepto de pago",
                    "Definir valor personalizado (descuento / beca)", "Registrar pago y generar factura PDF",
                    "Historial de pagos de un estudiante", "Historial de pagos de un acudiente",
                    "Listar facturas emitidas", "Regenerar PDF de una factura", "Reporte de estudiantes morosos");
            final int option = input.readInt("Seleccione una opción: ", 0, 9);
            if (option == 0) {
                return;
            }
            safely(() -> dispatch(option));
        }
    }

    private void dispatch(int option) throws ErpException {
        switch (option) {
            case 1: ConsoleUI.table(finance.listConcepts()); break;
            case 2: createConcept(); break;
            case 3: customAmount(); break;
            case 4: registerPayment(); break;
            case 5: studentHistory(); break;
            case 6: guardianHistory(); break;
            case 7: ConsoleUI.table(finance.listInvoices()); break;
            case 8: regenerate(); break;
            case 9: ConsoleUI.table(finance.delinquents()); break;
            default: break;
        }
    }

    private void createConcept() throws ErpException {
        PaymentConcept c = new PaymentConcept();
        c.setCode(input.readRequired("Código (ej: PENSION): ", 20));
        c.setName(input.readRequired("Nombre del concepto: ", 100));
        c.setDescription(input.readOptional("Descripción", 500));
        c.setBaseAmount(input.readMoney("Valor base en pesos: "));
        c.setRecurring(input.confirm("¿Es un pago recurrente (mensual)?"));
        int id = finance.createConcept(c);
        ConsoleUI.success("Concepto creado con ID " + id + ".");
    }

    private void customAmount() throws ErpException {
        int studentId = askStudentId();
        if (studentId == 0) {
            return;
        }
        ConsoleUI.table(finance.listConcepts());
        int conceptId = input.readId("ID del concepto");
        if (conceptId == 0) {
            return;
        }
        BigDecimal amount = input.readMoney("Valor personalizado en pesos: ");
        String reason = input.readOptional("Motivo (ej: Descuento por hermanos)", 150);
        finance.setCustomAmount(studentId, conceptId, amount, reason);
        ConsoleUI.success("Valor personalizado guardado.");
        ConsoleUI.table(finance.customAmounts(studentId));
    }

    private void registerPayment() throws ErpException {
        ConsoleUI.section("Registro de pago");
        int studentId = askStudentId();
        if (studentId == 0) {
            return;
        }
        ConsoleUI.table(finance.listConcepts());
        int conceptId = input.readId("ID del concepto a pagar");
        if (conceptId == 0) {
            return;
        }
        BigDecimal reference = finance.referenceAmount(studentId, conceptId);
        ConsoleUI.info("Valor de referencia para este estudiante: " + Money.format(reference));
        BigDecimal amount = input.readMoneyOrDefault("Monto pagado", reference);
        String method = input.readOption("Método de pago:", FinanceController.METHODS.toArray(new String[0]));
        String period = input.readOption("Período al que aplica:", "I", "II", "III", "IV", "Anual", "No aplica");
        if ("No aplica".equals(period)) {
            period = null;
        }
        int year = askYear();
        ConsoleUI.table(students.guardiansOf(studentId));
        int guardian = input.readInt("ID del acudiente que paga (0 = acudiente principal): ", 0, Integer.MAX_VALUE);
        LocalDate date = input.readDateOrToday("Fecha de pago");
        String notes = input.readOptional("Notas", 500);
        if (!input.confirm("¿Confirmar pago de " + Money.format(amount) + "?")) {
            ConsoleUI.info("Pago cancelado.");
            return;
        }
        PaymentResult result = finance.registerPayment(studentId, conceptId, amount, method, period, year,
                guardian == 0 ? null : guardian, date, notes);
        ConsoleUI.success("Pago registrado. Recibo: " + result.getReceiptNumber()
                + " | Factura: " + result.getInvoiceNumber());
        if (result.getPdfPath() != null) {
            ConsoleUI.success("Factura PDF guardada en: " + result.getPdfPath());
        }
        if (result.getWarning() != null) {
            ConsoleUI.error(result.getWarning());
        }
    }

    private void studentHistory() throws ErpException {
        int studentId = askStudentId();
        if (studentId != 0) {
            ConsoleUI.table(finance.paymentsOfStudent(studentId));
        }
    }

    private void guardianHistory() throws ErpException {
        int guardianId = input.readId("ID del acudiente");
        if (guardianId != 0) {
            ConsoleUI.table(finance.paymentsOfGuardian(guardianId));
        }
    }

    private void regenerate() throws ErpException {
        int paymentId = input.readId("ID del pago");
        if (paymentId != 0) {
            Path pdf = finance.regenerateInvoicePdf(paymentId);
            ConsoleUI.success("PDF regenerado en: " + pdf);
        }
    }
}
