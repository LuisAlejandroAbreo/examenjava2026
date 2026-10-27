package com.littlehorizons.erp.dao.impl;

import com.littlehorizons.erp.dao.AssignmentDao;
import com.littlehorizons.erp.model.TeacherAssignment;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.Jdbc;
import com.littlehorizons.erp.util.TableData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.Optional;

public class AssignmentDaoImpl implements AssignmentDao {

    private static TeacherAssignment map(ResultSet rs) throws SQLException {
        TeacherAssignment a = new TeacherAssignment();
        a.setId(rs.getInt("id_asignacion"));
        a.setTeacherId(rs.getInt("id_profesor"));
        a.setSubjectId(rs.getInt("id_materia"));
        a.setGradeId(rs.getInt("id_grado"));
        a.setAcademicYear(rs.getInt("año_academico"));
        a.setSection(rs.getString("seccion"));
        return a;
    }

    @Override
    public int insert(int teacherId, int subjectId, int academicYear, String section) throws ErpException {
        return (int) Jdbc.insert("INSERT INTO asignaciones_profesor (id_profesor, id_materia, año_academico, seccion) "
                + "VALUES (?, ?, ?, ?)", teacherId, subjectId, academicYear, section);
    }

    @Override
    public Optional<TeacherAssignment> findById(int assignmentId) throws ErpException {
        return Jdbc.queryOne("SELECT a.*, m.id_grado FROM asignaciones_profesor a "
                + "JOIN materias m ON m.id_materia = a.id_materia WHERE a.id_asignacion = ?",
                AssignmentDaoImpl::map, assignmentId);
    }

    @Override
    public boolean exists(int teacherId, int subjectId, int academicYear, String section) throws ErpException {
        return Jdbc.queryLong("SELECT COUNT(*) FROM asignaciones_profesor WHERE id_profesor = ? AND id_materia = ? "
                + "AND año_academico = ? AND COALESCE(seccion, '') = COALESCE(?, '')",
                teacherId, subjectId, academicYear, section) > 0;
    }

    @Override
    public boolean teacherIsAssigned(int teacherId, int subjectId, int academicYear) throws ErpException {
        return Jdbc.queryLong("SELECT COUNT(*) FROM asignaciones_profesor WHERE id_profesor = ? AND id_materia = ? "
                + "AND año_academico = ?", teacherId, subjectId, academicYear) > 0;
    }

    @Override
    public TableData findByTeacher(int teacherId, Integer academicYear) throws ErpException {
        String sql = "SELECT a.id_asignacion AS `ID asignación`, a.año_academico AS Año, m.codigo_materia AS Código, "
                + "m.nombre_materia AS Materia, g.nombre_grado AS Grado, a.seccion AS Sección, "
                + "m.horas_semanales AS Horas "
                + "FROM asignaciones_profesor a JOIN materias m ON m.id_materia = a.id_materia "
                + "JOIN grados g ON g.id_grado = m.id_grado WHERE a.id_profesor = ? ";
        if (academicYear == null) {
            return Jdbc.queryTable(sql + "ORDER BY a.año_academico DESC, g.nivel_numerico", teacherId);
        }
        return Jdbc.queryTable(sql + "AND a.año_academico = ? ORDER BY g.nivel_numerico", teacherId, academicYear);
    }

    @Override
    public boolean hasTeacherConflict(int teacherId, int academicYear, String day, LocalTime start, LocalTime end)
            throws ErpException {
        return Jdbc.queryLong("SELECT COUNT(*) FROM horarios_clase h JOIN asignaciones_profesor a "
                + "ON a.id_asignacion = h.id_asignacion WHERE a.id_profesor = ? AND a.año_academico = ? "
                + "AND h.dia_semana = ? AND h.hora_inicio < ? AND h.hora_fin > ?",
                teacherId, academicYear, day, end, start) > 0;
    }

    @Override
    public boolean hasGroupConflict(int gradeId, int academicYear, String section, String day, LocalTime start,
            LocalTime end) throws ErpException {
        return Jdbc.queryLong("SELECT COUNT(*) FROM horarios_clase h JOIN asignaciones_profesor a "
                + "ON a.id_asignacion = h.id_asignacion JOIN materias m ON m.id_materia = a.id_materia "
                + "WHERE m.id_grado = ? AND a.año_academico = ? AND COALESCE(a.seccion, '') = COALESCE(?, '') "
                + "AND h.dia_semana = ? AND h.hora_inicio < ? AND h.hora_fin > ?",
                gradeId, academicYear, section, day, end, start) > 0;
    }

    @Override
    public void addSchedule(int assignmentId, String day, LocalTime start, LocalTime end) throws ErpException {
        Jdbc.insert("INSERT INTO horarios_clase (id_asignacion, dia_semana, hora_inicio, hora_fin) VALUES (?, ?, ?, ?)",
                assignmentId, day, start, end);
    }

    @Override
    public TableData findScheduleByTeacher(int teacherId, int academicYear) throws ErpException {
        return Jdbc.queryTable("SELECT h.dia_semana AS Día, h.hora_inicio AS Inicio, h.hora_fin AS Fin, "
                + "m.nombre_materia AS Materia, g.nombre_grado AS Grado, a.seccion AS Sección "
                + "FROM horarios_clase h JOIN asignaciones_profesor a ON a.id_asignacion = h.id_asignacion "
                + "JOIN materias m ON m.id_materia = a.id_materia JOIN grados g ON g.id_grado = m.id_grado "
                + "WHERE a.id_profesor = ? AND a.año_academico = ? "
                + "ORDER BY FIELD(h.dia_semana, 'Lunes', 'Martes', 'Miércoles', 'Jueves', 'Viernes'), h.hora_inicio",
                teacherId, academicYear);
    }
}
