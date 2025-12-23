package com.homemate.analysis.service;

import com.homemate.analysis.dao.UserAnalysisDao;
import com.homemate.analysis.dto.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserAnalysisServiceTest {

    @Mock
    private UserAnalysisDao dao;

    @InjectMocks
    private UserAnalysisService service;

    @Test
    void getGenderCount_delegatesToDao() {
        GenderCountResponse resp = new GenderCountResponse(7L, 8L);
        when(dao.fetchGenderCounts()).thenReturn(resp);

        GenderCountResponse actual = service.getGenderCount();
        assertEquals(resp, actual);
    }

    @Test
    void getNewAccountsCounts_delegatesToDao() {
        NewAccountsRequest req = new NewAccountsRequest(
                List.of(new TimeRange(LocalDateTime.now().minusDays(3), LocalDateTime.now())));
        NewAccountsResponse resp = new NewAccountsResponse(List.of(12L));
        when(dao.fetchNewAccountsCounts(req)).thenReturn(resp);

        NewAccountsResponse actual = service.getNewAccountsCounts(req);
        assertEquals(resp, actual);
    }
}
