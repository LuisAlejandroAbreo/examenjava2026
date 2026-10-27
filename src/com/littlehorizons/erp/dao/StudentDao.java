package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.TableData;
import java.util.Optional;

public interface StudentDao {

    int insert(Student student) throws ErpException;

    Optional<Student> findById(int id) throws ErpException;

    Optional<Student> findByDocument(String documentNumber) throws ErpException;

    TableData findAllSummary() throws ErpException;

    TableData search(String text) throws ErpException;

    TableData findByGuardian(int guardianId) throws ErpException;

    void updateStatus(int id, String status) throws ErpException;
}
