package com.littlehorizons.erp.dao.impl;

import com.littlehorizons.erp.dao.SubjectDao;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.Jdbc;
import com.littlehorizons.erp.util.TableData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class SubjectDaoImpl implements SubjectDao {

    private static Subject map(ResultSet rs) throws SQLException {
        Subject s = new Subject();
        s.setId(rs.getInt("id_materia"));
        s.setCode(rs.getString("codigo_materia"));
        s.setName(rs.getString("nombre_materia"));
        s.setDescription(rs.getString("descripcion"));
        s.setGradeId(rs.getInt("id_grado"));
        s.setWeeklyHours(rs.getInt("horas_semanales"));
        s.setActive(rs.getBoolean("esta_activa"));
        return s;
    }

    @Override
    public int insert(Subject s) throws ErpException {
        return (int) Jdbc.insert("INSERT INTO materias (codigo_materia, nombre_materia, descripcion, id_grado, "
                + "horas_semanales, esta_activa) VALUES (?, ?, ?, ?, ?, ?)",
                s.getCode(), s.getName(), s.getDescription(), s.getGradeId(), s.getWeeklyHours(), s.isActive());
    }

    @Override
    public Optional<Subject> findById(int id) throws ErpException {
        return Jdbc.queryOne("SELECT * FROM materias WHERE id_materia = ?", SubjectDaoImpl::map, id);
    }

    @Override
    public List<Subject> findByGrade(int gradeId) throws ErpException {
        return Jdbc.queryList("SELECT * FROM materias WHERE id_grado = ? AND esta_activa = TRUE ORDER BY nombre_materia",
                SubjectDaoImpl::map, gradeId);
    }

    @Override
    public TableData findAllSummary(Integer gradeId) throws ErpException {
        String sql = "SELECT m.id_materia AS ID, m.codigo_materia AS Código, m.nombre_materia AS Materia, "
                + "g.nombre_grado AS Grado, m.horas_semanales AS Horas, m.esta_activa AS Activa "
                + "FROM materias m JOIN grados g ON g.id_grado = m.id_grado ";
        if (gradeId == null) {
            return Jdbc.queryTable(sql + "ORDER BY g.nivel_numerico, m.nombre_materia");
        }
        return Jdbc.queryTable(sql + "WHERE m.id_grado = ? ORDER BY m.nombre_materia", gradeId);
    }
}
