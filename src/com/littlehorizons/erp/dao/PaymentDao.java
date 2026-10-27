package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.TableData;
import java.time.LocalDate;
import java.util.Optional;

public interface PaymentDao {

    int insert(Payment payment) throws ErpException;

    Optional<Payment> findById(int id) throws ErpException;

    String nextReceiptNumber(int year) throws ErpException;

    TableData findByStudent(int studentId) throws ErpException;

    TableData findByGuardian(int guardianId) throws ErpException;

    /** Active students whose last PENSION payment is older than the cutoff (or who never paid). */
    TableData findDelinquents(LocalDate cutoff) throws ErpException;
}
