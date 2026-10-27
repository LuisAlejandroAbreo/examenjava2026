package com.littlehorizons.erp.dao.impl;

import com.littlehorizons.erp.dao.EnrollmentDao;
import com.littlehorizons.erp.model.Enrollment;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.Jdbc;
import com.littlehorizons.erp.util.TableData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class EnrollmentDaoImpl implements EnrollmentDao {

    private static Enrollment map(ResultSet rs) throws SQLException {
        Enrollment e = new Enrollment();
        e.setId(rs.getInt("id_matricula"));
        e.setStudentId(rs.getInt("id_estudiante"));
        e.setGradeId(rs.getInt("id_grado"));
        e.setAcademicYear(rs.getInt("año_academico"));
        e.setStatus(rs.getString("estado_matricula"));
        e.setEnrollmentDate(Jdbc.toLocalDate(rs, "fecha_matricula"));
        e.setWithdrawalDate(Jdbc.toLocalDate(rs, "fecha_retiro"));
        e.setNotes(rs.getString("observaciones"));
        return e;
    }

    @Override
    public int insert(Enrollment e) throws ErpException {
        return (int) Jdbc.insert("INSERT INTO matriculas (id_estudiante, id_grado, año_academico, estado_matricula, "
                + "fecha_matricula, observaciones) VALUES (?, ?, ?, ?, ?, ?)",
                e.getStudentId(), e.getGradeId(), e.getAcademicYear(), e.getStatus(), e.getEnrollmentDate(),
                e.getNotes());
    }

    @Override
    public Optional<Enrollment> findByStudentAndYear(int studentId, int academicYear) throws ErpException {
        return Jdbc.queryOne("SELECT * FROM matriculas WHERE id_estudiante = ? AND año_academico = ?",
                EnrollmentDaoImpl::map, studentId, academicYear);
    }

    @Override
    public TableData findByStudent(int studentId) throws ErpException {
        return Jdbc.queryTable("SELECT m.año_academico AS Año, g.nombre_grado AS Grado, m.estado_matricula AS Estado, "
                + "m.fecha_matricula AS `Fecha matrícula`, m.fecha_retiro AS `Fecha retiro` "
                + "FROM matriculas m JOIN grados g ON g.id_grado = m.id_grado "
                + "WHERE m.id_estudiante = ? ORDER BY m.año_academico DESC", studentId);
    }
}
