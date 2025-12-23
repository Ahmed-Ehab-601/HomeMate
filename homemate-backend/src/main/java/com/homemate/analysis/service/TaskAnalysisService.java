package com.homemate.analysis.service;

import com.homemate.analysis.dao.TaskAnalysisDao;
import com.homemate.analysis.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskAnalysisService {
    private final TaskAnalysisDao dao;

    public TaskDateRangesResponse getStartDateRanges(TaskDateRangesRequest request) {
        return dao.fetchStartDateRanges(request);
    }

    public TaskDateRangesResponse getEndDateRanges(TaskDateRangesRequest request) {
        return dao.fetchEndDateRanges(request);
    }

    public BillRangesResponse getBillRanges() {
        return dao.fetchBillRanges();
    }

    public TaskStatusCountsResponse getStatusCounts() {
        return dao.fetchStatusCounts();
    }
}
