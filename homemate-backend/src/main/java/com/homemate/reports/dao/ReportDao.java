package com.homemate.reports.dao;

import com.homemate.reports.dto.ShortReport;
import com.homemate.reports.dto.ReportFilterDto;
import java.util.List;

public interface ReportDao {
    List<ShortReport> getAllShortReports(Long limit, Long offset, ReportFilterDto filterDto);
    Long countAllReports(ReportFilterDto filterDto);
}
