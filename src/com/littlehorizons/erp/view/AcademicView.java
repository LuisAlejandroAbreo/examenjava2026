package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.AcademicController;
import com.littlehorizons.erp.controller.StudentController;
import com.littlehorizons.erp.controller.TeacherController;
import com.littlehorizons.erp.model.Subject;
import com.littlehorizons.erp.util.ConsoleUI;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.InputReader;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

/** Console view for the academic module: subjects, grades, averages and attendance. */
public class AcademicView extends BaseView {

    private final AcademicController academic;

    public AcademicView(InputReader input, StudentController students, TeacherController teachers, AcademicController academic) {
        super(input, students, teachers);
        this.academic = academic;
    }

    public void show() {
        while (true) {
            ConsoleUI.menu("GESTIÓN ACADÉMICA", "Crear materia", "Listar materias",
                    "Inscribir estudiante en todas las materias de su grado", "Inscribir estudiante en una materia",
                    "Registrar nota", "Consultar historial de notas", "Promedios de un estudiante",
                    "Promedio del curso en una materia", "Registrar asistencia", "Consultar asistencia de un estudiante");
            final int option = input.readInt("Seleccione una opción: ", 0, 10);
            if (option == 0) {
                return;
            }
            safely(() -> dispatch(option));
        }
    }

    private void dispatch(int option) throws ErpException {
        switch (option) {
            case 1: createSubject(); break;
            case 2: listSubjects(); break;
            case 3: enrollAll(); break;
            case 4: enrollOne(); break;
            case 5: registerGrade(); break;
            case 6: history(); break;
            case 7: studentAverages(); break;
            case 8: courseAverage(); break;
            case 9: markAttendance(); break;
            case 10: attendanceHistory(); break;
            default: break;
        }
    }

    private void createSubject() throws ErpException {
        ConsoleUI.section("Nueva materia");
        Subject s = new Subject();
        s.setCode(input.readRequired("Código (ej: MAT-5): ", 20).toUpperCase());
        s.setName(input.readRequired("Nombre de la materia: ", 100));
        s.setDescription(input.readOptional("Descripción", 500));
        int gradeId = chooseGrade();
        if (gradeId == 0) {
            return;
        }
        s.setGradeId(gradeId);
        s.setWeeklyHours(input.readInt("Horas semanales (1-20): ", 1, 20));
        int id = academic.createSubject(s);
        ConsoleUI.success("Materia creada con ID " + id + ".");
    }

    private void listSubjects() throws ErpException {
        Integer gradeId = null;
        if (input.confirm("¿Filtrar por grado?")) {
            int id = chooseGrade();
            if (id == 0) {
                return;
            }
            gradeId = id;
        }
        ConsoleUI.table(academic.listSubjects(gradeId));
    }

    private void enrollAll() throws ErpException {
        int studentId = askStudentId();
        if (studentId == 0) {
            return;
        }
        int year = askYear();
        String period = input.readOption("Período académico:", AcademicController.ENROLLMENT_PERIODS.toArray(new String[0]));
        String section = input.readOptional("Sección (ej: A)", 10);
        int added = academic.enrollInAllGradeSubjects(studentId, year, period, section);
        ConsoleUI.success(added + " inscripción(es) nuevas en el período " + period + " de " + year + ".");
        ConsoleUI.table(academic.subjectEnrollments(studentId, year));
    }

    private void enrollOne() throws ErpException {
        int studentId = askStudentId();
        if (studentId == 0) {
            return;
        }
        int subjectId = input.readId("ID de la materia");
        if (subjectId == 0) {
            return;
        }
        int year = askYear();
        String period = input.readOption("Período académico:", AcademicController.ENROLLMENT_PERIODS.toArray(new String[0]));
        String section = input.readOptional("Sección (ej: A)", 10);
        academic.enrollInSubject(studentId, subjectId, year, period, section);
        ConsoleUI.success("Inscripción registrada.");
    }

    private void registerGrade() throws ErpException {
        ConsoleUI.section("Registro de nota");
        int studentId = askStudentId();
        if (studentId == 0) {
            return;
        }
        int subjectId = input.readId("ID de la materia");
        if (subjectId == 0) {
            return;
        }
        int teacherId = input.readId("ID del profesor que registra");
        if (teacherId == 0) {
            return;
        }
        int year = askYear();
        String period = input.readOption("Período:", AcademicController.GRADE_PERIODS.toArray(new String[0]));
        BigDecimal value = input.readGrade("Nota (0.0 a 5.0): ");
        String observations = input.readOptional("Observaciones", 500);
        academic.registerGrade(studentId, subjectId, teacherId, year, period, value, observations);
        ConsoleUI.success("Nota registrada.");
    }

    private void history() throws ErpException {
        int studentId = askStudentId();
        if (studentId != 0) {
            ConsoleUI.table(academic.history(studentId));
        }
    }

    private void studentAverages() throws ErpException {
        int studentId = askStudentId();
        if (studentId == 0) {
            return;
        }
        int year = askYear();
        ConsoleUI.section("Promedio por materia - " + year);
        printAverages(academic.subjectAverages(studentId, year));
        ConsoleUI.section("Promedio general por período - " + year);
        printAverages(academic.periodAverages(studentId, year));
        ConsoleUI.line(String.format("%nPromedio general del año: %.2f", academic.overallAverage(studentId, year)));
    }

    private void courseAverage() throws ErpException {
        int subjectId = input.readId("ID de la materia");
        if (subjectId == 0) {
            return;
        }
        int year = askYear();
        ConsoleUI.section("Promedio del curso por período - " + year);
        printAverages(academic.courseAverages(subjectId, year));
    }

    private void printAverages(Map<String, Double> averages) {
        if (averages.isEmpty()) {
            ConsoleUI.info("No hay notas registradas.");
            return;
        }
        averages.forEach((name, avg) -> ConsoleUI.line(String.format("  %-32s %.2f", name, avg)));
    }

    private void markAttendance() throws ErpException {
        ConsoleUI.section("Registro de asistencia");
        int studentId = askStudentId();
        if (studentId == 0) {
            return;
        }
        int subjectId = input.readId("ID de la materia");
        if (subjectId == 0) {
            return;
        }
        int teacherId = input.readId("ID del profesor que registra");
        if (teacherId == 0) {
            return;
        }
        LocalDate date = input.readDateOrToday("Fecha de la clase");
        String status = input.readOption("Estado:", AcademicController.ATTENDANCE_STATES.toArray(new String[0]));
        String notes = input.readOptional("Observaciones", 500);
        academic.markAttendance(studentId, subjectId, date, status, notes, teacherId);
        ConsoleUI.success("Asistencia registrada.");
    }

    private void attendanceHistory() throws ErpException {
        int studentId = askStudentId();
        if (studentId == 0) {
            return;
        }
        LocalDate from = input.readDate("Desde");
        LocalDate to = input.readDateOrToday("Hasta");
        ConsoleUI.table(academic.attendanceOf(studentId, from, to));
    }
}
