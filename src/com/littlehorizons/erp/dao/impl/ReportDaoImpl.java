package com.littlehorizons.erp.dao.impl;

import com.littlehorizons.erp.dao.ReportDao;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.Jdbc;
import com.littlehorizons.erp.util.TableData;
import java.time.LocalDate;

public class ReportDaoImpl implements ReportDao {

    @Override
    public TableData findAbsencePatterns(LocalDate from, int maxAbsences) throws ErpException {
        return Jdbc.queryTable("SELECT e.id_estudiante AS ID, "
                + "CONCAT_WS(' ', e.primer_nombre, e.primer_apellido, e.segundo_apellido) AS Estudiante, "
                + "MIN(a.fecha_asistencia) AS `Primera ausencia de la semana`, COUNT(*) AS Ausencias "
                + "FROM asistencias a JOIN estudiantes e ON e.id_estudiante = a.id_estudiante "
                + "WHERE a.estado_asistencia = 'Ausente' AND a.fecha_asistencia >= ? "
                + "GROUP BY e.id_estudiante, e.primer_nombre, e.primer_apellido, e.segundo_apellido, "
                + "YEARWEEK(a.fecha_asistencia, 1) HAVING COUNT(*) > ? "
                + "ORDER BY Ausencias DESC, Estudiante", from, maxAbsences);
    }

    @Override
    public TableData findEventParticipation() throws ErpException {
        return Jdbc.queryTable("SELECT ev.nombre_evento AS Evento, ev.tipo_evento AS Tipo, ev.fecha_evento AS Fecha, "
                + "COUNT(pe.id_participacion) AS Participantes, "
                + "SUM(pe.posicion_obtenida = '1er Lugar') AS Ganadores "
                + "FROM eventos ev LEFT JOIN participantes_evento pe ON pe.id_evento = ev.id_evento "
                + "WHERE ev.esta_activo = TRUE GROUP BY ev.id_evento, ev.nombre_evento, ev.tipo_evento, ev.fecha_evento "
                + "ORDER BY ev.fecha_evento");
    }

    @Override
    public TableData findStudentsByGrade(int academicYear) throws ErpException {
        return Jdbc.queryTable("SELECT g.nombre_grado AS Grado, COUNT(m.id_matricula) AS Estudiantes "
                + "FROM grados g LEFT JOIN matriculas m ON m.id_grado = g.id_grado AND m.año_academico = ? "
                + "AND m.estado_matricula = 'Activa' GROUP BY g.id_grado, g.nombre_grado, g.nivel_numerico "
                + "ORDER BY g.nivel_numerico", academicYear);
    }

    @Override
    public TableData findClubOccupancy() throws ErpException {
        return Jdbc.queryTable("SELECT c.nombre_club AS Club, c.capacidad_maxima AS Cupo, "
                + "COUNT(mc.id_membresia) AS Inscritos, c.capacidad_maxima - COUNT(mc.id_membresia) AS Disponibles "
                + "FROM clubes c LEFT JOIN membresias_club mc ON mc.id_club = c.id_club "
                + "AND mc.estado_membresia = 'Activa' WHERE c.esta_activo = TRUE "
                + "GROUP BY c.id_club, c.nombre_club, c.capacidad_maxima ORDER BY Disponibles, c.nombre_club");
    }

    @Override
    public TableData findTopStudents(int academicYear, int limit) throws ErpException {
        return Jdbc.queryTable("SELECT e.id_estudiante AS ID, "
                + "CONCAT_WS(' ', e.primer_nombre, e.primer_apellido) AS Estudiante, g.nombre_grado AS Grado, "
                + "ROUND(AVG(r.valor_nota), 2) AS Promedio "
                + "FROM registros_academicos r JOIN estudiantes e ON e.id_estudiante = r.id_estudiante "
                + "JOIN materias m ON m.id_materia = r.id_materia JOIN grados g ON g.id_grado = m.id_grado "
                + "WHERE r.año_academico = ? AND r.periodo_academico <> 'Final' "
                + "GROUP BY e.id_estudiante, e.primer_nombre, e.primer_apellido, g.nombre_grado "
                + "ORDER BY Promedio DESC, Estudiante LIMIT ?", academicYear, limit);
    }
}
