package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.model.InvoiceDetail;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.TableData;
import java.util.Optional;

public interface InvoiceDao {

    /** Next consecutive invoice sequence number. */
    long nextSequence() throws ErpException;

    int insert(Invoice invoice) throws ErpException;

    void updateFile(int invoiceId, String fileName, String path) throws ErpException;

    Optional<Invoice> findByPaymentId(int paymentId) throws ErpException;

    Optional<InvoiceDetail> findDetailByPaymentId(int paymentId) throws ErpException;

    TableData findAllSummary() throws ErpException;
}
