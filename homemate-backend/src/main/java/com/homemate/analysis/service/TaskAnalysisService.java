package com.homemate.analysis.service;

import com.homemate.analysis.dao.TaskAnalysisDao;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TaskAnalysisService {
    private final TaskAnalysisDao dao;
}
