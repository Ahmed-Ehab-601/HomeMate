package com.homemate.analysis.service;

import com.homemate.analysis.dao.TaskAnalysisDao;
import com.homemate.analysis.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskAnalysisServiceTest {

    @Mock
    private TaskAnalysisDao dao;

    @InjectMocks
    private TaskAnalysisService service;

    @Test
    void getBillRanges_delegatesToDao() {
        BillRangesResponse resp = new BillRangesResponse(Map.of("0-10", 9L));
        when(dao.fetchBillRanges()).thenReturn(resp);

        assertEquals(resp, service.getBillRanges());
    }

    @Test
    void getStartDateRanges_delegatesToDao() {
        TaskDateRangesRequest req = new TaskDateRangesRequest(
                List.of(new TimeRange(LocalDateTime.now().minusDays(2), LocalDateTime.now())));
        TaskDateRangesResponse resp = new TaskDateRangesResponse(List.of(6L));
        when(dao.fetchStartDateRanges(req)).thenReturn(resp);

        assertEquals(resp, service.getStartDateRanges(req));
    }
}
