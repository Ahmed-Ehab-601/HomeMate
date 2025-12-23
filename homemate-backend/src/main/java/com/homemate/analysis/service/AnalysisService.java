package com.homemate.analysis.service;

import com.homemate.analysis.dao.AnalysisDao;
import com.homemate.analysis.dto.AnalysisResponse;

import org.springframework.stereotype.Service;

@Service
public class AnalysisService {

    private final AnalysisDao analysisDao;

    public AnalysisService(AnalysisDao analysisDao) {
        this.analysisDao = analysisDao;
    }

    public AnalysisResponse getAnalysis() {
        return AnalysisResponse.builder()
                .userAnalysis(analysisDao.getUserAnalysis())
                .taskerAnalysis(analysisDao.getTaskerAnalysis())
                .taskAnalysis(analysisDao.getTaskAnalysis())
                .build();
    }
}
