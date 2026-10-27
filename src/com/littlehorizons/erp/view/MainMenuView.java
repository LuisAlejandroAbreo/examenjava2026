package com.littlehorizons.erp.view;

import com.littlehorizons.erp.util.ConsoleUI;
import com.littlehorizons.erp.util.InputReader;

/** Main menu: groups every module of the ERP. */
public class MainMenuView {

    private final InputReader input;
    private final StudentView studentView;
    private final AcademicView academicView;
    private final TeacherView teacherView;
    private final ExtracurricularView extracurricularView;
    private final FinanceView financeView;
    private final ResourceView resourceView;
    private final ReportView reportView;

    public MainMenuView(InputReader input, StudentView studentView, AcademicView academicView,
            TeacherView teacherView, ExtracurricularView extracurricularView, FinanceView financeView,
            ResourceView resourceView, ReportView reportView) {
        this.input = input;
        this.studentView = studentView;
        this.academicView = academicView;
        this.teacherView = teacherView;
        this.extracurricularView = extracurricularView;
        this.financeView = financeView;
        this.resourceView = resourceView;
        this.reportView = reportView;
    }

    public void show() {
        while (true) {
            ConsoleUI.banner("ERP Colegio Little Horizons", "Sistema de Gestión Educativa");
            ConsoleUI.line("1. Gestión de Estudiantes");
            ConsoleUI.line("2. Gestión Académica");
            ConsoleUI.line("3. Gestión de Profesores");
            ConsoleUI.line("4. Actividades Extracurriculares");
            ConsoleUI.line("5. Gestión Financiera");
            ConsoleUI.line("6. Gestión de Recursos");
            ConsoleUI.line("7. Reportes y Estadísticas");
            ConsoleUI.line("8. Salir");
            int option = input.readInt("Seleccione una opción: ", 1, 8);
            switch (option) {
                case 1: studentView.show(); break;
                case 2: academicView.show(); break;
                case 3: teacherView.show(); break;
                case 4: extracurricularView.show(); break;
                case 5: financeView.show(); break;
                case 6: resourceView.show(); break;
                case 7: reportView.show(); break;
                default:
                    ConsoleUI.info("Hasta pronto.");
                    return;
            }
        }
    }
}
