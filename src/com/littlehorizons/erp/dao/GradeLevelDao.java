package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.GradeLevel;
import com.littlehorizons.erp.util.ErpException;
import java.util.List;
import java.util.Optional;

public interface GradeLevelDao {

    List<GradeLevel> findAll() throws ErpException;

    Optional<GradeLevel> findById(int id) throws ErpException;
}
