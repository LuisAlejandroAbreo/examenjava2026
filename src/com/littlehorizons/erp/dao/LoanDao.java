package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.TableData;
import java.time.LocalDate;

/** Loans of resources to students (prestamos_estudiante) and teachers (prestamos_profesor). */
public interface LoanDao {

    String STUDENT = "ESTUDIANTE";
    String TEACHER = "PROFESOR";

    /** Atomically checks availability, registers the loan and marks the resource as 'Prestado'. */
    int lend(int resourceId, String borrowerType, int borrowerId, LocalDate loanDate, LocalDate dueDate,
            String notes) throws ErpException;

    /** Atomically closes the loan and marks the resource as 'Disponible'. */
    void giveBack(String borrowerType, int loanId, LocalDate returnDate) throws ErpException;

    /** Marks active loans past their due date as 'Vencido'. Returns the number of loans updated. */
    int refreshOverdue(LocalDate today) throws ErpException;

    TableData findOpenLoans() throws ErpException;

    TableData findOverdue(LocalDate today) throws ErpException;
}
