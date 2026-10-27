package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.TeacherController;
import com.littlehorizons.erp.controller.StudentController;
import com.littlehorizons.erp.model.GradeLevel;
import com.littlehorizons.erp.util.AppLogger;
import com.littlehorizons.erp.util.ConsoleUI;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.InputClosedException;
import com.littlehorizons.erp.util.InputReader;
import java.time.LocalDate;
import java.util.List;

/** Shared behavior of every console view: error handling and common prompts. */
public abstract class BaseView {

    @FunctionalInterface
    protected interface Action {
        void run() throws ErpException;
    }

    protected final InputReader input;
    protected final StudentController students;
    protected final TeacherController teachers;

    protected BaseView(InputReader input, StudentController students, TeacherController teachers) {
        this.input = input;
        this.students = students;
        this.teachers = teachers;
    }

    /** Runs an action and turns any failure into a clear message instead of crashing the menu loop. */
    protected void safely(Action action) {
        try {
            action.run();
        } catch (ErpException e) {
            ConsoleUI.error(e.getMessage());
        } catch (InputClosedException e) {
            throw e;
        } catch (RuntimeException e) {
            AppLogger.error("Error inesperado en la vista", e);
            ConsoleUI.error("Ocurrió un error inesperado. Revise el archivo error_log.txt.");
        }
    }

    /** Asks for a student id or document. Returns 0 when the user cancels. */
    protected int askStudentId() {
        while (true) {
            String value = input.readLine("ID o documento del estudiante (0 para cancelar): ");
            if (value.equals("0")) {
                return 0;
            }
            if (value.isEmpty()) {
                ConsoleUI.error("Ingrese un ID o un documento.");
                continue;
            }
            try {
                int id = students.resolveStudentId(value);
                ConsoleUI.info("Estudiante: " + students.getStudent(id).getFullName());
                return id;
            } catch (ErpException e) {
                ConsoleUI.error(e.getMessage());
            }
        }
    }
    
    protected int askTeacherId() {
        while (true) {
            String value = input.readLine("ID o documento del profesor (0 para cancelar): ");
            if (value.equals("0")) {
                return 0;
            }
            if (value.isEmpty()) {
                ConsoleUI.error("Ingrese un ID o un documento.");
                continue;
            }
            try {
                int id = teachers.resolveTeacherId(value);
                ConsoleUI.info("Profesor: " + teachers.getTeacher(id).getFullName());
                return id;
            } catch (ErpException e) {
                ConsoleUI.error(e.getMessage());
            }
        }
    }

    /** Lists the grades and returns the chosen grade id (0 = cancel). */
    protected int chooseGrade() throws ErpException {
        List<GradeLevel> grades = students.listGrades();
        ConsoleUI.line("Grados disponibles:");
        for (GradeLevel g : grades) {
            ConsoleUI.line("  " + g.getId() + ". " + g.getName());
        }
        while (true) {
            int id = input.readInt("ID del grado (0 para cancelar): ", 0, Integer.MAX_VALUE);
            if (id == 0 || grades.stream().anyMatch(g -> g.getId() == id)) {
                return id;
            }
            ConsoleUI.error("Ese grado no existe.");
        }
    }

    protected int askYear() {
        int current = LocalDate.now().getYear();
        while (true) {
            String value = input.readLine("Año académico [Enter = " + current + "]: ");
            if (value.isEmpty()) {
                return current;
            }
            try {
                int year = Integer.parseInt(value);
                if (year >= 2000 && year <= current + 1) {
                    return year;
                }
            } catch (NumberFormatException e) {
                // falls through to the error message
            }
            ConsoleUI.error("Año inválido (2000 - " + (current + 1) + ").");
        }
    }

    /** Returns {first, middle, last, secondLast}; middle and second last name may be null. */
    protected String[] readNames() {
        String first = input.readRequired("Primer nombre: ", 100);
        String middle = input.readOptional("Segundo nombre", 100);
        String last = input.readRequired("Primer apellido: ", 100);
        String secondLast = input.readOptional("Segundo apellido", 100);
        return new String[] {first, middle, last, secondLast};
    }
}
