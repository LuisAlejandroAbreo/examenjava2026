package com.littlehorizons.erp.dao.impl;

import com.littlehorizons.erp.dao.ClubDao;
import com.littlehorizons.erp.model.Club;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.Jdbc;
import com.littlehorizons.erp.util.TableData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.Optional;

public class ClubDaoImpl implements ClubDao {

    private static Club map(ResultSet rs) throws SQLException {
        Club c = new Club();
        c.setId(rs.getInt("id_club"));
        c.setName(rs.getString("nombre_club"));
        c.setDescription(rs.getString("descripcion"));
        c.setSupervisorId(Jdbc.getInteger(rs, "id_profesor_supervisor"));
        c.setMaxCapacity(rs.getInt("capacidad_maxima"));
        c.setMeetingSchedule(rs.getString("horario_reunion"));
        c.setActive(rs.getBoolean("esta_activo"));
        return c;
    }

    @Override
    public int insert(Club c) throws ErpException {
        return (int) Jdbc.insert("INSERT INTO clubes (nombre_club, descripcion, id_profesor_supervisor, "
                + "capacidad_maxima, horario_reunion, esta_activo) VALUES (?, ?, ?, ?, ?, ?)",
                c.getName(), c.getDescription(), c.getSupervisorId(), c.getMaxCapacity(), c.getMeetingSchedule(),
                c.isActive());
    }

    @Override
    public Optional<Club> findById(int id) throws ErpException {
        return Jdbc.queryOne("SELECT * FROM clubes WHERE id_club = ?", ClubDaoImpl::map, id);
    }

    @Override
    public TableData findAllSummary() throws ErpException {
        return Jdbc.queryTable("SELECT c.id_club AS ID, c.nombre_club AS Club, "
                + "CONCAT_WS(' ', p.primer_nombre, p.primer_apellido) AS Supervisor, c.horario_reunion AS Horario, "
                + "(SELECT COUNT(*) FROM membresias_club mc WHERE mc.id_club = c.id_club "
                + "AND mc.estado_membresia = 'Activa') AS Inscritos, c.capacidad_maxima AS Cupo "
                + "FROM clubes c LEFT JOIN profesores p ON p.id_profesor = c.id_profesor_supervisor "
                + "WHERE c.esta_activo = TRUE ORDER BY c.id_club");
    }

    @Override
    public void addMember(int clubId, int studentId, LocalDate date) throws ErpException {
        Jdbc.inTransaction(connection -> {
            Optional<Integer> capacity = Jdbc.queryOne(connection,
                    "SELECT capacidad_maxima FROM clubes WHERE id_club = ? AND esta_activo = TRUE FOR UPDATE",
                    rs -> rs.getInt(1), clubId);
            if (!capacity.isPresent()) {
                throw new ErpException("El club no existe o no está activo.");
            }
            long active = Jdbc.queryLong(connection,
                    "SELECT COUNT(*) FROM membresias_club WHERE id_club = ? AND estado_membresia = 'Activa'", clubId);
            if (active >= capacity.get()) {
                throw new ErpException("El club alcanzó su cupo máximo (" + capacity.get() + " estudiantes).");
            }
            long already = Jdbc.queryLong(connection, "SELECT COUNT(*) FROM membresias_club WHERE id_club = ? "
                    + "AND id_estudiante = ? AND estado_membresia = 'Activa'", clubId, studentId);
            if (already > 0) {
                throw new ErpException("El estudiante ya es miembro activo de este club.");
            }
            Jdbc.insert(connection, "INSERT INTO membresias_club (id_estudiante, id_club, fecha_ingreso, "
                    + "estado_membresia) VALUES (?, ?, ?, 'Activa')", studentId, clubId, date);
            return null;
        });
    }

    @Override
    public void withdrawMember(int clubId, int studentId, LocalDate date, String reason) throws ErpException {
        int rows = Jdbc.update("UPDATE membresias_club SET estado_membresia = 'Retirada', fecha_retiro = ?, "
                + "motivo_retiro = ? WHERE id_club = ? AND id_estudiante = ? AND estado_membresia = 'Activa'",
                date, reason, clubId, studentId);
        if (rows == 0) {
            throw new ErpException("El estudiante no tiene una membresía activa en ese club.");
        }
    }

    @Override
    public TableData findMembers(int clubId) throws ErpException {
        return Jdbc.queryTable("SELECT e.id_estudiante AS ID, "
                + "CONCAT_WS(' ', e.primer_nombre, e.segundo_nombre, e.primer_apellido, e.segundo_apellido) AS Estudiante, "
                + "mc.fecha_ingreso AS Ingreso, mc.estado_membresia AS Estado, mc.fecha_retiro AS Retiro "
                + "FROM membresias_club mc JOIN estudiantes e ON e.id_estudiante = mc.id_estudiante "
                + "WHERE mc.id_club = ? ORDER BY mc.estado_membresia, e.primer_apellido", clubId);
    }
}
