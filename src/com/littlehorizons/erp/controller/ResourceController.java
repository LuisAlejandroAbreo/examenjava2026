package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.LoanDao;
import com.littlehorizons.erp.dao.ResourceDao;
import com.littlehorizons.erp.dao.StudentDao;
import com.littlehorizons.erp.dao.TeacherDao;
import com.littlehorizons.erp.model.Resource;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.TableData;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/** Business rules for the resource catalog and the loan system. */
public class ResourceController {

    public static final List<String> SETTABLE_STATUSES = Arrays.asList("Disponible", "En Mantenimiento", "Dado de Baja");
    private static final int MAX_LOAN_DAYS = 60;

    private final ResourceDao resourceDao;
    private final LoanDao loanDao;
    private final StudentDao studentDao;
    private final TeacherDao teacherDao;

    public ResourceController(ResourceDao resourceDao, LoanDao loanDao, StudentDao studentDao, TeacherDao teacherDao) {
        this.resourceDao = resourceDao;
        this.loanDao = loanDao;
        this.studentDao = studentDao;
        this.teacherDao = teacherDao;
    }

    public TableData categories() throws ErpException {
        return resourceDao.findCategories();
    }

    public TableData listResources(Integer categoryId, String status) throws ErpException {
        return resourceDao.findAllSummary(categoryId, status);
    }

    public int register(Resource resource) throws ErpException {
        if (!resourceDao.categoryExists(resource.getCategoryId())) {
            throw new ErpException("La categoría indicada no existe.");
        }
        if (resourceDao.findByCode(resource.getCode()).isPresent()) {
            throw new ErpException("Ya existe un recurso con el código " + resource.getCode() + ".");
        }
        resource.setStatus("Disponible");
        return resourceDao.insert(resource);
    }

    public void changeStatus(int resourceId, String status) throws ErpException {
        Resource resource = resourceDao.findById(resourceId)
                .orElseThrow(() -> new ErpException("No existe el recurso " + resourceId + "."));
        if (!SETTABLE_STATUSES.contains(status)) {
            throw new ErpException("Estado inválido.");
        }
        if ("Prestado".equals(resource.getStatus())) {
            throw new ErpException("El recurso está prestado; registre primero la devolución.");
        }
        resourceDao.updateStatus(resourceId, status);
    }

    public int lendToStudent(int resourceId, int studentId, LocalDate dueDate, String notes) throws ErpException {
        Student student = studentDao.findById(studentId)
                .orElseThrow(() -> new ErpException("No existe el estudiante " + studentId + "."));
        if (!"Activo".equals(student.getStatus())) {
            throw new ErpException("El estudiante no está activo.");
        }
        checkDueDate(dueDate);
        return loanDao.lend(resourceId, LoanDao.STUDENT, studentId, LocalDate.now(), dueDate, notes);
    }

    public int lendToTeacher(int resourceId, int teacherId, LocalDate dueDate, String notes) throws ErpException {
        Teacher teacher = teacherDao.findById(teacherId)
                .orElseThrow(() -> new ErpException("No existe el profesor " + teacherId + "."));
        if (!"Activo".equals(teacher.getStatus())) {
            throw new ErpException("El profesor no está activo.");
        }
        checkDueDate(dueDate);
        return loanDao.lend(resourceId, LoanDao.TEACHER, teacherId, LocalDate.now(), dueDate, notes);
    }

    private void checkDueDate(LocalDate dueDate) throws ErpException {
        LocalDate today = LocalDate.now();
        if (dueDate.isBefore(today)) {
            throw new ErpException("La fecha de devolución no puede ser anterior a hoy.");
        }
        if (dueDate.isAfter(today.plusDays(MAX_LOAN_DAYS))) {
            throw new ErpException("El préstamo no puede superar " + MAX_LOAN_DAYS + " días.");
        }
    }

    public void giveBack(boolean student, int loanId, LocalDate returnDate) throws ErpException {
        loanDao.giveBack(student ? LoanDao.STUDENT : LoanDao.TEACHER, loanId, returnDate);
    }

    public TableData openLoans() throws ErpException {
        loanDao.refreshOverdue(LocalDate.now());
        return loanDao.findOpenLoans();
    }

    public TableData overdueLoans() throws ErpException {
        LocalDate today = LocalDate.now();
        loanDao.refreshOverdue(today);
        return loanDao.findOverdue(today);
    }
}
