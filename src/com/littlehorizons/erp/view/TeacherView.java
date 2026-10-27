package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.StudentController;
import com.littlehorizons.erp.controller.TeacherController;
import com.littlehorizons.erp.controller.TeacherController;
import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.util.ConsoleUI;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.InputReader;
import java.time.LocalTime;

/** Console view for teachers, workload and class schedules. */
public class TeacherView extends BaseView {

    private final TeacherController teachers;

    public TeacherView(InputReader input, StudentController students, TeacherController teachers) {
        super(input, students, teachers);
        this.teachers = teachers;
    }

    public void show() {
        while (true) {
            ConsoleUI.menu("GESTIÓN DE PROFESORES", "Registrar profesor", "Listar profesores",
                    "Asignar materia a un profesor", "Ver carga académica de un profesor",
                    "Registrar horario de clase", "Ver horario de un profesor");
            final int option = input.readInt("Seleccione una opción: ", 0, 6);
            if (option == 0) {
                return;
            }
            safely(() -> dispatch(option));
        }
    }

    private void dispatch(int option) throws ErpException {
        switch (option) {
            case 1: register(); break;
            case 2: ConsoleUI.table(teachers.listTeachers()); break;
            case 3: assign(); break;
            case 4: workload(); break;
            case 5: addSchedule(); break;
            case 6: schedule(); break;
            default: break;
        }
    }

    private void register() throws ErpException {
        ConsoleUI.section("Registro de profesor");
        Teacher t = new Teacher();
        t.setDocumentType(input.readOption("Tipo de documento:", "CC", "CE", "Pasaporte"));
        t.setDocumentNumber(input.readDocument("Número de documento: "));
        String[] names = readNames();
        t.setFirstName(names[0]);
        t.setMiddleName(names[1]);
        t.setLastName(names[2]);
        t.setSecondLastName(names[3]);
        t.setEmail(input.readEmail("Correo electrónico", true));
        t.setPhone(input.readPhone("Teléfono", false));
        t.setSpecialization(input.readRequired("Especialización (ej: Matemáticas): ", 100));
        t.setHireDate(input.readPastDate("Fecha de contratación"));
        int id = teachers.register(t);
        ConsoleUI.success("Profesor registrado con ID " + id + ".");
    }

    private void assign() throws ErpException {
        int teacherId = input.readId("ID del profesor");
        if (teacherId == 0) {
            return;
        }
        int subjectId = input.readId("ID de la materia");
        if (subjectId == 0) {
            return;
        }
        int year = askYear();
        String section = input.readOptional("Sección o grupo (ej: A)", 10);
        int id = teachers.assignSubject(teacherId, subjectId, year, section);
        ConsoleUI.success("Materia asignada (ID de asignación " + id + ").");
    }

    private void workload() throws ErpException {
        int teacherId = input.readId("ID del profesor");
        if (teacherId != 0) {
            ConsoleUI.table(teachers.workload(teacherId, null));
        }
    }

    private void addSchedule() throws ErpException {
        ConsoleUI.line("Use 'Ver carga académica' para consultar el ID de asignación.");
        int assignmentId = input.readId("ID de la asignación");
        if (assignmentId == 0) {
            return;
        }
        String day = input.readOption("Día de la semana:", TeacherController.DAYS.toArray(new String[0]));
        LocalTime start = input.readTime("Hora de inicio");
        LocalTime end = input.readTime("Hora de fin");
        teachers.addSchedule(assignmentId, day, start, end);
        ConsoleUI.success("Horario registrado sin conflictos.");
    }

    private void schedule() throws ErpException {
        int teacherId = input.readId("ID del profesor");
        if (teacherId == 0) {
            return;
        }
        ConsoleUI.table(teachers.schedule(teacherId, askYear()));
    }
}
