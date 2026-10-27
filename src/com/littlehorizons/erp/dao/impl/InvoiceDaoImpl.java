package com.littlehorizons.erp.dao.impl;

import com.littlehorizons.erp.dao.InvoiceDao;
import com.littlehorizons.erp.model.Invoice;
import com.littlehorizons.erp.model.InvoiceDetail;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.Jdbc;
import com.littlehorizons.erp.util.TableData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class InvoiceDaoImpl implements InvoiceDao {

    private static Invoice map(ResultSet rs) throws SQLException {
        Invoice i = new Invoice();
        i.setId(rs.getInt("id_factura"));
        i.setInvoiceNumber(rs.getString("numero_factura"));
        i.setPaymentId(rs.getInt("id_pago"));
        i.setIssueDate(Jdbc.toLocalDate(rs, "fecha_emision"));
        i.setCufe(rs.getString("cufe"));
        i.setPdfFileName(rs.getString("nombre_archivo_pdf"));
        i.setPdfPath(rs.getString("ruta_archivo"));
        return i;
    }

    private static InvoiceDetail mapDetail(ResultSet rs) throws SQLException {
        InvoiceDetail d = new InvoiceDetail();
        d.setInvoiceNumber(rs.getString("numero_factura"));
        d.setIssueDate(Jdbc.toLocalDate(rs, "fecha_emision"));
        d.setCufe(rs.getString("cufe"));
        d.setGuardianName(rs.getString("nombre_acudiente"));
        d.setGuardianDocumentType(rs.getString("tipo_documento"));
        d.setGuardianDocument(rs.getString("numero_documento"));
        d.setGuardianAddress(rs.getString("direccion"));
        d.setGuardianEmail(rs.getString("correo_electronico"));
        d.setStudentName(rs.getString("nombre_estudiante"));
        d.setConceptCode(rs.getString("codigo_concepto"));
        d.setConceptName(rs.getString("nombre_concepto"));
        d.setQuantity(1);
        d.setAmountPaid(rs.getBigDecimal("monto_pagado"));
        d.setPaymentMethod(rs.getString("metodo_pago"));
        d.setReceiptNumber(rs.getString("numero_recibo"));
        d.setPaymentDate(Jdbc.toLocalDate(rs, "fecha_pago"));
        return d;
    }

    @Override
    public long nextSequence() throws ErpException {
        return Jdbc.queryLong("SELECT COALESCE(MAX(id_factura), 0) + 1 FROM facturas");
    }

    @Override
    public int insert(Invoice i) throws ErpException {
        return (int) Jdbc.insert("INSERT INTO facturas (numero_factura, id_pago, fecha_emision, cufe, "
                + "nombre_archivo_pdf, ruta_archivo) VALUES (?, ?, ?, ?, ?, ?)",
                i.getInvoiceNumber(), i.getPaymentId(), i.getIssueDate(), i.getCufe(), i.getPdfFileName(),
                i.getPdfPath());
    }

    @Override
    public void updateFile(int invoiceId, String fileName, String path) throws ErpException {
        Jdbc.update("UPDATE facturas SET nombre_archivo_pdf = ?, ruta_archivo = ? WHERE id_factura = ?",
                fileName, path, invoiceId);
    }

    @Override
    public Optional<Invoice> findByPaymentId(int paymentId) throws ErpException {
        return Jdbc.queryOne("SELECT * FROM facturas WHERE id_pago = ?", InvoiceDaoImpl::map, paymentId);
    }

    @Override
    public Optional<InvoiceDetail> findDetailByPaymentId(int paymentId) throws ErpException {
        return Jdbc.queryOne("SELECT f.numero_factura, f.fecha_emision, f.cufe, "
                + "CONCAT_WS(' ', a.primer_nombre, a.segundo_nombre, a.primer_apellido, a.segundo_apellido) AS nombre_acudiente, "
                + "a.tipo_documento, a.numero_documento, a.direccion, a.correo_electronico, "
                + "CONCAT_WS(' ', e.primer_nombre, e.segundo_nombre, e.primer_apellido, e.segundo_apellido) AS nombre_estudiante, "
                + "c.codigo_concepto, c.nombre_concepto, p.monto_pagado, p.metodo_pago, p.numero_recibo, p.fecha_pago "
                + "FROM facturas f JOIN pagos p ON p.id_pago = f.id_pago "
                + "JOIN estudiantes e ON e.id_estudiante = p.id_estudiante "
                + "JOIN conceptos_pago c ON c.id_concepto = p.id_concepto "
                + "JOIN acudientes a ON a.id_acudiente = p.id_acudiente WHERE f.id_pago = ?",
                InvoiceDaoImpl::mapDetail, paymentId);
    }

    @Override
    public TableData findAllSummary() throws ErpException {
        return Jdbc.queryTable("SELECT f.numero_factura AS Factura, f.id_pago AS Pago, f.fecha_emision AS Emisión, "
                + "p.monto_pagado AS Total, f.nombre_archivo_pdf AS Archivo "
                + "FROM facturas f JOIN pagos p ON p.id_pago = f.id_pago ORDER BY f.id_factura DESC");
    }
}
