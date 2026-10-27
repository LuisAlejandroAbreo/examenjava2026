package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.TeacherAssignment;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.TableData;
import java.time.LocalTime;
import java.util.Optional;

/** Teacher workload (subject assignments) and weekly class schedule. */
public interface AssignmentDao {

    int insert(int teacherId, int subjectId, int academicYear, String section) throws ErpException;

    Optional<TeacherAssignment> findById(int assignmentId) throws ErpException;

    boolean exists(int teacherId, int subjectId, int academicYear, String section) throws ErpException;

    boolean teacherIsAssigned(int teacherId, int subjectId, int academicYear) throws ErpException;

    TableData findByTeacher(int teacherId, Integer academicYear) throws ErpException;

    boolean hasTeacherConflict(int teacherId, int academicYear, String day, LocalTime start, LocalTime end)
            throws ErpException;

    boolean hasGroupConflict(int gradeId, int academicYear, String section, String day, LocalTime start,
            LocalTime end) throws ErpException;

    void addSchedule(int assignmentId, String day, LocalTime start, LocalTime end) throws ErpException;

    TableData findScheduleByTeacher(int teacherId, int academicYear) throws ErpException;
}
