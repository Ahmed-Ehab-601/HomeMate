package com.homemate.analysis.service.imp;

import com.homemate.analysis.dao.AnalysisDao;
import com.homemate.analysis.dto.AnalysisResponse;
import com.homemate.analysis.service.IAnalysisService;
import org.springframework.stereotype.Service;

@Service
public class AnalysisServiceImp implements IAnalysisService {

    private final AnalysisDao analysisDao;

    public AnalysisServiceImp(AnalysisDao analysisDao) {
        this.analysisDao = analysisDao;
    }

    @Override
    public AnalysisResponse getAnalysis() {
        return AnalysisResponse.builder()
                .userAnalysis(analysisDao.getUserAnalysis())
                .taskerAnalysis(analysisDao.getTaskerAnalysis())
                .taskAnalysis(analysisDao.getTaskAnalysis())
                .build();
    }
}
