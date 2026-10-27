package com.littlehorizons.erp.dao.impl;

import com.littlehorizons.erp.dao.LoanDao;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.Jdbc;
import com.littlehorizons.erp.util.TableData;
import java.time.LocalDate;
import java.util.Optional;

public class LoanDaoImpl implements LoanDao {

    private static String table(String borrowerType) {
        return STUDENT.equals(borrowerType) ? "prestamos_estudiante" : "prestamos_profesor";
    }

    @Override
    public int lend(int resourceId, String borrowerType, int borrowerId, LocalDate loanDate, LocalDate dueDate,
            String notes) throws ErpException {
        final boolean student = STUDENT.equals(borrowerType);
        return Jdbc.inTransaction(connection -> {
            Optional<String> status = Jdbc.queryOne(connection,
                    "SELECT estado_recurso FROM recursos WHERE id_recurso = ? FOR UPDATE",
                    rs -> rs.getString(1), resourceId);
            if (!status.isPresent()) {
                throw new ErpException("El recurso no existe.");
            }
            if (!"Disponible".equals(status.get())) {
                throw new ErpException("El recurso no está disponible (estado actual: " + status.get() + ").");
            }
            String sql = "INSERT INTO " + (student ? "prestamos_estudiante (id_recurso, id_estudiante"
                    : "prestamos_profesor (id_recurso, id_profesor")
                    + ", fecha_prestamo, fecha_devolucion_esperada, estado_prestamo, observaciones) "
                    + "VALUES (?, ?, ?, ?, 'Activo', ?)";
            long id = Jdbc.insert(connection, sql, resourceId, borrowerId, loanDate, dueDate, notes);
            Jdbc.update(connection, "UPDATE recursos SET estado_recurso = 'Prestado' WHERE id_recurso = ?",
                    resourceId);
            return (int) id;
        });
    }

    @Override
    public void giveBack(String borrowerType, int loanId, LocalDate returnDate) throws ErpException {
        final String table = table(borrowerType);
        Jdbc.inTransaction(connection -> {
            Optional<Object[]> loan = Jdbc.queryOne(connection,
                    "SELECT id_recurso, fecha_prestamo FROM " + table
                    + " WHERE id_prestamo = ? AND fecha_devolucion_real IS NULL FOR UPDATE",
                    rs -> new Object[] {rs.getInt(1), rs.getDate(2).toLocalDate()}, loanId);
            if (!loan.isPresent()) {
                throw new ErpException("No existe un préstamo pendiente de devolución con ese ID.");
            }
            LocalDate loanDate = (LocalDate) loan.get()[1];
            if (returnDate.isBefore(loanDate)) {
                throw new ErpException("La fecha de devolución no puede ser anterior a la del préstamo (" + loanDate + ").");
            }
            Jdbc.update(connection, "UPDATE " + table
                    + " SET fecha_devolucion_real = ?, estado_prestamo = 'Devuelto' WHERE id_prestamo = ?",
                    returnDate, loanId);
            Jdbc.update(connection, "UPDATE recursos SET estado_recurso = 'Disponible' "
                    + "WHERE id_recurso = ? AND estado_recurso = 'Prestado'", loan.get()[0]);
            return null;
        });
    }

    @Override
    public int refreshOverdue(LocalDate today) throws ErpException {
        int students = Jdbc.update("UPDATE prestamos_estudiante SET estado_prestamo = 'Vencido' "
                + "WHERE estado_prestamo = 'Activo' AND fecha_devolucion_real IS NULL "
                + "AND fecha_devolucion_esperada < ?", today);
        int teachers = Jdbc.update("UPDATE prestamos_profesor SET estado_prestamo = 'Vencido' "
                + "WHERE estado_prestamo = 'Activo' AND fecha_devolucion_real IS NULL "
                + "AND fecha_devolucion_esperada < ?", today);
        return students + teachers;
    }

    @Override
    public TableData findOpenLoans() throws ErpException {
        return Jdbc.queryTable("SELECT 'Estudiante' AS Tipo, p.id_prestamo AS `ID préstamo`, "
                + "r.codigo_recurso AS Código, r.nombre_recurso AS Recurso, "
                + "CONCAT_WS(' ', e.primer_nombre, e.primer_apellido) AS Prestatario, "
                + "p.fecha_prestamo AS Préstamo, p.fecha_devolucion_esperada AS `Devolución esperada`, "
                + "p.estado_prestamo AS Estado "
                + "FROM prestamos_estudiante p JOIN recursos r ON r.id_recurso = p.id_recurso "
                + "JOIN estudiantes e ON e.id_estudiante = p.id_estudiante WHERE p.fecha_devolucion_real IS NULL "
                + "UNION ALL "
                + "SELECT 'Profesor', p.id_prestamo, r.codigo_recurso, r.nombre_recurso, "
                + "CONCAT_WS(' ', t.primer_nombre, t.primer_apellido), p.fecha_prestamo, "
                + "p.fecha_devolucion_esperada, p.estado_prestamo "
                + "FROM prestamos_profesor p JOIN recursos r ON r.id_recurso = p.id_recurso "
                + "JOIN profesores t ON t.id_profesor = p.id_profesor WHERE p.fecha_devolucion_real IS NULL "
                + "ORDER BY 7");
    }

    @Override
    public TableData findOverdue(LocalDate today) throws ErpException {
        return Jdbc.queryTable("SELECT 'Estudiante' AS Tipo, p.id_prestamo AS `ID préstamo`, "
                + "r.codigo_recurso AS Código, r.nombre_recurso AS Recurso, "
                + "CONCAT_WS(' ', e.primer_nombre, e.primer_apellido) AS Prestatario, "
                + "p.fecha_devolucion_esperada AS `Debía devolver`, "
                + "DATEDIFF(?, p.fecha_devolucion_esperada) AS `Días de retraso` "
                + "FROM prestamos_estudiante p JOIN recursos r ON r.id_recurso = p.id_recurso "
                + "JOIN estudiantes e ON e.id_estudiante = p.id_estudiante "
                + "WHERE p.fecha_devolucion_real IS NULL AND p.fecha_devolucion_esperada < ? "
                + "UNION ALL "
                + "SELECT 'Profesor', p.id_prestamo, r.codigo_recurso, r.nombre_recurso, "
                + "CONCAT_WS(' ', t.primer_nombre, t.primer_apellido), p.fecha_devolucion_esperada, "
                + "DATEDIFF(?, p.fecha_devolucion_esperada) "
                + "FROM prestamos_profesor p JOIN recursos r ON r.id_recurso = p.id_recurso "
                + "JOIN profesores t ON t.id_profesor = p.id_profesor "
                + "WHERE p.fecha_devolucion_real IS NULL AND p.fecha_devolucion_esperada < ? "
                + "ORDER BY 7 DESC", today, today, today, today);
    }
}
