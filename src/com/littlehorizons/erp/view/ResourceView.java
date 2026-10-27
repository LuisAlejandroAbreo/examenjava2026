package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.ResourceController;
import com.littlehorizons.erp.controller.StudentController;
import com.littlehorizons.erp.controller.TeacherController;
import com.littlehorizons.erp.model.Resource;
import com.littlehorizons.erp.util.ConsoleUI;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.InputReader;
import java.time.LocalDate;

/** Console view for the resource catalog and loans. */
public class ResourceView extends BaseView {

    private final ResourceController resources;

    public ResourceView(InputReader input, StudentController students, TeacherController teachers, ResourceController resources) {
        super(input, students, teachers);
        this.resources = resources;
    }

    public void show() {
        while (true) {
            ConsoleUI.menu("GESTIÓN DE RECURSOS", "Listar recursos", "Registrar recurso", "Prestar recurso",
                    "Registrar devolución", "Préstamos activos", "Préstamos vencidos",
                    "Cambiar estado de un recurso (mantenimiento / baja / disponible)");
            final int option = input.readInt("Seleccione una opción: ", 0, 7);
            if (option == 0) {
                return;
            }
            safely(() -> dispatch(option));
        }
    }

    private void dispatch(int option) throws ErpException {
        switch (option) {
            case 1: list(); break;
            case 2: register(); break;
            case 3: lend(); break;
            case 4: giveBack(); break;
            case 5: ConsoleUI.table(resources.openLoans()); break;
            case 6: ConsoleUI.table(resources.overdueLoans()); break;
            case 7: changeStatus(); break;
            default: break;
        }
    }

    private void list() throws ErpException {
        Integer categoryId = null;
        if (input.confirm("¿Filtrar por categoría?")) {
            ConsoleUI.table(resources.categories());
            int id = input.readId("ID de la categoría");
            if (id == 0) {
                return;
            }
            categoryId = id;
        }
        String status = null;
        if (input.confirm("¿Filtrar por estado?")) {
            status = input.readOption("Estado:", "Disponible", "Prestado", "En Mantenimiento", "Dado de Baja");
        }
        ConsoleUI.table(resources.listResources(categoryId, status));
    }

    private void register() throws ErpException {
        ConsoleUI.section("Nuevo recurso");
        ConsoleUI.table(resources.categories());
        Resource r = new Resource();
        int categoryId = input.readId("ID de la categoría");
        if (categoryId == 0) {
            return;
        }
        r.setCategoryId(categoryId);
        r.setCode(input.readRequired("Código único (ej: LIB-0100): ", 50).toUpperCase());
        r.setName(input.readRequired("Nombre del recurso: ", 150));
        r.setDescription(input.readOptional("Descripción", 500));
        r.setAcquisitionDate(input.readPastDate("Fecha de adquisición"));
        r.setLocation(input.readOptional("Ubicación física", 100));
        int id = resources.register(r);
        ConsoleUI.success("Recurso registrado con ID " + id + ".");
    }

    private void lend() throws ErpException {
        ConsoleUI.section("Registro de préstamo");
        ConsoleUI.table(resources.listResources(null, "Disponible"));
        int resourceId = input.readId("ID del recurso a prestar");
        if (resourceId == 0) {
            return;
        }
        String borrower = input.readOption("¿Quién lo solicita?", "Estudiante", "Profesor");
        int borrowerId;
        if ("Estudiante".equals(borrower)) {
            borrowerId = askStudentId();
        } else {
            borrowerId = input.readId("ID del profesor");
        }
        if (borrowerId == 0) {
            return;
        }
        LocalDate due = input.readFutureOrTodayDate("Fecha de devolución programada", LocalDate.now());
        String notes = input.readOptional("Observaciones", 500);
        int loanId = "Estudiante".equals(borrower)
                ? resources.lendToStudent(resourceId, borrowerId, due, notes)
                : resources.lendToTeacher(resourceId, borrowerId, due, notes);
        ConsoleUI.success("Préstamo registrado con ID " + loanId + ". Devolución programada: " + due + ".");
    }

    private void giveBack() throws ErpException {
        ConsoleUI.table(resources.openLoans());
        String borrower = input.readOption("Tipo de prestatario:", "Estudiante", "Profesor");
        int loanId = input.readId("ID del préstamo");
        if (loanId == 0) {
            return;
        }
        LocalDate date = input.readDateOrToday("Fecha de devolución");
        resources.giveBack("Estudiante".equals(borrower), loanId, date);
        ConsoleUI.success("Devolución registrada. El recurso quedó disponible.");
    }

    private void changeStatus() throws ErpException {
        int resourceId = input.readId("ID del recurso");
        if (resourceId == 0) {
            return;
        }
        String status = input.readOption("Nuevo estado:", ResourceController.SETTABLE_STATUSES.toArray(new String[0]));
        resources.changeStatus(resourceId, status);
        ConsoleUI.success("Estado actualizado a " + status + ".");
    }
}
