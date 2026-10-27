package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.TableData;
import java.time.LocalDate;

/** Enrollment of students in subjects (table inscripciones_materia). */
public interface SubjectEnrollmentDao {

    void insert(int studentId, int subjectId, int academicYear, String period, String section, LocalDate date)
            throws ErpException;

    boolean exists(int studentId, int subjectId, int academicYear, String period) throws ErpException;

    /** True when the student is enrolled in the subject in any period of the year. */
    boolean existsInYear(int studentId, int subjectId, int academicYear) throws ErpException;

    TableData findByStudent(int studentId, int academicYear) throws ErpException;
}
