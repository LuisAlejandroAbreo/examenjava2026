package com.littlehorizons.erp.dao.impl;

import com.littlehorizons.erp.dao.ResourceDao;
import com.littlehorizons.erp.model.Resource;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.Jdbc;
import com.littlehorizons.erp.util.TableData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ResourceDaoImpl implements ResourceDao {

    private static Resource map(ResultSet rs) throws SQLException {
        Resource r = new Resource();
        r.setId(rs.getInt("id_recurso"));
        r.setCode(rs.getString("codigo_recurso"));
        r.setName(rs.getString("nombre_recurso"));
        r.setCategoryId(rs.getInt("id_categoria"));
        r.setDescription(rs.getString("descripcion"));
        r.setAcquisitionDate(Jdbc.toLocalDate(rs, "fecha_adquisicion"));
        r.setLocation(rs.getString("ubicacion_fisica"));
        r.setStatus(rs.getString("estado_recurso"));
        return r;
    }

    @Override
    public int insert(Resource r) throws ErpException {
        return (int) Jdbc.insert("INSERT INTO recursos (codigo_recurso, nombre_recurso, id_categoria, descripcion, "
                + "fecha_adquisicion, ubicacion_fisica, estado_recurso) VALUES (?, ?, ?, ?, ?, ?, ?)",
                r.getCode(), r.getName(), r.getCategoryId(), r.getDescription(), r.getAcquisitionDate(),
                r.getLocation(), r.getStatus());
    }

    @Override
    public Optional<Resource> findById(int id) throws ErpException {
        return Jdbc.queryOne("SELECT * FROM recursos WHERE id_recurso = ?", ResourceDaoImpl::map, id);
    }

    @Override
    public Optional<Resource> findByCode(String code) throws ErpException {
        return Jdbc.queryOne("SELECT * FROM recursos WHERE codigo_recurso = ?", ResourceDaoImpl::map, code);
    }

    @Override
    public TableData findCategories() throws ErpException {
        return Jdbc.queryTable("SELECT id_categoria AS ID, nombre_categoria AS Categoría FROM categorias_recurso "
                + "ORDER BY id_categoria");
    }

    @Override
    public boolean categoryExists(int categoryId) throws ErpException {
        return Jdbc.queryLong("SELECT COUNT(*) FROM categorias_recurso WHERE id_categoria = ?", categoryId) > 0;
    }

    @Override
    public TableData findAllSummary(Integer categoryId, String status) throws ErpException {
        StringBuilder sql = new StringBuilder("SELECT r.id_recurso AS ID, r.codigo_recurso AS Código, "
                + "r.nombre_recurso AS Recurso, c.nombre_categoria AS Categoría, r.ubicacion_fisica AS Ubicación, "
                + "r.estado_recurso AS Estado FROM recursos r JOIN categorias_recurso c "
                + "ON c.id_categoria = r.id_categoria WHERE 1 = 1 ");
        List<Object> params = new ArrayList<>();
        if (categoryId != null) {
            sql.append("AND r.id_categoria = ? ");
            params.add(categoryId);
        }
        if (status != null) {
            sql.append("AND r.estado_recurso = ? ");
            params.add(status);
        }
        sql.append("ORDER BY r.codigo_recurso");
        return Jdbc.queryTable(sql.toString(), params.toArray());
    }

    @Override
    public void updateStatus(int id, String status) throws ErpException {
        Jdbc.update("UPDATE recursos SET estado_recurso = ? WHERE id_recurso = ?", status, id);
    }
}
