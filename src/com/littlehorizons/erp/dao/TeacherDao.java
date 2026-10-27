package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.TableData;
import java.util.Optional;

public interface TeacherDao {

    int insert(Teacher teacher) throws ErpException;

    Optional<Teacher> findById(int id) throws ErpException;

    Optional<Teacher> findByDocument(String documentNumber) throws ErpException;

    TableData findAllSummary() throws ErpException;
}
