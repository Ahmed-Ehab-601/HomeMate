package com.homemate.reports.dao;

import com.homemate.reports.dto.ShortReport;
import com.homemate.reports.dto.SubmitReport;
import java.util.List;

public interface ReportDao {
    List<ShortReport> getAllShortReports(Long limit, Long offset);
    Long countAllReports();

    boolean submitReport(SubmitReport submitReport, boolean reporterIsUser);

    boolean taskExists(long taskId);

    boolean taskOwnedByUser(long taskId, long userId);

    boolean taskOwnedByTasker(long taskId, long taskerId);
}
