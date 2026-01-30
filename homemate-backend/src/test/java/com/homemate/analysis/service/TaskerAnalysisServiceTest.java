package com.homemate.analysis.service;

import com.homemate.analysis.dao.TaskerAnalysisDao;
import com.homemate.analysis.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskerAnalysisServiceTest {

    @Mock
    private TaskerAnalysisDao dao;

    @InjectMocks
    private TaskerAnalysisService service;

    @Test
    void getRatingRanges_delegatesToDao() {
        RatingRangesResponse resp = new RatingRangesResponse(Map.of("0-1", 1L));
        when(dao.fetchRatingRanges()).thenReturn(resp);

        assertEquals(resp, service.getRatingRanges());
    }

    @Test
    void getCityCounts_delegatesToDao() {
        CityCountRequest req = new CityCountRequest(List.of("Alex", "Cairo"));
        CityCountResponse resp = new CityCountResponse(Map.of("Alex", 3L, "Cairo", 5L));
        when(dao.fetchCityCounts(req)).thenReturn(resp);

        assertEquals(resp, service.getCityCounts(req));
    }
}
