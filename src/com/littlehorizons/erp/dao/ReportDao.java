package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.TableData;
import java.time.LocalDate;

public interface ReportDao {

    /** Students with more than {@code maxAbsences} 'Ausente' marks in the same week since {@code from}. */
    TableData findAbsencePatterns(LocalDate from, int maxAbsences) throws ErpException;

    TableData findEventParticipation() throws ErpException;

    TableData findStudentsByGrade(int academicYear) throws ErpException;

    TableData findClubOccupancy() throws ErpException;

    TableData findTopStudents(int academicYear, int limit) throws ErpException;
}
