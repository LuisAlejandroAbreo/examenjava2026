package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.TableData;
import java.util.List;
import java.util.Optional;

public interface SubjectDao {

    int insert(Subject subject) throws ErpException;

    Optional<Subject> findById(int id) throws ErpException;

    List<Subject> findByGrade(int gradeId) throws ErpException;

    TableData findAllSummary(Integer gradeId) throws ErpException;
}
