package com.littlehorizons.erp.examen.src.view;

import com.littlehorizons.erp.util.ConsoleUI;
import com.littlehorizons.erp.util.InputReader;
import com.littlehorizons.erp.view.AcademicView;
import com.littlehorizons.erp.view.ExtracurricularView;
import com.littlehorizons.erp.view.FinanceView;
import com.littlehorizons.erp.view.ReportView;
import com.littlehorizons.erp.view.ResourceView;
import com.littlehorizons.erp.view.StudentView;
import com.littlehorizons.erp.view.TeacherView;

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
    private final ExportView newView;

    public MainMenuView(InputReader input, StudentView studentView, AcademicView academicView,
            TeacherView teacherView, ExtracurricularView extracurricularView, FinanceView financeView,
            ResourceView resourceView, ReportView reportView, ExportView newView) {
        this.input = input;
        this.studentView = studentView;
        this.academicView = academicView;
        this.teacherView = teacherView;
        this.extracurricularView = extracurricularView;
        this.financeView = financeView;
        this.resourceView = resourceView;
        this.reportView = reportView;
        this.newView = newView;
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
            ConsoleUI.line("8. Exportar reportes a archivo");
            ConsoleUI.line("9. Salir");
            int option = input.readInt("Seleccione una opción: ", 1, 9);
            switch (option) {
                case 1: studentView.show(); break;
                case 2: academicView.show(); break;
                case 3: teacherView.show(); break;
                case 4: extracurricularView.show(); break;
                case 5: financeView.show(); break;
                case 6: resourceView.show(); break;
                case 7: reportView.show(); break;
                case 8: newView.show(); break;
                default:
                    ConsoleUI.info("Hasta pronto.");
                    return;
            }
        }
    }
}
