package com.homemate.reports.dao;

import com.homemate.reports.dto.ShortReport;
import java.util.List;

public interface ReportDao {
    List<ShortReport> getAllShortReports(Long limit, Long offset);
    Long countAllReports();
}
