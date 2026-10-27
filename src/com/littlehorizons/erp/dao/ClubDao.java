package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Club;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.TableData;
import java.time.LocalDate;
import java.util.Optional;

public interface ClubDao {

    int insert(Club club) throws ErpException;

    Optional<Club> findById(int id) throws ErpException;

    TableData findAllSummary() throws ErpException;

    /** Atomically validates capacity and registers the membership. */
    void addMember(int clubId, int studentId, LocalDate date) throws ErpException;

    void withdrawMember(int clubId, int studentId, LocalDate date, String reason) throws ErpException;

    TableData findMembers(int clubId) throws ErpException;
}
