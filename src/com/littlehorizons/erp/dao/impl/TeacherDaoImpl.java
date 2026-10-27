package com.littlehorizons.erp.dao.impl;

import com.littlehorizons.erp.dao.TeacherDao;
import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.Jdbc;
import com.littlehorizons.erp.util.TableData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class TeacherDaoImpl implements TeacherDao {

    private static Teacher map(ResultSet rs) throws SQLException {
        Teacher t = new Teacher();
        t.setId(rs.getInt("id_profesor"));
        t.setDocumentType(rs.getString("tipo_documento"));
        t.setDocumentNumber(rs.getString("numero_documento"));
        t.setFirstName(rs.getString("primer_nombre"));
        t.setMiddleName(rs.getString("segundo_nombre"));
        t.setLastName(rs.getString("primer_apellido"));
        t.setSecondLastName(rs.getString("segundo_apellido"));
        t.setEmail(rs.getString("correo_electronico"));
        t.setPhone(rs.getString("telefono"));
        t.setSpecialization(rs.getString("especializacion"));
        t.setHireDate(Jdbc.toLocalDate(rs, "fecha_contratacion"));
        t.setStatus(rs.getString("estado"));
        return t;
    }

    @Override
    public int insert(Teacher t) throws ErpException {
        return (int) Jdbc.insert("INSERT INTO profesores (tipo_documento, numero_documento, primer_nombre, "
                + "segundo_nombre, primer_apellido, segundo_apellido, correo_electronico, telefono, especializacion, "
                + "fecha_contratacion, estado) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                t.getDocumentType(), t.getDocumentNumber(), t.getFirstName(), t.getMiddleName(), t.getLastName(),
                t.getSecondLastName(), t.getEmail(), t.getPhone(), t.getSpecialization(), t.getHireDate(),
                t.getStatus());
    }

    @Override
    public Optional<Teacher> findById(int id) throws ErpException {
        return Jdbc.queryOne("SELECT * FROM profesores WHERE id_profesor = ?", TeacherDaoImpl::map, id);
    }

    @Override
    public Optional<Teacher> findByDocument(String documentNumber) throws ErpException {
        return Jdbc.queryOne("SELECT * FROM profesores WHERE numero_documento = ?", TeacherDaoImpl::map,
                documentNumber);
    }

    @Override
    public TableData findAllSummary() throws ErpException {
        return Jdbc.queryTable("SELECT id_profesor AS ID, numero_documento AS Documento, "
                + "CONCAT_WS(' ', primer_nombre, segundo_nombre, primer_apellido, segundo_apellido) AS Profesor, "
                + "especializacion AS Especialización, correo_electronico AS Correo, estado AS Estado "
                + "FROM profesores ORDER BY primer_apellido, primer_nombre");
    }
}
