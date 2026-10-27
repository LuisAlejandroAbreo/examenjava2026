package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.EnrollmentDao;
import com.littlehorizons.erp.dao.GradeLevelDao;
import com.littlehorizons.erp.dao.GuardianDao;
import com.littlehorizons.erp.dao.StudentDao;
import com.littlehorizons.erp.model.Enrollment;
import com.littlehorizons.erp.model.GradeLevel;
import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.TableData;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/** Business rules for students, guardians and grade enrollment. */
public class StudentController {

    public static final List<String> STATUSES = Arrays.asList("Activo", "Graduado", "Retirado", "Suspendido");

    private final StudentDao studentDao;
    private final GuardianDao guardianDao;
    private final EnrollmentDao enrollmentDao;
    private final GradeLevelDao gradeDao;

    public StudentController(StudentDao studentDao, GuardianDao guardianDao, EnrollmentDao enrollmentDao,
            GradeLevelDao gradeDao) {
        this.studentDao = studentDao;
        this.guardianDao = guardianDao;
        this.enrollmentDao = enrollmentDao;
        this.gradeDao = gradeDao;
    }

    /** Accepts an internal id (up to 6 digits) or a document number (7+ characters). */
    public int resolveStudentId(String text) throws ErpException {
        String value = text.trim();
        if (value.matches("\\d{1,6}")) {
            return getStudent(Integer.parseInt(value)).getId();
        }
        return studentDao.findByDocument(value)
                .orElseThrow(() -> new ErpException("No existe un estudiante con documento " + value + "."))
                .getId();
    }

    public Student getStudent(int id) throws ErpException {
        return studentDao.findById(id)
                .orElseThrow(() -> new ErpException("No existe un estudiante con ID " + id + "."));
    }

    public Guardian findGuardianByDocument(String document) throws ErpException {
        return guardianDao.findByDocument(document)
                .orElseThrow(() -> new ErpException("No existe un acudiente con documento " + document + "."));
    }

    public Guardian getGuardian(int id) throws ErpException {
        return guardianDao.findById(id)
                .orElseThrow(() -> new ErpException("No existe un acudiente con ID " + id + "."));
    }

    public int registerGuardian(Guardian guardian) throws ErpException {
        if (guardianDao.findByDocument(guardian.getDocumentNumber()).isPresent()) {
            throw new ErpException("Ya existe un acudiente con el documento " + guardian.getDocumentNumber() + ".");
        }
        return guardianDao.insert(guardian);
    }

    /** Registers the student and links the main guardian (every student needs at least one). */
    public int registerStudent(Student student, int mainGuardianId) throws ErpException {
        getGuardian(mainGuardianId);
        if (studentDao.findByDocument(student.getDocumentNumber()).isPresent()) {
            throw new ErpException("Ya existe un estudiante con el documento " + student.getDocumentNumber() + ".");
        }
        if (student.getBirthDate().isAfter(LocalDate.now())) {
            throw new ErpException("La fecha de nacimiento no puede ser futura.");
        }
        student.setStatus("Activo");
        int id = studentDao.insert(student);
        guardianDao.link(id, mainGuardianId, true, true);
        return id;
    }

    public void linkGuardian(int studentId, int guardianId, boolean main) throws ErpException {
        getStudent(studentId);
        getGuardian(guardianId);
        if (guardianDao.isLinked(studentId, guardianId)) {
            throw new ErpException("Ese acudiente ya está asociado al estudiante.");
        }
        guardianDao.link(studentId, guardianId, main, true);
    }

    public void changeStatus(int studentId, String status) throws ErpException {
        getStudent(studentId);
        if (!STATUSES.contains(status)) {
            throw new ErpException("Estado inválido.");
        }
        studentDao.updateStatus(studentId, status);
    }

    public int enroll(int studentId, int gradeId, int academicYear) throws ErpException {
        Student student = getStudent(studentId);
        if (!"Activo".equals(student.getStatus())) {
            throw new ErpException("Solo se pueden matricular estudiantes activos (estado: " + student.getStatus() + ").");
        }
        if (!gradeDao.findById(gradeId).isPresent()) {
            throw new ErpException("El grado seleccionado no existe.");
        }
        int currentYear = LocalDate.now().getYear();
        if (academicYear < 2000 || academicYear > currentYear + 1) {
            throw new ErpException("Año académico fuera de rango (2000 - " + (currentYear + 1) + ").");
        }
        if (enrollmentDao.findByStudentAndYear(studentId, academicYear).isPresent()) {
            throw new ErpException("El estudiante ya tiene matrícula en el año " + academicYear + ".");
        }
        Enrollment enrollment = new Enrollment();
        enrollment.setStudentId(studentId);
        enrollment.setGradeId(gradeId);
        enrollment.setAcademicYear(academicYear);
        enrollment.setStatus("Activa");
        enrollment.setEnrollmentDate(LocalDate.now());
        return enrollmentDao.insert(enrollment);
    }

    public List<GradeLevel> listGrades() throws ErpException {
        return gradeDao.findAll();
    }

    public TableData listStudents() throws ErpException {
        return studentDao.findAllSummary();
    }

    public TableData search(String text) throws ErpException {
        return studentDao.search(text);
    }

    public TableData guardiansOf(int studentId) throws ErpException {
        return guardianDao.findByStudent(studentId);
    }

    public TableData enrollmentsOf(int studentId) throws ErpException {
        return enrollmentDao.findByStudent(studentId);
    }

    public TableData studentsOfGuardian(int guardianId) throws ErpException {
        getGuardian(guardianId);
        return studentDao.findByGuardian(guardianId);
    }
}
