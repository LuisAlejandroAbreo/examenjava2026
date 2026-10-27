package com.littlehorizons.erp.dao.impl;

import com.littlehorizons.erp.dao.AcademicRecordDao;
import com.littlehorizons.erp.model.AcademicRecord;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.Jdbc;
import com.littlehorizons.erp.util.TableData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class AcademicRecordDaoImpl implements AcademicRecordDao {

    private static final String SELECT = "SELECT r.id_registro, r.id_estudiante, r.id_materia, r.id_profesor, "
            + "r.año_academico, r.periodo_academico, r.valor_nota, r.observaciones, r.fecha_registro, "
            + "m.nombre_materia FROM registros_academicos r JOIN materias m ON m.id_materia = r.id_materia ";

    private static AcademicRecord map(ResultSet rs) throws SQLException {
        AcademicRecord r = new AcademicRecord();
        r.setId(rs.getInt("id_registro"));
        r.setStudentId(rs.getInt("id_estudiante"));
        r.setSubjectId(rs.getInt("id_materia"));
        r.setTeacherId(rs.getInt("id_profesor"));
        r.setAcademicYear(rs.getInt("año_academico"));
        r.setPeriod(rs.getString("periodo_academico"));
        r.setValue(rs.getBigDecimal("valor_nota"));
        r.setObservations(rs.getString("observaciones"));
        r.setRecordDate(Jdbc.toLocalDate(rs, "fecha_registro"));
        r.setSubjectName(rs.getString("nombre_materia"));
        return r;
    }

    @Override
    public void save(AcademicRecord r) throws ErpException {
        Jdbc.update("INSERT INTO registros_academicos (id_estudiante, id_materia, id_profesor, año_academico, "
                + "periodo_academico, valor_nota, observaciones, fecha_registro) VALUES (?, ?, ?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE valor_nota = VALUES(valor_nota), id_profesor = VALUES(id_profesor), "
                + "observaciones = VALUES(observaciones), fecha_registro = VALUES(fecha_registro)",
                r.getStudentId(), r.getSubjectId(), r.getTeacherId(), r.getAcademicYear(), r.getPeriod(),
                r.getValue(), r.getObservations(), r.getRecordDate());
    }

    @Override
    public List<AcademicRecord> findByStudent(int studentId, int academicYear) throws ErpException {
        return Jdbc.queryList(SELECT + "WHERE r.id_estudiante = ? AND r.año_academico = ? "
                + "ORDER BY m.nombre_materia, r.periodo_academico", AcademicRecordDaoImpl::map, studentId,
                academicYear);
    }

    @Override
    public List<AcademicRecord> findBySubject(int subjectId, int academicYear) throws ErpException {
        return Jdbc.queryList(SELECT + "WHERE r.id_materia = ? AND r.año_academico = ?",
                AcademicRecordDaoImpl::map, subjectId, academicYear);
    }

    @Override
    public TableData findHistory(int studentId) throws ErpException {
        return Jdbc.queryTable("SELECT r.año_academico AS Año, g.nombre_grado AS Grado, m.nombre_materia AS Materia, "
                + "r.periodo_academico AS Período, r.valor_nota AS Nota, "
                + "CONCAT_WS(' ', p.primer_nombre, p.primer_apellido) AS Profesor, r.fecha_registro AS Fecha "
                + "FROM registros_academicos r JOIN materias m ON m.id_materia = r.id_materia "
                + "JOIN grados g ON g.id_grado = m.id_grado JOIN profesores p ON p.id_profesor = r.id_profesor "
                + "WHERE r.id_estudiante = ? "
                + "ORDER BY r.año_academico, m.nombre_materia, FIELD(r.periodo_academico, 'I', 'II', 'III', 'IV', 'Final')",
                studentId);
    }
}
