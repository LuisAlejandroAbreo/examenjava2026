package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.StudentController;
import com.littlehorizons.erp.controller.TeacherController;
import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.util.ConsoleUI;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.InputReader;

/** Console view for students, guardians and grade enrollment. */
public class StudentView extends BaseView {

    public StudentView(InputReader input, StudentController students, TeacherController teachers) {
        super(input, students, teachers);
    }

    public void show() {
        while (true) {
            ConsoleUI.menu("GESTIÓN DE ESTUDIANTES", "Registrar estudiante", "Listar estudiantes",
                    "Buscar estudiante", "Ver ficha del estudiante", "Matricular estudiante en un grado",
                    "Agregar acudiente a un estudiante", "Cambiar estado del estudiante",
                    "Ver estudiantes de un acudiente");
            final int option = input.readInt("Seleccione una opción: ", 0, 8);
            if (option == 0) {
                return;
            }
            safely(() -> dispatch(option));
        }
    }

    private void dispatch(int option) throws ErpException {
        switch (option) {
            case 1: registerStudent(); break;
            case 2: ConsoleUI.table(students.listStudents()); break;
            case 3: ConsoleUI.table(students.search(input.readRequired("Texto a buscar (nombre, apellido o documento): ", 50))); break;
            case 4: showProfile(); break;
            case 5: enrollInGrade(); break;
            case 6: addGuardian(); break;
            case 7: changeStatus(); break;
            case 8: studentsOfGuardian(); break;
            default: break;
        }
    }

    private void registerStudent() throws ErpException {
        ConsoleUI.section("Registro de estudiante");
        ConsoleUI.line("Todo estudiante debe tener al menos un acudiente.");
        int guardianId = obtainGuardian();
        Student s = new Student();
        s.setDocumentType(input.readOption("Tipo de documento:", "TI", "CC", "CE", "Pasaporte"));
        s.setDocumentNumber(input.readDocument("Número de documento: "));
        String[] names = readNames();
        s.setFirstName(names[0]);
        s.setMiddleName(names[1]);
        s.setLastName(names[2]);
        s.setSecondLastName(names[3]);
        s.setBirthDate(input.readPastDate("Fecha de nacimiento"));
        s.setGender(input.readOption("Género:", "M", "F", "Otro"));
        s.setAddress(input.readOptional("Dirección", 255));
        s.setPhone(input.readPhone("Teléfono", false));
        s.setEmail(input.readEmail("Correo electrónico", false));
        s.setBloodType(input.readOptional("Tipo de sangre (ej: O+)", 5));
        s.setAllergies(input.readOptional("Alergias", 500));
        s.setMedicalConditions(input.readOptional("Condiciones médicas", 500));
        s.setAdmissionDate(input.readDateOrToday("Fecha de ingreso"));
        int id = students.registerStudent(s, guardianId);
        ConsoleUI.success("Estudiante registrado con ID " + id + ".");
        if (input.confirm("¿Desea matricularlo en un grado ahora?")) {
            enrollStudent(id);
        }
    }

    private int obtainGuardian() throws ErpException {
        if (input.confirm("¿El acudiente ya está registrado en el sistema?")) {
            Guardian g = students.findGuardianByDocument(input.readDocument("Documento del acudiente: "));
            ConsoleUI.info("Acudiente: " + g.getFullName() + " (" + g.getRelationship() + ")");
            return g.getId();
        }
        return createGuardian();
    }

    private int createGuardian() throws ErpException {
        ConsoleUI.section("Registro de acudiente");
        Guardian g = new Guardian();
        g.setDocumentType(input.readOption("Tipo de documento:", "CC", "CE", "Pasaporte"));
        g.setDocumentNumber(input.readDocument("Número de documento: "));
        String[] names = readNames();
        g.setFirstName(names[0]);
        g.setMiddleName(names[1]);
        g.setLastName(names[2]);
        g.setSecondLastName(names[3]);
        g.setRelationship(input.readOption("Parentesco:", "Madre", "Padre", "Abuelo", "Abuela", "Tío", "Tía",
                "Tutor Legal", "Otro"));
        g.setMainPhone(input.readPhone("Teléfono principal", true));
        g.setSecondaryPhone(input.readPhone("Teléfono secundario", false));
        g.setEmail(input.readEmail("Correo electrónico", false));
        g.setAddress(input.readOptional("Dirección", 255));
        g.setOccupation(input.readOptional("Ocupación", 100));
        g.setFinanciallyResponsible(input.confirm("¿Es responsable de los pagos?"));
        int id = students.registerGuardian(g);
        ConsoleUI.success("Acudiente registrado con ID " + id + ".");
        return id;
    }

    private void showProfile() throws ErpException {
        int id = askStudentId();
        if (id == 0) {
            return;
        }
        Student s = students.getStudent(id);
        ConsoleUI.section("Ficha del estudiante");
        ConsoleUI.line("ID: " + s.getId() + "   Estado: " + s.getStatus());
        ConsoleUI.line("Nombre: " + s.getFullName());
        ConsoleUI.line("Documento: " + s.getDocumentType() + " " + s.getDocumentNumber());
        ConsoleUI.line("Nacimiento: " + s.getBirthDate() + "   Género: " + s.getGender());
        ConsoleUI.line("Dirección: " + orDash(s.getAddress()) + "   Teléfono: " + orDash(s.getPhone()));
        ConsoleUI.line("Correo: " + orDash(s.getEmail()) + "   Tipo de sangre: " + orDash(s.getBloodType()));
        ConsoleUI.line("Alergias: " + orDash(s.getAllergies()));
        ConsoleUI.line("Condiciones médicas: " + orDash(s.getMedicalConditions()));
        ConsoleUI.line("Fecha de ingreso: " + s.getAdmissionDate());
        ConsoleUI.section("Acudientes");
        ConsoleUI.table(students.guardiansOf(id));
        ConsoleUI.section("Matrículas");
        ConsoleUI.table(students.enrollmentsOf(id));
    }

    private void enrollInGrade() throws ErpException {
        int id = askStudentId();
        if (id != 0) {
            enrollStudent(id);
        }
    }

    private void enrollStudent(int studentId) throws ErpException {
        int gradeId = chooseGrade();
        if (gradeId == 0) {
            return;
        }
        int year = askYear();
        students.enroll(studentId, gradeId, year);
        ConsoleUI.success("Matrícula registrada para el año " + year + ".");
    }

    private void addGuardian() throws ErpException {
        int id = askStudentId();
        if (id == 0) {
            return;
        }
        int guardianId = obtainGuardian();
        boolean main = input.confirm("¿Será el acudiente principal?");
        students.linkGuardian(id, guardianId, main);
        ConsoleUI.success("Acudiente asociado al estudiante.");
    }

    private void changeStatus() throws ErpException {
        int id = askStudentId();
        if (id == 0) {
            return;
        }
        String status = input.readOption("Nuevo estado:", StudentController.STATUSES.toArray(new String[0]));
        students.changeStatus(id, status);
        ConsoleUI.success("Estado actualizado a " + status + ".");
    }

    private void studentsOfGuardian() throws ErpException {
        int id = input.readId("ID del acudiente");
        if (id != 0) {
            ConsoleUI.table(students.studentsOfGuardian(id));
        }
    }

    private static String orDash(String value) {
        return value == null || value.isEmpty() ? "-" : value;
    }
}
