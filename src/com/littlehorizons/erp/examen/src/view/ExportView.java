package com.littlehorizons.erp.examen.src.view;

import com.littlehorizons.erp.controller.AcademicController;
import com.littlehorizons.erp.controller.FinanceController;
import com.littlehorizons.erp.controller.ResourceController;
import com.littlehorizons.erp.controller.StudentController;
import com.littlehorizons.erp.controller.TeacherController;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.model.Teacher;
import com.littlehorizons.erp.util.AppLogger;
import com.littlehorizons.erp.util.ConsoleUI;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.InputReader;
import com.littlehorizons.erp.view.BaseView;
import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class ExportView extends BaseView {

    private static final Path EXPORT_DIR = Paths.get("exportes");
    private static final Path AUDIT_FILE = EXPORT_DIR.resolve("bitacora_exportes.txt");
    private static final DateTimeFormatter STAMP = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter READABLE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final double PASSING_GRADE = 3.0;

    private final FinanceController finance;
    private final ResourceController resources;
    private final AcademicController academic;

    public ExportView(InputReader input, StudentController students, TeacherController teachers, FinanceController finance,
            ResourceController resources, AcademicController academic) {
        super(input, students, teachers);
        this.finance = finance;
        this.resources = resources;
        this.academic = academic;
    }

    public void show() {
        while (true) {
            ConsoleUI.menu("EXPORTADOR DE MATERIAS DE UN PROFESOR",
                    "Exportar materias (TXT)");
            final int option = input.readInt("Seleccione una opción: ", 0, 1);
            if (option == 0) {
                return;
            }
            safely(() -> dispatch(option));
        }
    }

    private void dispatch(int option) throws ErpException {
        switch (option) {
            case 1: exportReportCard(); break;
            default: break;
        }
    }

    private void exportReportCard() throws ErpException {
        int teacherId = askTeacherId();
        if (teacherId == 0) {
            return;
        }
        int year = askYear();
        Teacher teacher = teachers.getTeacher(teacherId);
        Map<String, Double> bySubject = academic.subjectAverages(teacherId, year);
        if (bySubject.isEmpty()) {
            throw new ErpException("No hay materias registradas para el año " + year + ".");
        }
        Map<String, Double> byPeriod = academic.periodAverages(teacherId, year);
        double overall = academic.overallAverage(teacherId, year);

        Path file = newFile("boletin_" + teacher.getDocumentNumber() + "_" + year, "txt");
        try (BufferedWriter writer = openWriter(file, false)) {
            writeLine(writer, "COLEGIO INTERNACIONAL LITTLE HORIZONS - REPORTE DE MATERIAS ASIGNADAS " + year);
            writeLine(writer, "Profesor: " + teacher.getFullName());
            writeLine(writer, "Documento:  " + teacher.getDocumentType() + " " + teacher.getDocumentNumber());
            writeLine(writer, repeat('=', 52));
            writeLine(writer, String.format("%-30s   ", "Materias Asignadas"));
            writeLine(writer, repeat('-', 52));
            for (Map.Entry<String, Double> e : bySubject.entrySet()) {
                writeLine(writer, String.format("%-30s ", e.getKey(), e.getValue(),
                        e.getValue() >= PASSING_GRADE ? "Aprobada" : "Reprobada"));
            }
            
        } catch (IOException e) {
            throw writeError(e);
        }
        finish(file, bySubject.size() + " materia(s), promedio general " + String.format("%.2f", overall));
    }

    private void appendAudit(Path file, String detail) {
        try (BufferedWriter writer = openWriter(AUDIT_FILE, true)) {
            writeLine(writer, LocalDateTime.now().format(READABLE) + " | " + file.getFileName() + " | " + detail);
        } catch (IOException e) {
            AppLogger.error("No se pudo escribir la bitácora de exportes", e);
        }
    }

    private BufferedWriter openWriter(Path file, boolean append) throws IOException {
        Files.createDirectories(EXPORT_DIR);
        return new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file.toFile(), append),
                StandardCharsets.UTF_8));
    }

    private static void writeLine(BufferedWriter writer, String text) throws IOException {
        writer.write(text);
        writer.newLine();
    }

    private Path newFile(String name, String extension) {
        return EXPORT_DIR.resolve(name + "_" + LocalDateTime.now().format(STAMP) + "." + extension);
    }

    private void finish(Path file, String detail) {
        appendAudit(file, detail);
        ConsoleUI.success("Archivo generado: " + file.toAbsolutePath() + " (" + detail + ")");
    }

    private ErpException writeError(IOException e) {
        AppLogger.error("No se pudo escribir el archivo de exporte", e);
        return new ErpException("No se pudo escribir el archivo: " + e.getMessage(), e);
    }

    private static String repeat(char ch, int times) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < times; i++) {
            sb.append(ch);
        }
        return sb.toString();
    }

    private static String cut(String text, int max) {
        return text.length() <= max ? text : text.substring(0, max - 3) + "...";
    }
}
