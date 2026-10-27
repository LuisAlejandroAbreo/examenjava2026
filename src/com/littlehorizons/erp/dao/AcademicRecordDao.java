package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.AcademicRecord;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.TableData;
import java.util.List;

public interface AcademicRecordDao {

    /** Inserts the grade or updates it when the student already has one for that subject/year/period. */
    void save(AcademicRecord record) throws ErpException;

    List<AcademicRecord> findByStudent(int studentId, int academicYear) throws ErpException;

    List<AcademicRecord> findBySubject(int subjectId, int academicYear) throws ErpException;

    TableData findHistory(int studentId) throws ErpException;
}
