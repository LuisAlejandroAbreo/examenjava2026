package com.littlehorizons.erp.dao.impl;

import com.littlehorizons.erp.dao.EventDao;
import com.littlehorizons.erp.model.SchoolEvent;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.Jdbc;
import com.littlehorizons.erp.util.TableData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

public class EventDaoImpl implements EventDao {

    private static SchoolEvent map(ResultSet rs) throws SQLException {
        SchoolEvent e = new SchoolEvent();
        e.setId(rs.getInt("id_evento"));
        e.setName(rs.getString("nombre_evento"));
        e.setDescription(rs.getString("descripcion"));
        e.setType(rs.getString("tipo_evento"));
        e.setEventDate(Jdbc.toLocalDate(rs, "fecha_evento"));
        e.setLocation(rs.getString("lugar"));
        e.setCompetitive(rs.getBoolean("tiene_competencia"));
        e.setOrganizerId(Jdbc.getInteger(rs, "id_organizador"));
        e.setActive(rs.getBoolean("esta_activo"));
        return e;
    }

    @Override
    public int insert(SchoolEvent e) throws ErpException {
        return (int) Jdbc.insert("INSERT INTO eventos (nombre_evento, descripcion, tipo_evento, fecha_evento, lugar, "
                + "tiene_competencia, id_organizador, esta_activo) VALUES (?, ?, ?, ?, ?, ?, ?, ?)",
                e.getName(), e.getDescription(), e.getType(), e.getEventDate(), e.getLocation(), e.isCompetitive(),
                e.getOrganizerId(), e.isActive());
    }

    @Override
    public Optional<SchoolEvent> findById(int id) throws ErpException {
        return Jdbc.queryOne("SELECT * FROM eventos WHERE id_evento = ?", EventDaoImpl::map, id);
    }

    @Override
    public TableData findAllSummary() throws ErpException {
        return Jdbc.queryTable("SELECT ev.id_evento AS ID, ev.nombre_evento AS Evento, ev.tipo_evento AS Tipo, "
                + "ev.fecha_evento AS Fecha, ev.lugar AS Lugar, ev.tiene_competencia AS Competencia, "
                + "(SELECT COUNT(*) FROM participantes_evento pe WHERE pe.id_evento = ev.id_evento) AS Participantes "
                + "FROM eventos ev WHERE ev.esta_activo = TRUE ORDER BY ev.fecha_evento");
    }

    @Override
    public boolean isParticipant(int eventId, int studentId) throws ErpException {
        return Jdbc.queryLong("SELECT COUNT(*) FROM participantes_evento WHERE id_evento = ? AND id_estudiante = ?",
                eventId, studentId) > 0;
    }

    @Override
    public void addParticipant(int eventId, int studentId, String role, String position) throws ErpException {
        Jdbc.insert("INSERT INTO participantes_evento (id_evento, id_estudiante, rol_participacion, "
                + "posicion_obtenida) VALUES (?, ?, ?, ?)", eventId, studentId, role, position);
    }

    @Override
    public int setPosition(int eventId, int studentId, String position) throws ErpException {
        return Jdbc.update("UPDATE participantes_evento SET posicion_obtenida = ? WHERE id_evento = ? "
                + "AND id_estudiante = ?", position, eventId, studentId);
    }

    @Override
    public TableData findParticipants(int eventId) throws ErpException {
        return Jdbc.queryTable("SELECT e.id_estudiante AS ID, "
                + "CONCAT_WS(' ', e.primer_nombre, e.segundo_nombre, e.primer_apellido, e.segundo_apellido) AS Estudiante, "
                + "pe.rol_participacion AS Rol, pe.posicion_obtenida AS Posición "
                + "FROM participantes_evento pe JOIN estudiantes e ON e.id_estudiante = pe.id_estudiante "
                + "WHERE pe.id_evento = ? ORDER BY FIELD(pe.posicion_obtenida, '1er Lugar', '2do Lugar', '3er Lugar'), "
                + "e.primer_apellido", eventId);
    }
}
