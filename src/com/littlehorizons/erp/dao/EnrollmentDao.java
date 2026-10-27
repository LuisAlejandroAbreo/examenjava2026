package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Enrollment;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.TableData;
import java.util.Optional;

public interface EnrollmentDao {

    int insert(Enrollment enrollment) throws ErpException;

    Optional<Enrollment> findByStudentAndYear(int studentId, int academicYear) throws ErpException;

    TableData findByStudent(int studentId) throws ErpException;
}
