package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.AcademicRecordDao;
import com.littlehorizons.erp.dao.AssignmentDao;
import com.littlehorizons.erp.dao.AttendanceDao;
import com.littlehorizons.erp.dao.EnrollmentDao;
import com.littlehorizons.erp.dao.GradeLevelDao;
import com.littlehorizons.erp.dao.StudentDao;
import com.littlehorizons.erp.dao.SubjectDao;
import com.littlehorizons.erp.dao.SubjectEnrollmentDao;
import com.littlehorizons.erp.dao.TeacherDao;
import com.littlehorizons.erp.model.AcademicRecord;
import com.littlehorizons.erp.model.Attendance;
import com.littlehorizons.erp.model.Enrollment;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.TableData;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

/** Business rules for subjects, enrollment in subjects, grades, averages and attendance. */
public class AcademicController {

    public static final List<String> ENROLLMENT_PERIODS = Arrays.asList("I", "II", "III", "IV");
    public static final List<String> GRADE_PERIODS = Arrays.asList("I", "II", "III", "IV", "Final");
    public static final List<String> ATTENDANCE_STATES = Arrays.asList("Presente", "Ausente", "Tardanza", "Excusa Médica");

    private final SubjectDao subjectDao;
    private final StudentDao studentDao;
    private final TeacherDao teacherDao;
    private final EnrollmentDao enrollmentDao;
    private final SubjectEnrollmentDao subjectEnrollmentDao;
    private final AcademicRecordDao recordDao;
    private final AttendanceDao attendanceDao;
    private final AssignmentDao assignmentDao;
    private final GradeLevelDao gradeDao;

    public AcademicController(SubjectDao subjectDao, StudentDao studentDao, TeacherDao teacherDao,
            EnrollmentDao enrollmentDao, SubjectEnrollmentDao subjectEnrollmentDao, AcademicRecordDao recordDao,
            AttendanceDao attendanceDao, AssignmentDao assignmentDao, GradeLevelDao gradeDao) {
        this.subjectDao = subjectDao;
        this.studentDao = studentDao;
        this.teacherDao = teacherDao;
        this.enrollmentDao = enrollmentDao;
        this.subjectEnrollmentDao = subjectEnrollmentDao;
        this.recordDao = recordDao;
        this.attendanceDao = attendanceDao;
        this.assignmentDao = assignmentDao;
        this.gradeDao = gradeDao;
    }

    // ---------- Subjects ----------

    public int createSubject(Subject subject) throws ErpException {
        if (!gradeDao.findById(subject.getGradeId()).isPresent()) {
            throw new ErpException("El grado indicado no existe.");
        }
        subject.setActive(true);
        return subjectDao.insert(subject);
    }

    public TableData listSubjects(Integer gradeId) throws ErpException {
        return subjectDao.findAllSummary(gradeId);
    }

    // ---------- Subject enrollment ----------

    public void enrollInSubject(int studentId, int subjectId, int year, String period, String section)
            throws ErpException {
        Enrollment enrollment = requireActiveEnrollment(studentId, year);
        Subject subject = subjectDao.findById(subjectId)
                .orElseThrow(() -> new ErpException("No existe una materia con ID " + subjectId + "."));
        if (subject.getGradeId() != enrollment.getGradeId()) {
            throw new ErpException("La materia no corresponde al grado en que está matriculado el estudiante.");
        }
        if (subjectEnrollmentDao.exists(studentId, subjectId, year, period)) {
            throw new ErpException("El estudiante ya está inscrito en esa materia para el período " + period + ".");
        }
        subjectEnrollmentDao.insert(studentId, subjectId, year, period, section, LocalDate.now());
    }

    /** Enrolls the student in every subject of his/her grade not yet enrolled. Returns how many were added. */
    public int enrollInAllGradeSubjects(int studentId, int year, String period, String section) throws ErpException {
        Enrollment enrollment = requireActiveEnrollment(studentId, year);
        int added = 0;
        for (Subject subject : subjectDao.findByGrade(enrollment.getGradeId())) {
            if (!subjectEnrollmentDao.exists(studentId, subject.getId(), year, period)) {
                subjectEnrollmentDao.insert(studentId, subject.getId(), year, period, section, LocalDate.now());
                added++;
            }
        }
        return added;
    }

    public TableData subjectEnrollments(int studentId, int year) throws ErpException {
        return subjectEnrollmentDao.findByStudent(studentId, year);
    }

    private Enrollment requireActiveEnrollment(int studentId, int year) throws ErpException {
        studentDao.findById(studentId).orElseThrow(() -> new ErpException("No existe el estudiante " + studentId + "."));
        Enrollment enrollment = enrollmentDao.findByStudentAndYear(studentId, year)
                .orElseThrow(() -> new ErpException("El estudiante no tiene matrícula en el año " + year + "."));
        if (!"Activa".equals(enrollment.getStatus())) {
            throw new ErpException("La matrícula del año " + year + " no está activa.");
        }
        return enrollment;
    }

    // ---------- Grades ----------

    public void registerGrade(int studentId, int subjectId, int teacherId, int year, String period, BigDecimal value,
            String observations) throws ErpException {
        studentDao.findById(studentId).orElseThrow(() -> new ErpException("No existe el estudiante " + studentId + "."));
        subjectDao.findById(subjectId).orElseThrow(() -> new ErpException("No existe la materia " + subjectId + "."));
        teacherDao.findById(teacherId).orElseThrow(() -> new ErpException("No existe el profesor " + teacherId + "."));
        if (!assignmentDao.teacherIsAssigned(teacherId, subjectId, year)) {
            throw new ErpException("El profesor no tiene asignada esa materia en el año " + year + ".");
        }
        boolean enrolled = "Final".equals(period)
                ? subjectEnrollmentDao.existsInYear(studentId, subjectId, year)
                : subjectEnrollmentDao.exists(studentId, subjectId, year, period);
        if (!enrolled) {
            throw new ErpException("El estudiante no está inscrito en esa materia para ese período.");
        }
        AcademicRecord record = new AcademicRecord();
        record.setStudentId(studentId);
        record.setSubjectId(subjectId);
        record.setTeacherId(teacherId);
        record.setAcademicYear(year);
        record.setPeriod(period);
        record.setValue(value);
        record.setObservations(observations);
        record.setRecordDate(LocalDate.now());
        recordDao.save(record);
    }

    public TableData history(int studentId) throws ErpException {
        return recordDao.findHistory(studentId);
    }

    /** Average per subject (periods I-IV; the 'Final' record is excluded to avoid double counting). */
    public Map<String, Double> subjectAverages(int studentId, int year) throws ErpException {
        return recordDao.findByStudent(studentId, year).stream()
                .filter(r -> !"Final".equals(r.getPeriod()))
                .collect(Collectors.groupingBy(AcademicRecord::getSubjectName, TreeMap::new,
                        Collectors.averagingDouble(r -> r.getValue().doubleValue())));
    }

    /** General average of the student in each period (all subjects). */
    public Map<String, Double> periodAverages(int studentId, int year) throws ErpException {
        return recordDao.findByStudent(studentId, year).stream()
                .filter(r -> !"Final".equals(r.getPeriod()))
                .collect(Collectors.groupingBy(AcademicRecord::getPeriod, TreeMap::new,
                        Collectors.averagingDouble(r -> r.getValue().doubleValue())));
    }

    public double overallAverage(int studentId, int year) throws ErpException {
        return recordDao.findByStudent(studentId, year).stream()
                .filter(r -> !"Final".equals(r.getPeriod()))
                .mapToDouble(r -> r.getValue().doubleValue()).average().orElse(0.0);
    }

    /** Course average of one subject in each period. */
    public Map<String, Double> courseAverages(int subjectId, int year) throws ErpException {
        subjectDao.findById(subjectId).orElseThrow(() -> new ErpException("No existe la materia " + subjectId + "."));
        return recordDao.findBySubject(subjectId, year).stream()
                .filter(r -> !"Final".equals(r.getPeriod()))
                .collect(Collectors.groupingBy(AcademicRecord::getPeriod, TreeMap::new,
                        Collectors.averagingDouble(r -> r.getValue().doubleValue())));
    }

    // ---------- Attendance ----------

    public void markAttendance(int studentId, int subjectId, LocalDate date, String status, String notes,
            int teacherId) throws ErpException {
        studentDao.findById(studentId).orElseThrow(() -> new ErpException("No existe el estudiante " + studentId + "."));
        if (date.isAfter(LocalDate.now())) {
            throw new ErpException("No se puede registrar asistencia en una fecha futura.");
        }
        if (!ATTENDANCE_STATES.contains(status)) {
            throw new ErpException("Estado de asistencia inválido.");
        }
        if (!subjectEnrollmentDao.existsInYear(studentId, subjectId, date.getYear())) {
            throw new ErpException("El estudiante no está inscrito en esa materia en " + date.getYear() + ".");
        }
        if (!assignmentDao.teacherIsAssigned(teacherId, subjectId, date.getYear())) {
            throw new ErpException("El profesor no dicta esa materia en " + date.getYear() + ".");
        }
        Attendance attendance = new Attendance();
        attendance.setStudentId(studentId);
        attendance.setSubjectId(subjectId);
        attendance.setDate(date);
        attendance.setStatus(status);
        attendance.setNotes(notes);
        attendance.setRecordedBy(teacherId);
        attendanceDao.save(attendance);
    }

    public TableData attendanceOf(int studentId, LocalDate from, LocalDate to) throws ErpException {
        if (to.isBefore(from)) {
            throw new ErpException("La fecha final no puede ser anterior a la inicial.");
        }
        return attendanceDao.findByStudent(studentId, from, to);
    }
}
