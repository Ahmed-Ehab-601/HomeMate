package com.homemate.analysis.dao;

import com.homemate.analysis.dto.*;

public interface AnalysisDao {
    UserAnalysisDto getUserAnalysis();
    TaskerAnalysisDto getTaskerAnalysis();
    TaskAnalysisDto getTaskAnalysis();
}
