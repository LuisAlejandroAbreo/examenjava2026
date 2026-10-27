package com.littlehorizons.erp.dao.impl;

import com.littlehorizons.erp.dao.PaymentConceptDao;
import com.littlehorizons.erp.model.PaymentConcept;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.Jdbc;
import com.littlehorizons.erp.util.TableData;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class PaymentConceptDaoImpl implements PaymentConceptDao {

    private static PaymentConcept map(ResultSet rs) throws SQLException {
        PaymentConcept c = new PaymentConcept();
        c.setId(rs.getInt("id_concepto"));
        c.setCode(rs.getString("codigo_concepto"));
        c.setName(rs.getString("nombre_concepto"));
        c.setDescription(rs.getString("descripcion"));
        c.setBaseAmount(rs.getBigDecimal("monto_base"));
        c.setRecurring(rs.getBoolean("es_recurrente"));
        c.setActive(rs.getBoolean("esta_activo"));
        return c;
    }

    @Override
    public int insert(PaymentConcept c) throws ErpException {
        return (int) Jdbc.insert("INSERT INTO conceptos_pago (codigo_concepto, nombre_concepto, descripcion, "
                + "monto_base, es_recurrente, esta_activo) VALUES (?, ?, ?, ?, ?, ?)",
                c.getCode(), c.getName(), c.getDescription(), c.getBaseAmount(), c.isRecurring(), c.isActive());
    }

    @Override
    public Optional<PaymentConcept> findById(int id) throws ErpException {
        return Jdbc.queryOne("SELECT * FROM conceptos_pago WHERE id_concepto = ?", PaymentConceptDaoImpl::map, id);
    }

    @Override
    public TableData findAllSummary() throws ErpException {
        return Jdbc.queryTable("SELECT id_concepto AS ID, codigo_concepto AS Código, nombre_concepto AS Concepto, "
                + "monto_base AS `Valor base`, es_recurrente AS Recurrente, esta_activo AS Activo "
                + "FROM conceptos_pago ORDER BY id_concepto");
    }

    @Override
    public void saveCustomAmount(int studentId, int conceptId, BigDecimal amount, String reason) throws ErpException {
        Jdbc.update("INSERT INTO valores_concepto_estudiante (id_estudiante, id_concepto, monto_personalizado, motivo) "
                + "VALUES (?, ?, ?, ?) ON DUPLICATE KEY UPDATE monto_personalizado = VALUES(monto_personalizado), "
                + "motivo = VALUES(motivo)", studentId, conceptId, amount, reason);
    }

    @Override
    public Optional<BigDecimal> findCustomAmount(int studentId, int conceptId) throws ErpException {
        return Jdbc.queryOne("SELECT monto_personalizado FROM valores_concepto_estudiante "
                + "WHERE id_estudiante = ? AND id_concepto = ?", rs -> rs.getBigDecimal(1), studentId, conceptId);
    }

    @Override
    public TableData findCustomAmounts(int studentId) throws ErpException {
        return Jdbc.queryTable("SELECT c.nombre_concepto AS Concepto, c.monto_base AS `Valor base`, "
                + "v.monto_personalizado AS `Valor personalizado`, v.motivo AS Motivo "
                + "FROM valores_concepto_estudiante v JOIN conceptos_pago c ON c.id_concepto = v.id_concepto "
                + "WHERE v.id_estudiante = ? ORDER BY c.nombre_concepto", studentId);
    }
}
