package com.littlehorizons.erp.dao.impl;

import com.littlehorizons.erp.dao.PaymentDao;
import com.littlehorizons.erp.model.Payment;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.Jdbc;
import com.littlehorizons.erp.util.TableData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public class PaymentDaoImpl implements PaymentDao {

    private static final String HISTORY_SELECT =
            "SELECT p.id_pago AS ID, p.numero_recibo AS Recibo, p.fecha_pago AS Fecha, "
            + "CONCAT_WS(' ', e.primer_nombre, e.primer_apellido) AS Estudiante, c.nombre_concepto AS Concepto, "
            + "p.monto_pagado AS Monto, p.metodo_pago AS Método, p.periodo_academico AS Período, "
            + "f.numero_factura AS Factura "
            + "FROM pagos p JOIN estudiantes e ON e.id_estudiante = p.id_estudiante "
            + "JOIN conceptos_pago c ON c.id_concepto = p.id_concepto "
            + "LEFT JOIN facturas f ON f.id_pago = p.id_pago ";

    private static Payment map(ResultSet rs) throws SQLException {
        Payment p = new Payment();
        p.setId(rs.getInt("id_pago"));
        p.setStudentId(rs.getInt("id_estudiante"));
        p.setGuardianId(Jdbc.getInteger(rs, "id_acudiente"));
        p.setConceptId(rs.getInt("id_concepto"));
        p.setAmountPaid(rs.getBigDecimal("monto_pagado"));
        p.setPaymentDate(Jdbc.toLocalDate(rs, "fecha_pago"));
        p.setMethod(rs.getString("metodo_pago"));
        p.setReceiptNumber(rs.getString("numero_recibo"));
        p.setAcademicYear(Jdbc.getInteger(rs, "año_academico"));
        p.setPeriod(rs.getString("periodo_academico"));
        p.setNotes(rs.getString("notas"));
        return p;
    }

    @Override
    public int insert(Payment p) throws ErpException {
        return (int) Jdbc.insert("INSERT INTO pagos (id_estudiante, id_acudiente, id_concepto, monto_pagado, "
                + "fecha_pago, metodo_pago, numero_recibo, año_academico, periodo_academico, notas) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                p.getStudentId(), p.getGuardianId(), p.getConceptId(), p.getAmountPaid(), p.getPaymentDate(),
                p.getMethod(), p.getReceiptNumber(), p.getAcademicYear(), p.getPeriod(), p.getNotes());
    }

    @Override
    public Optional<Payment> findById(int id) throws ErpException {
        return Jdbc.queryOne("SELECT * FROM pagos WHERE id_pago = ?", PaymentDaoImpl::map, id);
    }

    @Override
    public String nextReceiptNumber(int year) throws ErpException {
        long next = Jdbc.queryLong("SELECT COALESCE(MAX(id_pago), 0) + 1 FROM pagos");
        return String.format("REC-%d-%06d", year, next);
    }

    @Override
    public TableData findByStudent(int studentId) throws ErpException {
        return Jdbc.queryTable(HISTORY_SELECT + "WHERE p.id_estudiante = ? ORDER BY p.fecha_pago DESC, p.id_pago DESC",
                studentId);
    }

    @Override
    public TableData findByGuardian(int guardianId) throws ErpException {
        return Jdbc.queryTable(HISTORY_SELECT
                + "WHERE p.id_acudiente = ? OR p.id_estudiante IN "
                + "(SELECT ea.id_estudiante FROM estudiante_acudiente ea WHERE ea.id_acudiente = ?) "
                + "ORDER BY p.fecha_pago DESC, p.id_pago DESC", guardianId, guardianId);
    }

    @Override
    public TableData findDelinquents(LocalDate cutoff) throws ErpException {
        return Jdbc.queryTable("SELECT e.id_estudiante AS ID, "
                + "CONCAT_WS(' ', e.primer_nombre, e.segundo_nombre, e.primer_apellido, e.segundo_apellido) AS Estudiante, "
                + "g.nombre_grado AS Grado, up.ultimo AS `Último pago de pensión`, "
                + "(SELECT CONCAT_WS(' ', a.primer_nombre, a.primer_apellido) FROM estudiante_acudiente ea "
                + "JOIN acudientes a ON a.id_acudiente = ea.id_acudiente WHERE ea.id_estudiante = e.id_estudiante "
                + "ORDER BY ea.es_principal DESC, a.id_acudiente LIMIT 1) AS Acudiente, "
                + "(SELECT a.telefono_principal FROM estudiante_acudiente ea "
                + "JOIN acudientes a ON a.id_acudiente = ea.id_acudiente WHERE ea.id_estudiante = e.id_estudiante "
                + "ORDER BY ea.es_principal DESC, a.id_acudiente LIMIT 1) AS Teléfono "
                + "FROM estudiantes e "
                + "LEFT JOIN matriculas m ON m.id_estudiante = e.id_estudiante AND m.año_academico = "
                + "(SELECT MAX(m2.año_academico) FROM matriculas m2 WHERE m2.id_estudiante = e.id_estudiante) "
                + "LEFT JOIN grados g ON g.id_grado = m.id_grado "
                + "LEFT JOIN (SELECT p.id_estudiante, MAX(p.fecha_pago) AS ultimo FROM pagos p "
                + "JOIN conceptos_pago c ON c.id_concepto = p.id_concepto WHERE c.codigo_concepto = 'PENSION' "
                + "GROUP BY p.id_estudiante) up ON up.id_estudiante = e.id_estudiante "
                + "WHERE e.estado = 'Activo' AND (up.ultimo IS NULL OR up.ultimo < ?) "
                + "ORDER BY up.ultimo, e.primer_apellido", cutoff);
    }
}
