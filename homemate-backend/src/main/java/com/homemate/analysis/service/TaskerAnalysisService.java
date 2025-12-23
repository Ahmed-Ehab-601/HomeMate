package com.homemate.analysis.service;

import com.homemate.analysis.dao.TaskerAnalysisDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskerAnalysisService {
    private final TaskerAnalysisDao dao;
}
