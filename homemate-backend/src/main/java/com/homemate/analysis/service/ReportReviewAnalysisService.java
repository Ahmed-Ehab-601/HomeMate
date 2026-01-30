package com.homemate.analysis.service;

import com.homemate.analysis.dao.ReportReviewAnalysisDao;
import com.homemate.analysis.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ReportReviewAnalysisService {

    private final ReportReviewAnalysisDao reportReviewAnalysisDao;

    public ReportsPerServiceResponse getReportsPerService() {
        return reportReviewAnalysisDao.fetchReportsPerService();
    }

    public ReviewsPerServiceResponse getReviewsPerService() {
        return reportReviewAnalysisDao.fetchReviewsPerService();
    }

    public ReportStatusCountResponse getReportStatusCounts() {
        return reportReviewAnalysisDao.fetchReportStatusCounts();
    }

    public AvgRatingPerServiceResponse getAvgRatingPerService() {
        return reportReviewAnalysisDao.fetchAvgRatingPerService();
    }
}
