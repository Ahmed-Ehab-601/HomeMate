package com.homemate.analysis.service;

import com.homemate.analysis.dao.UserAnalysisDao;
import com.homemate.analysis.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserAnalysisService {

    private final UserAnalysisDao dao;

    public GenderCountResponse getGenderCount() {
        return dao.fetchGenderCounts();
    }

    public StatusCountsResponse getStatusCounts() {
        return dao.fetchStatusCounts();
    }

    public AgeBucketsResponse getAgeBuckets() {
        return dao.fetchAgeBuckets();
    }

    public NewAccountsResponse getNewAccountsCounts(NewAccountsRequest request) {
        return dao.fetchNewAccountsCounts(request);
    }
}
