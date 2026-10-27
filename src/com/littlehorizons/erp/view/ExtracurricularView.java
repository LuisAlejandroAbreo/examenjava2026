package com.littlehorizons.erp.view;

import com.littlehorizons.erp.controller.ExtracurricularController;
import com.littlehorizons.erp.controller.StudentController;
import com.littlehorizons.erp.controller.TeacherController;
import com.littlehorizons.erp.model.Club;
import com.littlehorizons.erp.model.SchoolEvent;
import com.littlehorizons.erp.util.ConsoleUI;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.InputReader;

/** Console view for clubs and school events. */
public class ExtracurricularView extends BaseView {

    private final ExtracurricularController extra;

    public ExtracurricularView(InputReader input, StudentController students, TeacherController teachers, ExtracurricularController extra) {
        super(input, students, teachers);
        this.extra = extra;
    }

    public void show() {
        while (true) {
            ConsoleUI.menu("ACTIVIDADES EXTRACURRICULARES", "Listar clubes", "Crear club",
                    "Inscribir estudiante en un club", "Retirar estudiante de un club", "Ver miembros de un club",
                    "Listar eventos", "Crear evento", "Registrar participante en un evento",
                    "Registrar posición obtenida", "Ver participantes de un evento");
            final int option = input.readInt("Seleccione una opción: ", 0, 10);
            if (option == 0) {
                return;
            }
            safely(() -> dispatch(option));
        }
    }

    private void dispatch(int option) throws ErpException {
        switch (option) {
            case 1: ConsoleUI.table(extra.listClubs()); break;
            case 2: createClub(); break;
            case 3: joinClub(); break;
            case 4: leaveClub(); break;
            case 5: clubMembers(); break;
            case 6: ConsoleUI.table(extra.listEvents()); break;
            case 7: createEvent(); break;
            case 8: addParticipant(); break;
            case 9: setPosition(); break;
            case 10: eventParticipants(); break;
            default: break;
        }
    }

    private Integer askOptionalTeacher() {
        int id = input.readInt("ID del profesor responsable (0 = ninguno): ", 0, Integer.MAX_VALUE);
        return id == 0 ? null : id;
    }

    private void createClub() throws ErpException {
        ConsoleUI.section("Nuevo club");
        Club c = new Club();
        c.setName(input.readRequired("Nombre del club: ", 100));
        c.setDescription(input.readOptional("Descripción", 500));
        c.setSupervisorId(askOptionalTeacher());
        c.setMaxCapacity(input.readInt("Cupo máximo (1-500): ", 1, 500));
        c.setMeetingSchedule(input.readOptional("Horario de reunión (ej: Martes 3-5 PM)", 100));
        int id = extra.createClub(c);
        ConsoleUI.success("Club creado con ID " + id + ".");
    }

    private void joinClub() throws ErpException {
        ConsoleUI.table(extra.listClubs());
        int clubId = input.readId("ID del club");
        if (clubId == 0) {
            return;
        }
        int studentId = askStudentId();
        if (studentId == 0) {
            return;
        }
        extra.joinClub(clubId, studentId);
        ConsoleUI.success("Estudiante inscrito en el club.");
    }

    private void leaveClub() throws ErpException {
        int clubId = input.readId("ID del club");
        if (clubId == 0) {
            return;
        }
        int studentId = askStudentId();
        if (studentId == 0) {
            return;
        }
        String reason = input.readOptional("Motivo del retiro", 255);
        extra.leaveClub(clubId, studentId, reason);
        ConsoleUI.success("Estudiante retirado del club.");
    }

    private void clubMembers() throws ErpException {
        int clubId = input.readId("ID del club");
        if (clubId != 0) {
            ConsoleUI.table(extra.clubMembers(clubId));
        }
    }

    private void createEvent() throws ErpException {
        ConsoleUI.section("Nuevo evento");
        SchoolEvent e = new SchoolEvent();
        e.setName(input.readRequired("Nombre del evento: ", 150));
        e.setDescription(input.readOptional("Descripción", 500));
        e.setType(input.readOption("Tipo de evento:", ExtracurricularController.EVENT_TYPES.toArray(new String[0])));
        e.setEventDate(input.readDate("Fecha del evento"));
        e.setLocation(input.readOptional("Lugar", 150));
        e.setCompetitive(input.confirm("¿Tiene competencia con posiciones?"));
        e.setOrganizerId(askOptionalTeacher());
        int id = extra.createEvent(e);
        ConsoleUI.success("Evento creado con ID " + id + ".");
    }

    private void addParticipant() throws ErpException {
        ConsoleUI.table(extra.listEvents());
        int eventId = input.readId("ID del evento");
        if (eventId == 0) {
            return;
        }
        int studentId = askStudentId();
        if (studentId == 0) {
            return;
        }
        String role = input.readOption("Rol:", ExtracurricularController.ROLES.toArray(new String[0]));
        extra.addParticipant(eventId, studentId, role);
        ConsoleUI.success("Participante registrado.");
    }

    private void setPosition() throws ErpException {
        int eventId = input.readId("ID del evento (con competencia)");
        if (eventId == 0) {
            return;
        }
        int studentId = askStudentId();
        if (studentId == 0) {
            return;
        }
        String position = input.readOption("Posición obtenida:", ExtracurricularController.POSITIONS.toArray(new String[0]));
        extra.setPosition(eventId, studentId, position);
        ConsoleUI.success("Posición registrada.");
    }

    private void eventParticipants() throws ErpException {
        int eventId = input.readId("ID del evento");
        if (eventId != 0) {
            ConsoleUI.table(extra.eventParticipants(eventId));
        }
    }
}
