package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.ClubDao;
import com.littlehorizons.erp.dao.EventDao;
import com.littlehorizons.erp.dao.StudentDao;
import com.littlehorizons.erp.dao.TeacherDao;
import com.littlehorizons.erp.model.Club;
import com.littlehorizons.erp.model.SchoolEvent;
import com.littlehorizons.erp.model.Student;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.TableData;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

/** Business rules for clubs and school events. */
public class ExtracurricularController {

    public static final List<String> EVENT_TYPES = Arrays.asList("Académico", "Cultural", "Deportivo", "Social", "Científico");
    public static final List<String> POSITIONS = Arrays.asList("1er Lugar", "2do Lugar", "3er Lugar", "Participante");
    public static final List<String> ROLES = Arrays.asList("Participante", "Competidor", "Organizador", "Asistente");

    private final ClubDao clubDao;
    private final EventDao eventDao;
    private final StudentDao studentDao;
    private final TeacherDao teacherDao;

    public ExtracurricularController(ClubDao clubDao, EventDao eventDao, StudentDao studentDao,
            TeacherDao teacherDao) {
        this.clubDao = clubDao;
        this.eventDao = eventDao;
        this.studentDao = studentDao;
        this.teacherDao = teacherDao;
    }

    private Student activeStudent(int studentId) throws ErpException {
        Student s = studentDao.findById(studentId)
                .orElseThrow(() -> new ErpException("No existe el estudiante " + studentId + "."));
        if (!"Activo".equals(s.getStatus())) {
            throw new ErpException("El estudiante no está activo (estado: " + s.getStatus() + ").");
        }
        return s;
    }

    private void requireTeacher(Integer teacherId) throws ErpException {
        if (teacherId != null && !teacherDao.findById(teacherId).isPresent()) {
            throw new ErpException("No existe el profesor " + teacherId + ".");
        }
    }

    // ---------- Clubs ----------

    public int createClub(Club club) throws ErpException {
        requireTeacher(club.getSupervisorId());
        if (club.getMaxCapacity() < 1) {
            throw new ErpException("El cupo máximo debe ser al menos 1.");
        }
        club.setActive(true);
        return clubDao.insert(club);
    }

    public TableData listClubs() throws ErpException {
        return clubDao.findAllSummary();
    }

    public void joinClub(int clubId, int studentId) throws ErpException {
        activeStudent(studentId);
        clubDao.addMember(clubId, studentId, LocalDate.now());
    }

    public void leaveClub(int clubId, int studentId, String reason) throws ErpException {
        clubDao.withdrawMember(clubId, studentId, LocalDate.now(), reason);
    }

    public TableData clubMembers(int clubId) throws ErpException {
        clubDao.findById(clubId).orElseThrow(() -> new ErpException("No existe el club " + clubId + "."));
        return clubDao.findMembers(clubId);
    }

    // ---------- Events ----------

    public int createEvent(SchoolEvent event) throws ErpException {
        if (!EVENT_TYPES.contains(event.getType())) {
            throw new ErpException("Tipo de evento inválido.");
        }
        requireTeacher(event.getOrganizerId());
        event.setActive(true);
        return eventDao.insert(event);
    }

    public TableData listEvents() throws ErpException {
        return eventDao.findAllSummary();
    }

    public void addParticipant(int eventId, int studentId, String role) throws ErpException {
        SchoolEvent event = eventDao.findById(eventId)
                .orElseThrow(() -> new ErpException("No existe el evento " + eventId + "."));
        activeStudent(studentId);
        if (eventDao.isParticipant(eventId, studentId)) {
            throw new ErpException("El estudiante ya está registrado en este evento.");
        }
        if (!ROLES.contains(role)) {
            throw new ErpException("Rol inválido.");
        }
        eventDao.addParticipant(event.getId(), studentId, role, null);
    }

    public void setPosition(int eventId, int studentId, String position) throws ErpException {
        SchoolEvent event = eventDao.findById(eventId)
                .orElseThrow(() -> new ErpException("No existe el evento " + eventId + "."));
        if (!event.isCompetitive()) {
            throw new ErpException("Este evento no tiene competencia; no se registran posiciones.");
        }
        if (!POSITIONS.contains(position)) {
            throw new ErpException("Posición inválida.");
        }
        if (eventDao.setPosition(eventId, studentId, position) == 0) {
            throw new ErpException("El estudiante no está registrado en este evento.");
        }
    }

    public TableData eventParticipants(int eventId) throws ErpException {
        eventDao.findById(eventId).orElseThrow(() -> new ErpException("No existe el evento " + eventId + "."));
        return eventDao.findParticipants(eventId);
    }
}
