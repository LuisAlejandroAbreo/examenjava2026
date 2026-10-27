package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Resource;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.TableData;
import java.util.Optional;

public interface ResourceDao {

    int insert(Resource resource) throws ErpException;

    Optional<Resource> findById(int id) throws ErpException;

    Optional<Resource> findByCode(String code) throws ErpException;

    TableData findCategories() throws ErpException;

    boolean categoryExists(int categoryId) throws ErpException;

    TableData findAllSummary(Integer categoryId, String status) throws ErpException;

    void updateStatus(int id, String status) throws ErpException;
}
