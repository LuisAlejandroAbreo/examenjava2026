package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Attendance;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.TableData;
import java.time.LocalDate;

public interface AttendanceDao {

    /** Inserts or updates the attendance mark for student + subject + date. */
    void save(Attendance attendance) throws ErpException;

    TableData findByStudent(int studentId, LocalDate from, LocalDate to) throws ErpException;
}
