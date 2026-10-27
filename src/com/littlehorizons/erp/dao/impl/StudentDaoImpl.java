package com.littlehorizons.erp.dao.impl;

import com.littlehorizons.erp.dao.StudentDao;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.Jdbc;
import com.littlehorizons.erp.util.TableData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class StudentDaoImpl implements StudentDao {

    private static final String SUMMARY_SELECT =
            "SELECT e.id_estudiante AS ID, e.tipo_documento AS Tipo, e.numero_documento AS Documento, "
            + "CONCAT_WS(' ', e.primer_nombre, e.segundo_nombre, e.primer_apellido, e.segundo_apellido) AS Estudiante, "
            + "g.nombre_grado AS Grado, e.estado AS Estado "
            + "FROM estudiantes e "
            + "LEFT JOIN matriculas m ON m.id_estudiante = e.id_estudiante AND m.año_academico = "
            + "(SELECT MAX(m2.año_academico) FROM matriculas m2 WHERE m2.id_estudiante = e.id_estudiante) "
            + "LEFT JOIN grados g ON g.id_grado = m.id_grado ";

    private static Student map(ResultSet rs) throws SQLException {
        Student s = new Student();
        s.setId(rs.getInt("id_estudiante"));
        s.setDocumentType(rs.getString("tipo_documento"));
        s.setDocumentNumber(rs.getString("numero_documento"));
        s.setFirstName(rs.getString("primer_nombre"));
        s.setMiddleName(rs.getString("segundo_nombre"));
        s.setLastName(rs.getString("primer_apellido"));
        s.setSecondLastName(rs.getString("segundo_apellido"));
        s.setBirthDate(Jdbc.toLocalDate(rs, "fecha_nacimiento"));
        s.setGender(rs.getString("genero"));
        s.setAddress(rs.getString("direccion"));
        s.setPhone(rs.getString("telefono"));
        s.setEmail(rs.getString("correo_electronico"));
        s.setBloodType(rs.getString("tipo_sangre"));
        s.setAllergies(rs.getString("alergias"));
        s.setMedicalConditions(rs.getString("condiciones_medicas"));
        s.setAdmissionDate(Jdbc.toLocalDate(rs, "fecha_ingreso"));
        s.setStatus(rs.getString("estado"));
        return s;
    }

    @Override
    public int insert(Student s) throws ErpException {
        return (int) Jdbc.insert("INSERT INTO estudiantes (tipo_documento, numero_documento, primer_nombre, "
                + "segundo_nombre, primer_apellido, segundo_apellido, fecha_nacimiento, genero, direccion, telefono, "
                + "correo_electronico, tipo_sangre, alergias, condiciones_medicas, fecha_ingreso, estado) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                s.getDocumentType(), s.getDocumentNumber(), s.getFirstName(), s.getMiddleName(), s.getLastName(),
                s.getSecondLastName(), s.getBirthDate(), s.getGender(), s.getAddress(), s.getPhone(), s.getEmail(),
                s.getBloodType(), s.getAllergies(), s.getMedicalConditions(), s.getAdmissionDate(), s.getStatus());
    }

    @Override
    public Optional<Student> findById(int id) throws ErpException {
        return Jdbc.queryOne("SELECT * FROM estudiantes WHERE id_estudiante = ?", StudentDaoImpl::map, id);
    }

    @Override
    public Optional<Student> findByDocument(String documentNumber) throws ErpException {
        return Jdbc.queryOne("SELECT * FROM estudiantes WHERE numero_documento = ?", StudentDaoImpl::map,
                documentNumber);
    }

    @Override
    public TableData findAllSummary() throws ErpException {
        return Jdbc.queryTable(SUMMARY_SELECT + "ORDER BY e.primer_apellido, e.segundo_apellido, e.primer_nombre");
    }

    @Override
    public TableData search(String text) throws ErpException {
        String like = "%" + text + "%";
        return Jdbc.queryTable(SUMMARY_SELECT
                + "WHERE e.numero_documento LIKE ? OR e.primer_nombre LIKE ? OR e.segundo_nombre LIKE ? "
                + "OR e.primer_apellido LIKE ? OR e.segundo_apellido LIKE ? "
                + "ORDER BY e.primer_apellido, e.primer_nombre", like, like, like, like, like);
    }

    @Override
    public TableData findByGuardian(int guardianId) throws ErpException {
        return Jdbc.queryTable(SUMMARY_SELECT
                + "JOIN estudiante_acudiente ea ON ea.id_estudiante = e.id_estudiante "
                + "WHERE ea.id_acudiente = ? ORDER BY e.primer_apellido, e.primer_nombre", guardianId);
    }

    @Override
    public void updateStatus(int id, String status) throws ErpException {
        Jdbc.update("UPDATE estudiantes SET estado = ? WHERE id_estudiante = ?", status, id);
    }
}
