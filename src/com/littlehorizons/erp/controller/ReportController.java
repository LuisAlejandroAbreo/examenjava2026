package com.littlehorizons.erp.controller;

import com.littlehorizons.erp.dao.ReportDao;
import com.littlehorizons.erp.util.ErpException;
import com.littlehorizons.erp.util.TableData;
import java.time.LocalDate;

/** Read-only statistics and reports. */
public class ReportController {

    private final ReportDao reportDao;
    private final FinanceController financeController;
    private final ResourceController resourceController;

    public ReportController(ReportDao reportDao, FinanceController financeController,
            ResourceController resourceController) {
        this.reportDao = reportDao;
        this.financeController = financeController;
        this.resourceController = resourceController;
    }

    public TableData absencePatterns(int weeksBack, int maxAbsences) throws ErpException {
        return reportDao.findAbsencePatterns(LocalDate.now().minusWeeks(weeksBack), maxAbsences);
    }

    public TableData delinquents() throws ErpException {
        return financeController.delinquents();
    }

    public TableData overdueLoans() throws ErpException {
        return resourceController.overdueLoans();
    }

    public TableData eventParticipation() throws ErpException {
        return reportDao.findEventParticipation();
    }

    public TableData studentsByGrade(int year) throws ErpException {
        return reportDao.findStudentsByGrade(year);
    }

    public TableData clubOccupancy() throws ErpException {
        return reportDao.findClubOccupancy();
    }

    public TableData topStudents(int year, int limit) throws ErpException {
        return reportDao.findTopStudents(year, limit);
    }
}
