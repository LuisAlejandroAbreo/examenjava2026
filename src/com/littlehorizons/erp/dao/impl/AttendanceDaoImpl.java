package com.littlehorizons.erp.dao.impl;

import com.littlehorizons.erp.dao.AttendanceDao;
import com.littlehorizons.erp.model.Attendance;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.Jdbc;
import com.littlehorizons.erp.util.TableData;
import java.time.LocalDate;

public class AttendanceDaoImpl implements AttendanceDao {

    @Override
    public void save(Attendance a) throws ErpException {
        Jdbc.update("INSERT INTO asistencias (id_estudiante, id_materia, fecha_asistencia, estado_asistencia, "
                + "observaciones, registrado_por) VALUES (?, ?, ?, ?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE estado_asistencia = VALUES(estado_asistencia), "
                + "observaciones = VALUES(observaciones), registrado_por = VALUES(registrado_por)",
                a.getStudentId(), a.getSubjectId(), a.getDate(), a.getStatus(), a.getNotes(), a.getRecordedBy());
    }

    @Override
    public TableData findByStudent(int studentId, LocalDate from, LocalDate to) throws ErpException {
        return Jdbc.queryTable("SELECT a.fecha_asistencia AS Fecha, m.nombre_materia AS Materia, "
                + "a.estado_asistencia AS Estado, a.observaciones AS Observaciones "
                + "FROM asistencias a JOIN materias m ON m.id_materia = a.id_materia "
                + "WHERE a.id_estudiante = ? AND a.fecha_asistencia BETWEEN ? AND ? "
                + "ORDER BY a.fecha_asistencia DESC, m.nombre_materia", studentId, from, to);
    }
}
