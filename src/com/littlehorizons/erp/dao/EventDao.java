package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.SchoolEvent;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.TableData;
import java.util.Optional;

public interface EventDao {

    int insert(SchoolEvent event) throws ErpException;

    Optional<SchoolEvent> findById(int id) throws ErpException;

    TableData findAllSummary() throws ErpException;

    boolean isParticipant(int eventId, int studentId) throws ErpException;

    void addParticipant(int eventId, int studentId, String role, String position) throws ErpException;

    int setPosition(int eventId, int studentId, String position) throws ErpException;

    TableData findParticipants(int eventId) throws ErpException;
}
