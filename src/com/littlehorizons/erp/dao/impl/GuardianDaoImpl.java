package com.littlehorizons.erp.dao.impl;

import com.littlehorizons.erp.dao.GuardianDao;
import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.Jdbc;
import com.littlehorizons.erp.util.TableData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class GuardianDaoImpl implements GuardianDao {

    private static Guardian map(ResultSet rs) throws SQLException {
        Guardian g = new Guardian();
        g.setId(rs.getInt("id_acudiente"));
        g.setDocumentType(rs.getString("tipo_documento"));
        g.setDocumentNumber(rs.getString("numero_documento"));
        g.setFirstName(rs.getString("primer_nombre"));
        g.setMiddleName(rs.getString("segundo_nombre"));
        g.setLastName(rs.getString("primer_apellido"));
        g.setSecondLastName(rs.getString("segundo_apellido"));
        g.setRelationship(rs.getString("parentesco"));
        g.setMainPhone(rs.getString("telefono_principal"));
        g.setSecondaryPhone(rs.getString("telefono_secundario"));
        g.setEmail(rs.getString("correo_electronico"));
        g.setAddress(rs.getString("direccion"));
        g.setOccupation(rs.getString("ocupacion"));
        g.setFinanciallyResponsible(rs.getBoolean("es_responsable_financiero"));
        return g;
    }

    @Override
    public int insert(Guardian g) throws ErpException {
        return (int) Jdbc.insert("INSERT INTO acudientes (tipo_documento, numero_documento, primer_nombre, "
                + "segundo_nombre, primer_apellido, segundo_apellido, parentesco, telefono_principal, "
                + "telefono_secundario, correo_electronico, direccion, ocupacion, es_responsable_financiero) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                g.getDocumentType(), g.getDocumentNumber(), g.getFirstName(), g.getMiddleName(), g.getLastName(),
                g.getSecondLastName(), g.getRelationship(), g.getMainPhone(), g.getSecondaryPhone(), g.getEmail(),
                g.getAddress(), g.getOccupation(), g.isFinanciallyResponsible());
    }

    @Override
    public Optional<Guardian> findById(int id) throws ErpException {
        return Jdbc.queryOne("SELECT * FROM acudientes WHERE id_acudiente = ?", GuardianDaoImpl::map, id);
    }

    @Override
    public Optional<Guardian> findByDocument(String documentNumber) throws ErpException {
        return Jdbc.queryOne("SELECT * FROM acudientes WHERE numero_documento = ?", GuardianDaoImpl::map,
                documentNumber);
    }

    @Override
    public Optional<Guardian> findMainGuardian(int studentId) throws ErpException {
        return Jdbc.queryOne("SELECT a.* FROM acudientes a JOIN estudiante_acudiente ea "
                + "ON ea.id_acudiente = a.id_acudiente WHERE ea.id_estudiante = ? "
                + "ORDER BY ea.es_principal DESC, a.es_responsable_financiero DESC, a.id_acudiente LIMIT 1",
                GuardianDaoImpl::map, studentId);
    }

    @Override
    public TableData findByStudent(int studentId) throws ErpException {
        return Jdbc.queryTable("SELECT a.id_acudiente AS ID, a.tipo_documento AS Tipo, a.numero_documento AS Documento, "
                + "CONCAT_WS(' ', a.primer_nombre, a.segundo_nombre, a.primer_apellido, a.segundo_apellido) AS Acudiente, "
                + "a.parentesco AS Parentesco, a.telefono_principal AS Teléfono, ea.es_principal AS Principal, "
                + "a.es_responsable_financiero AS `Resp. financiero` "
                + "FROM estudiante_acudiente ea JOIN acudientes a ON a.id_acudiente = ea.id_acudiente "
                + "WHERE ea.id_estudiante = ? ORDER BY ea.es_principal DESC", studentId);
    }

    @Override
    public boolean isLinked(int studentId, int guardianId) throws ErpException {
        return Jdbc.queryLong("SELECT COUNT(*) FROM estudiante_acudiente WHERE id_estudiante = ? AND id_acudiente = ?",
                studentId, guardianId) > 0;
    }

    @Override
    public void link(int studentId, int guardianId, boolean main, boolean emergencyContact) throws ErpException {
        Jdbc.update("INSERT INTO estudiante_acudiente (id_estudiante, id_acudiente, es_principal, "
                + "es_contacto_emergencia) VALUES (?, ?, ?, ?)", studentId, guardianId, main, emergencyContact);
    }
}
