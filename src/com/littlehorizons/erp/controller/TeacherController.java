package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.AssignmentDao;
import com.littlehorizons.erp.dao.SubjectDao;
import com.littlehorizons.erp.dao.TeacherDao;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.model.TeacherAssignment;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.TableData;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.List;

/** Business rules for teachers, workload assignment and class schedules. */
public class TeacherController {

    public static final List<String> DAYS = Arrays.asList("Lunes", "Martes", "Miércoles", "Jueves", "Viernes");
    private static final LocalTime SCHOOL_OPENS = LocalTime.of(6, 0);
    private static final LocalTime SCHOOL_CLOSES = LocalTime.of(18, 0);

    private final TeacherDao teacherDao;
    private final AssignmentDao assignmentDao;
    private final SubjectDao subjectDao;

    public TeacherController(TeacherDao teacherDao, AssignmentDao assignmentDao, SubjectDao subjectDao) {
        this.teacherDao = teacherDao;
        this.assignmentDao = assignmentDao;
        this.subjectDao = subjectDao;
    }
    
    public int resolveTeacherId(String text) throws ErpException {
        String value = text.trim();
        if (value.matches("\\d{1,6}")) {
            return getTeacher(Integer.parseInt(value)).getId();
        }
        return teacherDao.findByDocument(value)
                .orElseThrow(() -> new ErpException("No existe un estudiante con documento " + value + "."))
                .getId();
    }


    public Teacher getTeacher(int id) throws ErpException {
        return teacherDao.findById(id).orElseThrow(() -> new ErpException("No existe un profesor con ID " + id + "."));
    }

    public int register(Teacher teacher) throws ErpException {
        if (teacherDao.findByDocument(teacher.getDocumentNumber()).isPresent()) {
            throw new ErpException("Ya existe un profesor con el documento " + teacher.getDocumentNumber() + ".");
        }
        teacher.setStatus("Activo");
        return teacherDao.insert(teacher);
    }

    public int assignSubject(int teacherId, int subjectId, int academicYear, String section) throws ErpException {
        Teacher teacher = getTeacher(teacherId);
        if (!"Activo".equals(teacher.getStatus())) {
            throw new ErpException("El profesor no está activo (estado: " + teacher.getStatus() + ").");
        }
        Subject subject = subjectDao.findById(subjectId)
                .orElseThrow(() -> new ErpException("No existe una materia con ID " + subjectId + "."));
        if (!subject.isActive()) {
            throw new ErpException("La materia está inactiva.");
        }
        if (assignmentDao.exists(teacherId, subjectId, academicYear, section)) {
            throw new ErpException("El profesor ya tiene asignada esa materia en ese año y sección.");
        }
        return assignmentDao.insert(teacherId, subjectId, academicYear, section);
    }

    /** Registers a weekly class block after checking teacher and group conflicts. */
    public void addSchedule(int assignmentId, String day, LocalTime start, LocalTime end) throws ErpException {
        TeacherAssignment a = assignmentDao.findById(assignmentId)
                .orElseThrow(() -> new ErpException("No existe una asignación con ID " + assignmentId + "."));
        if (!DAYS.contains(day)) {
            throw new ErpException("Día inválido.");
        }
        if (!end.isAfter(start)) {
            throw new ErpException("La hora de fin debe ser posterior a la hora de inicio.");
        }
        if (start.isBefore(SCHOOL_OPENS) || end.isAfter(SCHOOL_CLOSES)) {
            throw new ErpException("El horario debe estar entre 06:00 y 18:00.");
        }
        if (assignmentDao.hasTeacherConflict(a.getTeacherId(), a.getAcademicYear(), day, start, end)) {
            throw new ErpException("Conflicto: el profesor ya tiene otra clase en ese día y rango de horas.");
        }
        if (assignmentDao.hasGroupConflict(a.getGradeId(), a.getAcademicYear(), a.getSection(), day, start, end)) {
            throw new ErpException("Conflicto: el grupo ya tiene otra materia en ese día y rango de horas.");
        }
        assignmentDao.addSchedule(assignmentId, day, start, end);
    }

    public TableData listTeachers() throws ErpException {
        return teacherDao.findAllSummary();
    }

    public TableData workload(int teacherId, Integer academicYear) throws ErpException {
        getTeacher(teacherId);
        return assignmentDao.findByTeacher(teacherId, academicYear);
    }

    public TableData schedule(int teacherId, int academicYear) throws ErpException {
        getTeacher(teacherId);
        return assignmentDao.findScheduleByTeacher(teacherId, academicYear);
    }
}
