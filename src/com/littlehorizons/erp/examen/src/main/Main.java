package com.littlehorizons.erp.examen.src.main;

import com.littlehorizons.erp.controller.AcademicController;
import com.littlehorizons.erp.controller.ExtracurricularController;
import com.littlehorizons.erp.controller.FinanceController;
import com.littlehorizons.erp.controller.ReportController;
import com.littlehorizons.erp.controller.ResourceController;
import com.littlehorizons.erp.controller.StudentController;
import com.littlehorizons.erp.controller.TeacherController;
import com.littlehorizons.erp.dao.AcademicRecordDao;
import com.littlehorizons.erp.dao.AssignmentDao;
import com.littlehorizons.erp.dao.AttendanceDao;
import com.littlehorizons.erp.dao.ClubDao;
import com.littlehorizons.erp.dao.EnrollmentDao;
import com.littlehorizons.erp.dao.EventDao;
import com.littlehorizons.erp.dao.GradeLevelDao;
import com.littlehorizons.erp.dao.GuardianDao;
import com.littlehorizons.erp.dao.InvoiceDao;
import com.littlehorizons.erp.dao.LoanDao;
import com.littlehorizons.erp.dao.PaymentConceptDao;
import com.littlehorizons.erp.dao.PaymentDao;
import com.littlehorizons.erp.dao.ReportDao;
import com.littlehorizons.erp.dao.ResourceDao;
import com.littlehorizons.erp.dao.StudentDao;
import com.littlehorizons.erp.dao.SubjectDao;
import com.littlehorizons.erp.dao.SubjectEnrollmentDao;
import com.littlehorizons.erp.dao.TeacherDao;
import com.littlehorizons.erp.dao.impl.AcademicRecordDaoImpl;
import com.littlehorizons.erp.dao.impl.AssignmentDaoImpl;
import com.littlehorizons.erp.dao.impl.AttendanceDaoImpl;
import com.littlehorizons.erp.dao.impl.ClubDaoImpl;
import com.littlehorizons.erp.dao.impl.EnrollmentDaoImpl;
import com.littlehorizons.erp.dao.impl.EventDaoImpl;
import com.littlehorizons.erp.dao.impl.GradeLevelDaoImpl;
import com.littlehorizons.erp.dao.impl.GuardianDaoImpl;
import com.littlehorizons.erp.dao.impl.InvoiceDaoImpl;
import com.littlehorizons.erp.dao.impl.LoanDaoImpl;
import com.littlehorizons.erp.dao.impl.PaymentConceptDaoImpl;
import com.littlehorizons.erp.dao.impl.PaymentDaoImpl;
import com.littlehorizons.erp.dao.impl.ReportDaoImpl;
import com.littlehorizons.erp.dao.impl.ResourceDaoImpl;
import com.littlehorizons.erp.dao.impl.StudentDaoImpl;
import com.littlehorizons.erp.dao.impl.SubjectDaoImpl;
import com.littlehorizons.erp.dao.impl.SubjectEnrollmentDaoImpl;
import com.littlehorizons.erp.dao.impl.TeacherDaoImpl;
import com.littlehorizons.erp.util.AppLogger;
import com.littlehorizons.erp.util.ConsoleUI;
import com.littlehorizons.erp.util.DatabaseConnection;
import com.littlehorizons.erp.util.InputClosedException;
import com.littlehorizons.erp.util.InputReader;
import com.littlehorizons.erp.util.InvoicePdfGenerator;
import com.littlehorizons.erp.view.AcademicView;
import com.littlehorizons.erp.view.ExtracurricularView;
import com.littlehorizons.erp.view.FinanceView;
import com.littlehorizons.erp.examen.src.view.MainMenuView;
import com.littlehorizons.erp.view.ReportView;
import com.littlehorizons.erp.view.ResourceView;
import com.littlehorizons.erp.view.StudentView;
import com.littlehorizons.erp.view.TeacherView;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.sql.SQLException;
import java.util.Scanner;
import com.littlehorizons.erp.examen.src.view.ExportView;
/** Application entry point: wires DAOs, controllers and views (manual dependency injection). */
public class Main {

    public static void main(String[] args) {
        configureConsoleEncoding();
        AppLogger.info("Aplicación iniciada");

        try {
            DatabaseConnection.getInstance().testConnection();
        } catch (SQLException e) {
            AppLogger.error("No se pudo conectar a la base de datos", e);
            ConsoleUI.error("No se pudo conectar a la base de datos: " + e.getMessage());
            ConsoleUI.line("Verifique que MySQL 8 esté en ejecución, que exista la base campuslands,");
            ConsoleUI.line("que db.properties tenga el usuario/clave correctos y que el driver JDBC esté en Libraries.");
            return;
        }

        // DAOs
        StudentDao studentDao = new StudentDaoImpl();
        GuardianDao guardianDao = new GuardianDaoImpl();
        GradeLevelDao gradeDao = new GradeLevelDaoImpl();
        EnrollmentDao enrollmentDao = new EnrollmentDaoImpl();
        SubjectDao subjectDao = new SubjectDaoImpl();
        TeacherDao teacherDao = new TeacherDaoImpl();
        AssignmentDao assignmentDao = new AssignmentDaoImpl();
        SubjectEnrollmentDao subjectEnrollmentDao = new SubjectEnrollmentDaoImpl();
        AcademicRecordDao recordDao = new AcademicRecordDaoImpl();
        AttendanceDao attendanceDao = new AttendanceDaoImpl();
        ClubDao clubDao = new ClubDaoImpl();
        EventDao eventDao = new EventDaoImpl();
        PaymentConceptDao conceptDao = new PaymentConceptDaoImpl();
        PaymentDao paymentDao = new PaymentDaoImpl();
        InvoiceDao invoiceDao = new InvoiceDaoImpl();
        ResourceDao resourceDao = new ResourceDaoImpl();
        LoanDao loanDao = new LoanDaoImpl();
        ReportDao reportDao = new ReportDaoImpl();

        // Controllers
        StudentController studentController = new StudentController(studentDao, guardianDao, enrollmentDao, gradeDao);
        TeacherController teacherController = new TeacherController(teacherDao, assignmentDao, subjectDao);
        AcademicController academicController = new AcademicController(subjectDao, studentDao, teacherDao,
                enrollmentDao, subjectEnrollmentDao, recordDao, attendanceDao, assignmentDao, gradeDao);
        ExtracurricularController extraController = new ExtracurricularController(clubDao, eventDao, studentDao,
                teacherDao);
        FinanceController financeController = new FinanceController(conceptDao, paymentDao, invoiceDao, guardianDao,
                studentDao, new InvoicePdfGenerator());
        ResourceController resourceController = new ResourceController(resourceDao, loanDao, studentDao, teacherDao);
        ReportController reportController = new ReportController(reportDao, financeController, resourceController);

        // Views
        try (Scanner scanner = new Scanner(System.in, "UTF-8")) {
            InputReader input = new InputReader(scanner);
            MainMenuView menu = new MainMenuView(input,
                    new StudentView(input, studentController, teacherController),
                    new AcademicView(input, studentController, teacherController, academicController),
                    new TeacherView(input, studentController, teacherController),
                    new ExtracurricularView(input, studentController, teacherController, extraController),
                    new FinanceView(input, studentController, teacherController, financeController),
                    new ResourceView(input, studentController, teacherController, resourceController),
                    new ReportView(input, studentController, teacherController,  reportController),
                    new ExportView(input, studentController,teacherController, financeController, resourceController, academicController));
            menu.show();
        } catch (InputClosedException e) {
            ConsoleUI.info("Entrada finalizada. Cerrando la aplicación.");
        } catch (RuntimeException e) {
            AppLogger.error("Error fatal no controlado", e);
            ConsoleUI.error("Error fatal. Revise " + AppLogger.getLogFile());
        }
        AppLogger.info("Aplicación finalizada");
    }

    /** Forces UTF-8 on standard output so accents and box characters render correctly. */
    private static void configureConsoleEncoding() {
        try {
            System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true, "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            AppLogger.error("UTF-8 no soportado en la consola", e);
        }
    }
}
