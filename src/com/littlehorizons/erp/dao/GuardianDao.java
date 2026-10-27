package com.littlehorizons.erp.dao;

import com.littlehorizons.erp.model.Guardian;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.TableData;
import java.util.Optional;

public interface GuardianDao {

    int insert(Guardian guardian) throws ErpException;

    Optional<Guardian> findById(int id) throws ErpException;

    Optional<Guardian> findByDocument(String documentNumber) throws ErpException;

    /** Main guardian of the student (falls back to any financially responsible guardian). */
    Optional<Guardian> findMainGuardian(int studentId) throws ErpException;

    TableData findByStudent(int studentId) throws ErpException;

    boolean isLinked(int studentId, int guardianId) throws ErpException;

    void link(int studentId, int guardianId, boolean main, boolean emergencyContact) throws ErpException;
}
