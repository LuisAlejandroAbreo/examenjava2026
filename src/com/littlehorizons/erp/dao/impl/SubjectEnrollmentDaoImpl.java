package com.littlehorizons.erp.dao.impl;

import com.littlehorizons.erp.dao.SubjectEnrollmentDao;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.Jdbc;
import com.littlehorizons.erp.util.TableData;
import java.time.LocalDate;

public class SubjectEnrollmentDaoImpl implements SubjectEnrollmentDao {

    @Override
    public void insert(int studentId, int subjectId, int academicYear, String period, String section, LocalDate date)
            throws ErpException {
        Jdbc.insert("INSERT INTO inscripciones_materia (id_estudiante, id_materia, año_academico, periodo_academico, "
                + "seccion, fecha_inscripcion, estado) VALUES (?, ?, ?, ?, ?, ?, 'Activa')",
                studentId, subjectId, academicYear, period, section, date);
    }

    @Override
    public boolean exists(int studentId, int subjectId, int academicYear, String period) throws ErpException {
        return Jdbc.queryLong("SELECT COUNT(*) FROM inscripciones_materia WHERE id_estudiante = ? AND id_materia = ? "
                + "AND año_academico = ? AND periodo_academico = ?", studentId, subjectId, academicYear, period) > 0;
    }

    @Override
    public boolean existsInYear(int studentId, int subjectId, int academicYear) throws ErpException {
        return Jdbc.queryLong("SELECT COUNT(*) FROM inscripciones_materia WHERE id_estudiante = ? AND id_materia = ? "
                + "AND año_academico = ?", studentId, subjectId, academicYear) > 0;
    }

    @Override
    public TableData findByStudent(int studentId, int academicYear) throws ErpException {
        return Jdbc.queryTable("SELECT i.periodo_academico AS Período, m.codigo_materia AS Código, "
                + "m.nombre_materia AS Materia, i.seccion AS Sección, i.estado AS Estado "
                + "FROM inscripciones_materia i JOIN materias m ON m.id_materia = i.id_materia "
                + "WHERE i.id_estudiante = ? AND i.año_academico = ? ORDER BY i.periodo_academico, m.nombre_materia",
                studentId, academicYear);
    }
}
