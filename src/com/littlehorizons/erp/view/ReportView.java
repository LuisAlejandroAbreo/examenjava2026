package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.ReportController;
import com.littlehorizons.erp.controller.StudentController;
import com.littlehorizons.erp.controller.TeacherController;
import com.littlehorizons.erp.util.ConsoleUI;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.InputReader;

/** Console view for reports and statistics. */
public class ReportView extends BaseView {

    private final ReportController reports;

    public ReportView(InputReader input, StudentController students, TeacherController teachers, ReportController reports) {
        super(input, students, teachers);
        this.reports = reports;
    }

    public void show() {
        while (true) {
            ConsoleUI.menu("REPORTES Y ESTADÍSTICAS", "Estudiantes con inasistencias preocupantes",
                    "Estudiantes morosos (más de un mes sin pagar pensión)", "Préstamos vencidos",
                    "Participación en eventos", "Estudiantes matriculados por grado", "Ocupación de clubes",
                    "Mejores promedios del año");
            final int option = input.readInt("Seleccione una opción: ", 0, 7);
            if (option == 0) {
                return;
            }
            safely(() -> dispatch(option));
        }
    }

    private void dispatch(int option) throws ErpException {
        switch (option) {
            case 1: absences(); break;
            case 2: ConsoleUI.table(reports.delinquents()); break;
            case 3: ConsoleUI.table(reports.overdueLoans()); break;
            case 4: ConsoleUI.table(reports.eventParticipation()); break;
            case 5: ConsoleUI.table(reports.studentsByGrade(askYear())); break;
            case 6: ConsoleUI.table(reports.clubOccupancy()); break;
            case 7: topStudents(); break;
            default: break;
        }
    }

    private void absences() throws ErpException {
        int weeks = input.readInt("Semanas hacia atrás a revisar (1-52): ", 1, 52);
        int max = input.readInt("Ausencias permitidas en una misma semana (1-5): ", 1, 5);
        ConsoleUI.info("Se listan estudiantes con MÁS de " + max + " ausencias en una semana.");
        ConsoleUI.table(reports.absencePatterns(weeks, max));
    }

    private void topStudents() throws ErpException {
        int year = askYear();
        int limit = input.readInt("¿Cuántos estudiantes mostrar? (1-50): ", 1, 50);
        ConsoleUI.table(reports.topStudents(year, limit));
    }
}
