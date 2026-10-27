package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.PaymentConcept;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.TableData;
import java.math.BigDecimal;
import java.util.Optional;

public interface PaymentConceptDao {

    int insert(PaymentConcept concept) throws ErpException;

    Optional<PaymentConcept> findById(int id) throws ErpException;

    TableData findAllSummary() throws ErpException;

    /** Creates or updates the custom amount of a concept for one student (discount / scholarship). */
    void saveCustomAmount(int studentId, int conceptId, BigDecimal amount, String reason) throws ErpException;

    Optional<BigDecimal> findCustomAmount(int studentId, int conceptId) throws ErpException;

    TableData findCustomAmounts(int studentId) throws ErpException;
}
