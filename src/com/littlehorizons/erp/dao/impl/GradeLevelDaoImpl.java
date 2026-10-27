package com.littlehorizons.erp.dao.impl;

import com.littlehorizons.erp.dao.GradeLevelDao;
import com.littlehorizons.erp.model.GradeLevel;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.Jdbc;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

public class GradeLevelDaoImpl implements GradeLevelDao {

    private static GradeLevel map(ResultSet rs) throws SQLException {
        GradeLevel g = new GradeLevel();
        g.setId(rs.getInt("id_grado"));
        g.setName(rs.getString("nombre_grado"));
        g.setNumericLevel(rs.getInt("nivel_numerico"));
        return g;
    }

    @Override
    public List<GradeLevel> findAll() throws ErpException {
        return Jdbc.queryList("SELECT * FROM grados ORDER BY nivel_numerico", GradeLevelDaoImpl::map);
    }

    @Override
    public Optional<GradeLevel> findById(int id) throws ErpException {
        return Jdbc.queryOne("SELECT * FROM grados WHERE id_grado = ?", GradeLevelDaoImpl::map, id);
    }
}
