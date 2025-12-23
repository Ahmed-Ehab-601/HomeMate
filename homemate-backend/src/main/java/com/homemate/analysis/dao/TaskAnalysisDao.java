package com.homemate.analysis.dao;

import org.springframework.jdbc.core.JdbcTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TaskAnalysisDao {
    private final JdbcTemplate jdbcTemplate;
}
